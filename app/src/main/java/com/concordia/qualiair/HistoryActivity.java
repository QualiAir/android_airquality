package com.concordia.qualiair;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends AppCompatActivity {

    private TextView buttonNh3;
    private TextView buttonCo2;
    private TextView buttonPm25;
    private LineChart lineChart;
    private RecyclerView recyclerView;
    private ReadingAdapter adapter;

    private TextView yAxisTitleTextView;
    private List<Reading> readings = new ArrayList<>();

    // PM2.5: Based on EPA 2024 AQI Breakpoints
    private static final float PM25_CAUTION = 9.0f;  // Top of "Good"
    private static final float PM25_ALARM = 35.4f;   // Top of "Moderate"

    // CO2: Based on Indoor Ventilation Standards (ASHRAE)
    private static final float CO2_CAUTION = 1000f;  // Stuffy air
    private static final float CO2_ALARM = 2000f;    // Significant drowsiness/headache

    // NH3: Based on NIOSH Occupational Safety
    private static final float NH3_CAUTION = 25f;    // Recommended exposure limit
    private static final float NH3_ALARM = 35f;     // Short-term exposure limit (15 min)

    private float currentCaution = NH3_CAUTION;
    private float currentAlarm = NH3_ALARM;

    private TextView buttonFilterAlarm;
    private boolean isFilterActive = false;
    private List<Reading> filteredReadings = new ArrayList<>();

    //drop down menu for daily, weekly, monthly, last hour
    private Spinner timeRangeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //Buttons
        buttonNh3 = findViewById(R.id.button_nh3);
        buttonCo2 = findViewById(R.id.button_co2);
        buttonPm25 = findViewById(R.id.button_pm25);
        buttonFilterAlarm = findViewById(R.id.button_filter_alarm);

        //showing ppm on y axis
        yAxisTitleTextView = findViewById(R.id.y_axis_title);

        //Chart
        lineChart = findViewById(R.id.line_chart);
        // helper function to initialize and configure the LineChart
        setupChart();

        //RecyclerView
        recyclerView = findViewById(R.id.recycler_readings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //drop down time at the top right
        timeRangeSpinner = findViewById(R.id.time_range_spinner);
        setupTimeRangeSpinner();
        filteredReadings.addAll(readings); // Initialize with all data
        adapter = new ReadingAdapter(filteredReadings);
        recyclerView.setAdapter(adapter);
        updateButtonStates(buttonNh3);
        loadNh3Data();

        //setOnClickListerner for each buton
        buttonNh3.setOnClickListener(v -> {
            updateButtonStates(buttonNh3);
            loadNh3Data();
        });
        buttonCo2.setOnClickListener(v -> {
            updateButtonStates(buttonCo2);
            loadCo2Data();
        });
        buttonPm25.setOnClickListener(v -> {
            updateButtonStates(buttonPm25);
            loadPm25Data();
        });

        buttonFilterAlarm.setOnClickListener(v -> {
            isFilterActive = !isFilterActive; // Toggle state

            if (isFilterActive) {
                buttonFilterAlarm.setText("Show All");
                buttonFilterAlarm.setTextColor(getColor(R.color.danger));
                buttonFilterAlarm.setBackgroundResource(R.drawable.bg_btn_selected); // Or a specific alert style
            } else {
                buttonFilterAlarm.setText("Show Alarms");
                buttonFilterAlarm.setTextColor(getColor(R.color.textMuted));
                buttonFilterAlarm.setBackgroundResource(R.drawable.bg_btn_unselected);
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        filteredReadings.clear();
        TextView noAlarmsText = findViewById(R.id.text_no_alarms);

        if (isFilterActive) {
            // Only show readings that meet or exceed the alarm threshold
            for (Reading r : readings) {
                if (r.getValue() >= currentAlarm) {
                    filteredReadings.add(r);
                }
            }
            noAlarmsText.setVisibility(filteredReadings.isEmpty() ? View.VISIBLE : View.GONE);//show message if table empty after filetr
        } else {
            // Show everything
            filteredReadings.addAll(readings);
            noAlarmsText.setVisibility(View.GONE);//hide message on show all
        }
        adapter.notifyDataSetChanged();
    }

    private String getStatus(float value, float caution, float alarm) {
        if (value >= alarm) return "High";
        if (value >= caution) return "Moderate";
        return "Low";
    }

    private void setupTimeRangeSpinner() {
        // Create an adapter to populate the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                // Array in strings.xml
                R.array.time_range_options,
                // Default Android layout for the selected item view
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the dropdown list appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply this adapter to spinner
        timeRangeSpinner.setAdapter(adapter);

        // Set a listener that will be called when the user selects a new item
        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //  Key for the backend (history.py)
                String backendRangeKey;
                String selectedTimeRange = parent.getItemAtPosition(position).toString();
                if (selectedTimeRange.equals("Daily")) backendRangeKey = "24h";
                else if (selectedTimeRange.equals("Weekly")) backendRangeKey = "1w";
                else if (selectedTimeRange.equals("Monthly")) backendRangeKey = "1m";
                else backendRangeKey = "1h";

                // Trigger a refresh of the current gas data
                // Check which gas button is currently "selected" and reload it
                //the range for pm25 is µg
                if (yAxisTitleTextView.getText().toString().contains("µg")) {
                    loadPm25Data();
                } else if (buttonCo2.getCurrentTextColor() == getColor(R.color.safe)) {
                    loadCo2Data();
                } else {
                    loadNh3Data();
                }

                // Verify the mapping for the demo
                android.widget.Toast.makeText(HistoryActivity.this,
                        "Range updated to: " + backendRangeKey,
                        android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This method is required, but we can leave it empty.
            }
        });
    }

    private void updateButtonStates(TextView selectedButton) {
        TextView[] allButtons = {buttonNh3, buttonCo2, buttonPm25};
        for (TextView button : allButtons) {
            if (button.getId() == selectedButton.getId()) {
                selectButton(button);
            } else {
                unselectedButton(button);
            }
        }
    }

    private void setupChart() {
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // left, top, right, bottom
        lineChart.setExtraOffsets(12f, 20f, 12f, 20f);


        // --- X-Axis (Horizontal) Styling ---
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getColor(R.color.supreme));
        xAxis.setGranularity(1f);
        // show the X-axis line
        xAxis.setDrawAxisLine(true);
        //set its color
        xAxis.setAxisLineColor(getColor(R.color.supreme));

        // --- Y-Axis (Vertical) Styling ---
        lineChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setTextColor(getColor(R.color.supreme));
        leftAxis.setXOffset(10f);
        // show the Y-axis line
        leftAxis.setDrawAxisLine(true);
        // set its color
        leftAxis.setAxisLineColor(getColor(R.color.supreme));

        // First, clear any old lines to be safe
        leftAxis.removeAllLimitLines();

        // ALARM ZONE BOUNDARY (Red)
        // Set the threshold for alarm
        LimitLine alarmLimit = new LimitLine(80f, "Alarm");
        alarmLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        alarmLimit.setTextSize(10f);
        alarmLimit.setTextColor(getColor(R.color.danger));
        alarmLimit.setLineWidth(2f);
        alarmLimit.enableDashedLine(20f, 10f, 0f);
        alarmLimit.setLineColor(getColor(R.color.danger));
        leftAxis.addLimitLine(alarmLimit);

        // CAUTION ZONE BOUNDARY (Yellow)
        // Set your threshold for warning
        LimitLine cautionLimit = new LimitLine(40f, "Caution");
        cautionLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        cautionLimit.setTextSize(10f);
        cautionLimit.setTextColor(getColor(R.color.warning));
        cautionLimit.setLineWidth(2f);
        cautionLimit.enableDashedLine(20f, 10f, 0f);
        cautionLimit.setLineColor(getColor(R.color.warning));
        leftAxis.addLimitLine(cautionLimit);

        //green data line is on top of the alarm and caution line
        leftAxis.setDrawLimitLinesBehindData(true);
    }

    private void selectButton(TextView button) {
        button.setBackgroundResource(R.drawable.bg_btn_selected);
        button.setTextColor(getColor(R.color.safe));
    }

    private void unselectedButton(TextView button) {
        button.setBackgroundResource(R.drawable.bg_btn_unselected);
        button.setTextColor(getColor(R.color.textMuted));
    }

    //static for now just to showcase the graph
    private void loadNh3Data() {
        yAxisTitleTextView.setText("ppm");
        currentCaution = NH3_CAUTION;
        currentAlarm = NH3_ALARM;

        readings.clear();        // Get the current spinner selection to decide which "fake" data to show
        String currentRange = timeRangeSpinner.getSelectedItem().toString();

        if (currentRange.equals("Daily")) {
            // Simulated 24h data (showing hours)
            readings.add(new Reading("08:00", 12f, getStatus(12f, NH3_CAUTION, NH3_ALARM)));
            readings.add(new Reading("12:00", 40f, getStatus(40f, NH3_CAUTION, NH3_ALARM)));
            readings.add(new Reading("18:00", 27f, getStatus(27f, NH3_CAUTION, NH3_ALARM)));
        } else if (currentRange.equals("Weekly")) {
            // Simulated Weekly data (showing days)
            readings.add(new Reading("Mon", 37f, getStatus(37f, NH3_CAUTION, NH3_ALARM)));
            readings.add(new Reading("Wed", 20f, getStatus(20f, NH3_CAUTION, NH3_ALARM)));
            readings.add(new Reading("Fri", 30f, getStatus(30f, NH3_CAUTION, NH3_ALARM)));
        } else if (currentRange.equals("Monthly")) {
            // Simulated Monthly data (showing months)
            readings.add(new Reading("Jan", 28f, getStatus(28f, NH3_CAUTION, NH3_ALARM)));
            readings.add(new Reading("Feb", 15f, getStatus(15f, NH3_CAUTION, NH3_ALARM)));
        } else {
            // Default "Last Hour" (showing minutes)
            readings.add(new Reading("13:45", 28f, getStatus(28f, NH3_CAUTION, NH3_ALARM)));
            readings.add(new Reading("14:00", 36f, getStatus(36f, NH3_CAUTION, NH3_ALARM)));
            readings.add(new Reading("14:15", 15f, getStatus(15f, NH3_CAUTION, NH3_ALARM)));
        }

        applyFilter(); // checks table if alarm only shows
        updateChart();
    }

    private void loadCo2Data() {
        yAxisTitleTextView.setText("ppm");
        currentCaution = CO2_CAUTION;
        currentAlarm = CO2_ALARM;

        readings.clear();
        String currentRange = timeRangeSpinner.getSelectedItem().toString();

        if (currentRange.equals("Daily")) {
            // Simulated 24h CO2
            readings.add(new Reading("09:00", 1800f, getStatus(1800f, CO2_CAUTION, CO2_ALARM)));
            readings.add(new Reading("10:00", 900f, getStatus(900f, CO2_CAUTION, CO2_ALARM)));
            readings.add(new Reading("14:00", 2200f, getStatus(2200f, CO2_CAUTION, CO2_ALARM)));
        } else if (currentRange.equals("Weekly")) {
            // Simulated Weekly CO2
            readings.add(new Reading("Tue", 2800f, getStatus(2800f, CO2_CAUTION, CO2_ALARM)));
            readings.add(new Reading("Wed", 1700f, getStatus(1700f, CO2_CAUTION, CO2_ALARM)));
            readings.add(new Reading("Thu", 750f, getStatus(750f, CO2_CAUTION, CO2_ALARM)));
        } else if (currentRange.equals("Monthly")) {
            // Simulated Monthly CO2
            readings.add(new Reading("Feb", 500f, getStatus(500f, CO2_CAUTION, CO2_ALARM)));
            readings.add(new Reading("Mar", 1500f, getStatus(1500f, CO2_CAUTION, CO2_ALARM)));
        } else {
            // Default Last Hour
            readings.add(new Reading("13:45", 800f, getStatus(800f, CO2_CAUTION, CO2_ALARM)));
            readings.add(new Reading("14:00", 1200f, getStatus(1200f, CO2_CAUTION, CO2_ALARM)));
            readings.add(new Reading("14:15", 2100f, getStatus(2100f, CO2_CAUTION, CO2_ALARM)));
        }

        applyFilter();
        updateChart();
    }

    private void loadPm25Data() {
        yAxisTitleTextView.setText("µg/m³");
        currentCaution = PM25_CAUTION;
        currentAlarm = PM25_ALARM;

        readings.clear();
        String currentRange = timeRangeSpinner.getSelectedItem().toString();

        if (currentRange.equals("Daily")) {
            readings.add(new Reading("13:00", 8f, getStatus(8f, PM25_CAUTION, PM25_ALARM)));
            readings.add(new Reading("14:00", 15f, getStatus(15f, PM25_CAUTION, PM25_ALARM)));
            readings.add(new Reading("15:00", 42f, getStatus(42f, PM25_CAUTION, PM25_ALARM)));
        } else if (currentRange.equals("Weekly")) {
            readings.add(new Reading("Sat", 18f, getStatus(18f, PM25_CAUTION, PM25_ALARM)));
            readings.add(new Reading("Sun", 5f, getStatus(5f, PM25_CAUTION, PM25_ALARM)));
            readings.add(new Reading("Mon", 38f, getStatus(38f, PM25_CAUTION, PM25_ALARM)));
        } else if (currentRange.equals("Monthly")) {
            readings.add(new Reading("Jun", 15f, getStatus(15f, PM25_CAUTION, PM25_ALARM)));
            readings.add(new Reading("Jul", 4f, getStatus(4f, PM25_CAUTION, PM25_ALARM)));
        } else {
            // Default Last Hour
            readings.add(new Reading("13:45", 8f, getStatus(8f, PM25_CAUTION, PM25_ALARM)));
            readings.add(new Reading("14:00", 15f, getStatus(15f, PM25_CAUTION, PM25_ALARM)));
            readings.add(new Reading("14:15", 40f, getStatus(40f, PM25_CAUTION, PM25_ALARM)));
        }

        applyFilter();
        updateChart();
    }

    private void updateChart() {
        if (readings.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        //for dynamic scaling and limit lines
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.removeAllLimitLines();

        //Will set max value 20% above the highest reading
        float maxValue = 0;
        for (Reading r : readings) if (r.getValue() > maxValue) maxValue = r.getValue();
        leftAxis.setAxisMaximum(Math.max(currentAlarm, maxValue) * 1.2f);

        //create alarm limit line
        LimitLine alarmLimit = new LimitLine(currentAlarm, "Alarm");
        alarmLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        alarmLimit.setTextSize(10f);
        alarmLimit.setTextColor(getColor(R.color.danger)); // Red
        alarmLimit.setLineWidth(2f);
        alarmLimit.enableDashedLine(20f, 10f, 0f);
        alarmLimit.setLineColor(getColor(R.color.danger));
        leftAxis.addLimitLine(alarmLimit);

        //create caution limit line
        LimitLine cautionLimit = new LimitLine(currentCaution, "Caution");
        cautionLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        cautionLimit.setTextSize(10f);
        cautionLimit.setTextColor(getColor(R.color.warning)); // Yellow
        cautionLimit.setLineWidth(2f);
        cautionLimit.enableDashedLine(20f, 10f, 0f);
        cautionLimit.setLineColor(getColor(R.color.warning));
        leftAxis.addLimitLine(cautionLimit);

        leftAxis.setDrawLimitLinesBehindData(true);//limit line stays behind data pointa

        lineChart.getXAxis().setValueFormatter(new TimeAxisValueFormatter(readings));
        ArrayList<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < readings.size(); i++) {
            Reading currentReading = readings.get(i);
            chartEntries.add(new Entry(i, currentReading.getValue()));
        }

        LineDataSet lineDataSet = new LineDataSet(chartEntries, "Readings");

        //readings line settings
        lineDataSet.setColor(getColor(R.color.safe));
        lineDataSet.setLineWidth(3f);
        lineDataSet.setCircleColor(getColor(R.color.safe));
        lineDataSet.setCircleHoleColor(getColor(R.color.supreme));
        lineDataSet.setCircleRadius(6f);
        lineDataSet.setCircleHoleRadius(4f);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawValues(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        lineChart.animateX(500); // Add a simple animation
        lineChart.invalidate();  // Refresh the chart
    }

    private static class TimeAxisValueFormatter extends com.github.mikephil.charting.formatter.ValueFormatter {
        private final List<Reading> readings;

        public TimeAxisValueFormatter(List<Reading> readings) {
            this.readings = readings;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < readings.size()) {
                return readings.get(index).getTime();
            }
            return "";
        }
    }

}
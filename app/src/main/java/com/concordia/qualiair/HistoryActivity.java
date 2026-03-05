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
    private List<Reading> readings =new ArrayList<>();

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
        adapter= new ReadingAdapter(readings);
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

    private void setupChart(){
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
    private void selectButton(TextView button){
        button.setBackgroundResource(R.drawable.bg_btn_selected);
        button.setTextColor(getColor(R.color.safe));
    }
    private void unselectedButton(TextView button){
        button.setBackgroundResource(R.drawable.bg_btn_unselected);
        button.setTextColor(getColor(R.color.textMuted));
    }
    //static for now just to showcase the graph
    private void loadNh3Data(){
        yAxisTitleTextView.setText("ppm");
        readings.clear();        // Get the current spinner selection to decide which "fake" data to show
        String currentRange = timeRangeSpinner.getSelectedItem().toString();

        if (currentRange.equals("Daily")) {
            // Simulated 24h data (showing hours)
            readings.add(new Reading("08:00", 30, "Moderate"));
            readings.add(new Reading("12:00", 85, "High"));    // High = Above 80 (Alarm)
            readings.add(new Reading("18:00", 25, "Low"));
        } else if (currentRange.equals("Weekly")) {
            // Simulated Weekly data (showing days)
            readings.add(new Reading("Mon", 20, "Low"));
            readings.add(new Reading("Wed", 90, "High"));     // High = Above 80
            readings.add(new Reading("Fri", 45, "Moderate")); // Moderate = Above 40
        } else if (currentRange.equals("Monthly")) {
            // Simulated Monthly data (showing months)
            readings.add(new Reading("Jan", 15, "Low"));
            readings.add(new Reading("Feb", 50, "Moderate"));
        } else {
            // Default "Last Hour" (showing minutes)
            readings.add(new Reading("13:45", 82, "High"));
            readings.add(new Reading("14:00", 45, "Moderate"));
            readings.add(new Reading("14:15", 30, "Low"));
        }

        adapter.notifyDataSetChanged();
        updateChart();
    }
    private void loadCo2Data() {
        yAxisTitleTextView.setText("ppm");
        readings.clear();
        String currentRange = timeRangeSpinner.getSelectedItem().toString();

        if (currentRange.equals("Daily")) {
            // Simulated 24h CO2
            readings.add(new Reading("09:00", 35, "Low"));
            readings.add(new Reading("15:00", 82, "High"));
        } else if (currentRange.equals("Weekly")) {
            // Simulated Weekly CO2
            readings.add(new Reading("Tue", 42, "Moderate"));
            readings.add(new Reading("Thu", 30, "Low"));
        } else if (currentRange.equals("Monthly")) {
            // Simulated Monthly CO2
            readings.add(new Reading("Mar", 45, "Moderate"));
            readings.add(new Reading("Apr", 88, "High"));
        } else {
            // Default Last Hour
            readings.add(new Reading("13:45", 43, "Moderate"));
            readings.add(new Reading("14:00", 50, "High"));
            readings.add(new Reading("14:15", 20, "Moderate"));
        }

        adapter.notifyDataSetChanged();
        updateChart();
    }

    private void loadPm25Data() {
        yAxisTitleTextView.setText("µg/m³");
        readings.clear();
        String currentRange = timeRangeSpinner.getSelectedItem().toString();

        if (currentRange.equals("Daily")) {
            readings.add(new Reading("10:00", 15, "Low"));
            readings.add(new Reading("20:00", 45, "High"));
        } else if (currentRange.equals("Weekly")) {
            readings.add(new Reading("Sat", 10, "Low"));
            readings.add(new Reading("Sun", 25, "Moderate"));
        } else if (currentRange.equals("Monthly")) {
            readings.add(new Reading("May", 30, "Moderate"));
            readings.add(new Reading("Jun", 12, "Low"));
        } else {
            // Default Last Hour
            readings.add(new Reading("13:45", 12, "Low"));
            readings.add(new Reading("14:00", 25, "Moderate"));
            readings.add(new Reading("14:15", 40, "High"));
        }

        adapter.notifyDataSetChanged();
        updateChart();
    }
    private void updateChart(){
        if (readings.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }
        lineChart.getXAxis().setValueFormatter(new TimeAxisValueFormatter(readings));
        ArrayList<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < readings.size(); i++) {
            Reading currentReading = readings.get(i);
            chartEntries.add(new Entry(i, currentReading.getValue()));
        }

        LineDataSet lineDataSet = new LineDataSet(chartEntries, "Readings");


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
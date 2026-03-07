package com.concordia.qualiair;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Collections;

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
    private ApiService apiService;
    private final String DEVICE_ID="sensor1";

    private Spinner timeRangeSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("https://backend-airquality.onrender.com/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();

        this.apiService = retrofit.create(ApiService.class);

        //Buttons
        buttonNh3 = findViewById(R.id.button_nh3);
        buttonCo2 = findViewById(R.id.button_co2);
        buttonPm25 = findViewById(R.id.button_pm25);

        //showing ppm on y axis
        yAxisTitleTextView = findViewById(R.id.y_axis_title);

        //Chart
        lineChart = findViewById(R.id.line_chart);
        // helper function to initialize and configure the LineChart
        setupChart("ammonia");

        //RecyclerView
        recyclerView = findViewById(R.id.recycler_readings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //drop down time at the top right
        timeRangeSpinner = findViewById(R.id.time_range_spinner);
        setupTimeRangeSpinner();
        adapter= new ReadingAdapter(readings);
        recyclerView.setAdapter(adapter);

        //setOnClickListerner for each buton
        buttonNh3.setOnClickListener(v -> {
            updateButtonStates(buttonNh3);
            fetchDataFromServer("ammonia"); //call rest api

        });
        buttonCo2.setOnClickListener(v -> {
            updateButtonStates(buttonCo2);
            fetchDataFromServer("hydrogen_sulfide");//call rest api
        });
        buttonPm25.setOnClickListener(v -> {
            updateButtonStates(buttonPm25);
            fetchDataFromServer("dust");//call rest api
        });
        fetchDataFromServer("ammonia");
        updateButtonStates(buttonNh3);
    }
    //create a function that will get the gas , and the range
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

                // Determine which sensor key to send to the Python backend
                String activeSensor = "ammonia";
                if (yAxisTitleTextView.getText().toString().contains("µg")) {
                    activeSensor = "dust"; // Key for PM2.5
                } else if (buttonCo2.getCurrentTextColor() == getColor(R.color.safe)) {
                    activeSensor = "hydrogen_sulfide"; // Key for H2S
                }

                // Call the REAL server function
                fetchDataFromServer(activeSensor);

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
    private void fetchDataFromServer(String sensorName) {
        String selected = timeRangeSpinner.getSelectedItem().toString();
        String rangeKey = "1h";
        if (selected.equals("Daily")) rangeKey = "24h";
        else if (selected.equals("Weekly")) rangeKey = "1w";
        else if (selected.equals("Monthly")) rangeKey = "1m";
        setupChart(sensorName);

        // Use the apiService initialized in onCreate

        HistoryActivity.this.apiService.getHistory(rangeKey, sensorName, this.DEVICE_ID).enqueue(new retrofit2.Callback<HistoryResponse>() {
            @Override
            public void onResponse(retrofit2.Call<HistoryResponse> call, retrofit2.Response<HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Reading> dataFromServer = response.body().getData();

                    if (dataFromServer != null) {
                        readings.clear();
                        readings.addAll(dataFromServer);
                        updateChart();//update chart first

                        List<Reading> reversedForTable = new ArrayList<>(dataFromServer);
                        Collections.reverse(reversedForTable);

                        adapter.updateData(reversedForTable);
                    }
                }

            }
            @Override
            public void onFailure(retrofit2.Call<HistoryResponse> call, Throwable t) {
                android.widget.Toast.makeText(HistoryActivity.this, "Server waking up... try again", android.widget.Toast.LENGTH_SHORT).show();
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

    private void setupChart(String sensorName){

        if (lineChart == null) return;

        // 1. Disable the Right Y-Axis (Removes the extra numbers on the right)
        lineChart.getAxisRight().setEnabled(false);

        // 2. Configure X-Axis (Move Time to Bottom)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Puts time labels at the bottom
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getColor(R.color.supreme));
        xAxis.setGranularity(1f);

        // 3. Configure Left Y-Axis (PPM)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(getColor(R.color.supreme));
        leftAxis.removeAllLimitLines(); // Clear old lines so they don't overlap

        // Basic Configuration
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setExtraOffsets(12f, 40f, 12f, 20f);


        leftAxis.removeAllLimitLines(); // Clear old lines so they don't overlap

        // Variables for the dynamic thresholds
        float alarmValue;
        float cautionValue;
        float maxView;

        // Apply numbers from your teammate's research
        switch (sensorName) {
            case "hydrogen_sulfide":
                cautionValue = 1f;  // TLV-TWA
                alarmValue = 5f;    // TLV-STEL
                maxView = 10f;
                break;

            case "dust": // PM2.5
                cautionValue = 102f; // Unhealthy average WYND techonologies
                alarmValue = 200f;   // Very Unhealthy middle
                maxView = 300f;
                break;

            case "ammonia":
            default:
                cautionValue = 25f; // TLV-TWA
                alarmValue = 35f;   // TLV-STEL
                maxView = 60f;
                break;
        }

        leftAxis.setAxisMaximum(maxView);
        leftAxis.setAxisMinimum(0f);

        // --- Red Alarm Line ---
        LimitLine alarmLimit = new LimitLine(alarmValue, "Alarm");
        alarmLimit.setLineColor(getColor(R.color.danger));
        alarmLimit.setTextColor(getColor(R.color.danger));
        alarmLimit.setLineWidth(2f);
        alarmLimit.enableDashedLine(20f, 10f, 0f);
        alarmLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        leftAxis.addLimitLine(alarmLimit);

        // --- Yellow Caution Line ---
        LimitLine cautionLimit = new LimitLine(cautionValue, "Caution");
        cautionLimit.setLineColor(getColor(R.color.warning));
        cautionLimit.setTextColor(getColor(R.color.warning));
        cautionLimit.setLineWidth(2f);
        cautionLimit.enableDashedLine(20f, 10f, 0f);
        cautionLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        leftAxis.addLimitLine(cautionLimit);

        lineChart.invalidate(); // Refresh view
    }
    private void selectButton(TextView button){
        button.setBackgroundResource(R.drawable.bg_btn_selected);
        button.setTextColor(getColor(R.color.safe));
    }
    private void unselectedButton(TextView button){
        button.setBackgroundResource(R.drawable.bg_btn_unselected);
        button.setTextColor(getColor(R.color.textMuted));
    }
    private void updateChart() {
        // Add this safety check
        if (lineChart == null || readings == null || readings.isEmpty()) {
            if (lineChart != null) {
                lineChart.clear();
                lineChart.invalidate();
            }
            return;
        }

        // Ensure we don't crash if the formatter fails
        try {
            lineChart.getXAxis().setValueFormatter(new TimeAxisValueFormatter(readings));
            }
        catch (Exception e) {
            android.util.Log.e("CHART_ERROR", "Error setting formatter: " + e.getMessage());
        }

        ArrayList<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < readings.size(); i++) {
            Reading currentReading = readings.get(i);
            // Fix: Add (float) cast to resolve "lossy conversion" error
            chartEntries.add(new Entry(i, (float) currentReading.getValue()));
        }

    LineDataSet lineDataSet = new LineDataSet(chartEntries, "Readings");

    // Styling the line (Green theme)
    lineDataSet.setColor(getColor(R.color.safe));
    lineDataSet.setLineWidth(3f);
    lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooths the line

    // Styling the points (Hollow ring effect)
    lineDataSet.setCircleColor(getColor(R.color.safe));
    lineDataSet.setCircleHoleColor(getColor(R.color.supreme)); // Matches background
    lineDataSet.setCircleRadius(6f);
    lineDataSet.setCircleHoleRadius(4f);
    lineDataSet.setDrawCircleHole(true);
    lineDataSet.setDrawValues(false); // Keeps the UI clean

    LineData lineData = new LineData(lineDataSet);
    lineChart.setData(lineData);

    lineChart.animateX(500); // 0.5s animation
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
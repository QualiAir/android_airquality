package com.concordia.qualiair;

import android.content.Intent;
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

import com.concordia.qualiair.Device.DeviceActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;

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
    private ApiService apiService;
    private final String DEVICE_ID = "sensor1";


    private float currentCaution = ThresholdLevels.NORMAL.nh3Caution;
    private float currentAlarm = ThresholdLevels.NORMAL.nh3Alarm;

    private TextView buttonFilterAlarm;
    private boolean isFilterActive = false;
    private List<Reading> filteredReadings = new ArrayList<>();

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

        // Buttons
        buttonNh3 = findViewById(R.id.button_nh3);
        buttonCo2 = findViewById(R.id.button_co2);
        buttonPm25 = findViewById(R.id.button_pm25);
        buttonFilterAlarm = findViewById(R.id.button_filter_alarm);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        yAxisTitleTextView = findViewById(R.id.y_axis_title);

        // Chart
        lineChart = findViewById(R.id.line_chart);
        setupChart("ammonia");

        // RecyclerView
        recyclerView = findViewById(R.id.recycler_readings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Spinner
        timeRangeSpinner = findViewById(R.id.time_range_spinner);
        setupTimeRangeSpinner();

        filteredReadings.addAll(readings);
        adapter = new ReadingAdapter(filteredReadings);
        recyclerView.setAdapter(adapter);

        // Sensor button listeners
        buttonNh3.setOnClickListener(v -> {
            updateButtonStates(buttonNh3);
            resetFilter();
            fetchDataFromServer("ammonia");
        });
        buttonCo2.setOnClickListener(v -> {
            updateButtonStates(buttonCo2);
            resetFilter();
            fetchDataFromServer("hydrogen_sulfide");
        });
        buttonPm25.setOnClickListener(v -> {
            updateButtonStates(buttonPm25);
            resetFilter();
            fetchDataFromServer("dust");
        });

        fetchDataFromServer("ammonia");
        updateButtonStates(buttonNh3);

        // Filter alarm button listener
        buttonFilterAlarm.setOnClickListener(v -> {
            isFilterActive = !isFilterActive;

            if (isFilterActive) {
                buttonFilterAlarm.setText("Show All");
                buttonFilterAlarm.setTextColor(getColor(R.color.danger));
                buttonFilterAlarm.setBackgroundResource(R.drawable.bg_btn_selected);
            } else {
                buttonFilterAlarm.setText("Show Alarms");
                buttonFilterAlarm.setTextColor(getColor(R.color.textMuted));
                buttonFilterAlarm.setBackgroundResource(R.drawable.bg_btn_unselected);
            }
            applyFilter();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_history); // highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_history) {
                return true; // already here
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(HistoryActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_devices) {
                startActivity(new Intent(HistoryActivity.this, DeviceActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HistoryActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_faq) {
                startActivity(new Intent(HistoryActivity.this, FAQActivity.class));
                return true;
            }
            return false;
        });
    }

    private float getSavedThreshold(String key, float defaultVal) {
        return getSharedPreferences("QualiAirPreferences", MODE_PRIVATE)
                .getFloat(key, defaultVal);
    }

    private void resetFilter() {
        isFilterActive = false;
        buttonFilterAlarm.setText("Show Alarms");
        buttonFilterAlarm.setTextColor(getColor(R.color.textMuted));
        buttonFilterAlarm.setBackgroundResource(R.drawable.bg_btn_unselected);
    }

    private void applyFilter() {
        TextView noAlarmsText = findViewById(R.id.text_no_alarms);
        List<Reading> toShow = new ArrayList<>();

        if (isFilterActive) {
            for (Reading r : readings) {
                if (r.getValue() >= currentAlarm) {
                    toShow.add(r);
                }
            }
            noAlarmsText.setVisibility(toShow.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            toShow.addAll(readings);
            noAlarmsText.setVisibility(View.GONE);
        }

        Collections.reverse(toShow); // newest first
        adapter.updateData(toShow);  // push fresh list directly to adapter
    }

    private String getStatus(float value, float caution, float alarm) {
        if (value >= alarm) return "High";
        if (value >= caution) return "Moderate";
        return "Low";
    }

    private void setupTimeRangeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.time_range_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeRangeSpinner.setAdapter(adapter);

        timeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String backendRangeKey;
                String selectedTimeRange = parent.getItemAtPosition(position).toString();
                if (selectedTimeRange.equals("Daily")) backendRangeKey = "24h";
                else if (selectedTimeRange.equals("Weekly")) backendRangeKey = "1w";
                else if (selectedTimeRange.equals("Monthly")) backendRangeKey = "1m";
                else backendRangeKey = "1h";

                String activeSensor = "ammonia";
                if (yAxisTitleTextView.getText().toString().contains("µg")) {
                    activeSensor = "dust";
                } else if (buttonCo2.getCurrentTextColor() == getColor(R.color.safe)) {
                    activeSensor = "hydrogen_sulfide";
                }

                fetchDataFromServer(activeSensor);

                android.widget.Toast.makeText(HistoryActivity.this,
                        "Range updated to: " + backendRangeKey,
                        android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void fetchDataFromServer(String sensorName) {
        // FIX 3a — update thresholds for the selected sensor
        switch (sensorName) {
            case "hydrogen_sulfide":
                currentCaution = getSavedThreshold(ThresholdLevels.KEY_H2S_CAUTION,  ThresholdLevels.NORMAL.h2sCaution);
                currentAlarm   = getSavedThreshold(ThresholdLevels.KEY_H2S_ALARM,    ThresholdLevels.NORMAL.h2sAlarm);
                yAxisTitleTextView.setText("H₂S (ppm)");
                break;
            case "dust":
                currentCaution = getSavedThreshold(ThresholdLevels.KEY_PM25_CAUTION, ThresholdLevels.NORMAL.pm25Caution);
                currentAlarm   = getSavedThreshold(ThresholdLevels.KEY_PM25_ALARM,   ThresholdLevels.NORMAL.pm25Alarm);
                yAxisTitleTextView.setText("PM2.5 (µg/m³)");
                break;
            case "ammonia":
            default:
                currentCaution = getSavedThreshold(ThresholdLevels.KEY_NH3_CAUTION,  ThresholdLevels.NORMAL.nh3Caution);
                currentAlarm   = getSavedThreshold(ThresholdLevels.KEY_NH3_ALARM,    ThresholdLevels.NORMAL.nh3Alarm);
                yAxisTitleTextView.setText("NH₃ (ppm)");
                break;
        }

        String selected = timeRangeSpinner.getSelectedItem().toString();
        String rangeKey = "1h";
        if (selected.equals("Daily")) rangeKey = "24h";
        else if (selected.equals("Weekly")) rangeKey = "1w";
        else if (selected.equals("Monthly")) rangeKey = "1m";

        setupChart(sensorName);

        // Capture final copies for use inside the callback lambda
        final float callbackCaution = currentCaution;
        final float callbackAlarm = currentAlarm;

        HistoryActivity.this.apiService.getHistory(rangeKey, sensorName, this.DEVICE_ID)
                .enqueue(new retrofit2.Callback<HistoryResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<HistoryResponse> call,
                                           retrofit2.Response<HistoryResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Reading> dataFromServer = response.body().getData();

                            if (dataFromServer != null) {
                                readings.clear();
                                readings.addAll(dataFromServer);

                                // recompute level locally using our thresholds,
                                // overriding whatever the server sent
                                for (Reading r : readings) {
                                    r.setLevel(getStatus(
                                            (float) r.getValue(),
                                            callbackCaution,
                                            callbackAlarm
                                    ));
                                }

                                updateChart();

                                // Build reversed list for table (newest first)
                                List<Reading> reversedForTable = new ArrayList<>(readings);
                                Collections.reverse(reversedForTable);
                                adapter.updateData(reversedForTable);
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<HistoryResponse> call, Throwable t) {
                        android.widget.Toast.makeText(HistoryActivity.this,
                                "Server waking up... try again",
                                android.widget.Toast.LENGTH_SHORT).show();
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

    private void setupChart(String sensorName) {
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        if (lineChart == null) return;

        lineChart.getAxisRight().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getColor(R.color.supreme));
        xAxis.setGranularity(1f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(getColor(R.color.supreme));
        leftAxis.removeAllLimitLines();

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setExtraOffsets(12f, 40f, 12f, 20f);

        leftAxis.removeAllLimitLines();

        float cautionValue = getSavedThreshold(
                sensorName.equals("hydrogen_sulfide") ? ThresholdLevels.KEY_H2S_CAUTION  :
                        sensorName.equals("dust")             ? ThresholdLevels.KEY_PM25_CAUTION : ThresholdLevels.KEY_NH3_CAUTION,
                sensorName.equals("hydrogen_sulfide") ? ThresholdLevels.NORMAL.h2sCaution  :
                        sensorName.equals("dust")             ? ThresholdLevels.NORMAL.pm25Caution : ThresholdLevels.NORMAL.nh3Caution
        );
        float alarmValue = getSavedThreshold(
                sensorName.equals("hydrogen_sulfide") ? ThresholdLevels.KEY_H2S_ALARM  :
                        sensorName.equals("dust")             ? ThresholdLevels.KEY_PM25_ALARM : ThresholdLevels.KEY_NH3_ALARM,
                sensorName.equals("hydrogen_sulfide") ? ThresholdLevels.NORMAL.h2sAlarm  :
                        sensorName.equals("dust")             ? ThresholdLevels.NORMAL.pm25Alarm : ThresholdLevels.NORMAL.nh3Alarm
        );
        float maxView = alarmValue * 1.5f;

        leftAxis.setAxisMaximum(maxView);
        leftAxis.setAxisMinimum(0f);

        LimitLine alarmLimit = new LimitLine(alarmValue, "Alarm");
        alarmLimit.setLineColor(getColor(R.color.danger));
        alarmLimit.setTextColor(getColor(R.color.danger));
        alarmLimit.setLineWidth(2f);
        alarmLimit.enableDashedLine(20f, 10f, 0f);
        alarmLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        leftAxis.addLimitLine(alarmLimit);

        LimitLine cautionLimit = new LimitLine(cautionValue, "Caution");
        cautionLimit.setLineColor(getColor(R.color.warning));
        cautionLimit.setTextColor(getColor(R.color.warning));
        cautionLimit.setLineWidth(2f);
        cautionLimit.enableDashedLine(20f, 10f, 0f);
        cautionLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        leftAxis.addLimitLine(cautionLimit);

        lineChart.invalidate();
    }

    private void selectButton(TextView button) {
        button.setBackgroundResource(R.drawable.bg_btn_selected);
        button.setTextColor(getColor(R.color.safe));
    }

    private void unselectedButton(TextView button) {
        button.setBackgroundResource(R.drawable.bg_btn_unselected);
        button.setTextColor(getColor(R.color.textMuted));
    }

    private void updateChart() {
        if (lineChart == null || readings == null || readings.isEmpty()) {
            if (lineChart != null) {
                lineChart.clear();
                lineChart.invalidate();
            }
            return;
        }

        try {
            lineChart.getXAxis().setValueFormatter(new TimeAxisValueFormatter(readings));
        } catch (Exception e) {
            android.util.Log.e("CHART_ERROR", "Error setting formatter: " + e.getMessage());
        }

        ArrayList<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < readings.size(); i++) {
            Reading currentReading = readings.get(i);
            chartEntries.add(new Entry(i, (float) currentReading.getValue()));
        }

        LineDataSet lineDataSet = new LineDataSet(chartEntries, "Readings");
        lineDataSet.setColor(getColor(R.color.safe));
        lineDataSet.setLineWidth(3f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCircleColor(getColor(R.color.safe));
        lineDataSet.setCircleHoleColor(getColor(R.color.supreme));
        lineDataSet.setCircleRadius(6f);
        lineDataSet.setCircleHoleRadius(4f);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawValues(false);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.animateX(500);
        lineChart.invalidate();
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
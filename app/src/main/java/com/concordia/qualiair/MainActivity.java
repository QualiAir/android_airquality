package com.concordia.qualiair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.SharedPreferences;

import com.concordia.qualiair.Device.DeviceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends AppCompatActivity {

    private GaugeView gaugeMain;
    private TextView tvGaugeValue;
    private TextView tvGaugeStatus;
    private TextView tvGaugeUnit;
    private MaterialButton btnNh3;
    private MaterialButton btnH2S;
    private MaterialButton btnPm25;

    private TextView tvPressureValue;
    private TextView tvHumidityValue;
    private TextView tvTempValue;

    private MqttClient mqttClient;

    private TextView tvGaugeLabel;

    private TextView tvLevelLow;
    private TextView tvLevelModerate;
    private TextView tvLevelHigh;

    private AlertManager alertManager;

    // --- Currently selected sensor ---
    private String selectedSensor = "nh3"; // default to NH3

    private AirQualityMonitor monitor = new AirQualityMonitor();

    SharedPreferences devicesSP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alertManager = new AlertManager(this);

        devicesSP = getSharedPreferences("QualiAirDevices", MODE_PRIVATE);

        if (devicesSP.getAll().isEmpty()) {
            Log.e("MainActivity", "No devices");
            // auto sends to device activity
            Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
            startActivity(intent);
            return;
        }
        Log.e("MainActivity", " still on create");

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }

        gaugeMain = findViewById(R.id.gauge_main);
        tvGaugeValue = findViewById(R.id.tv_gauge_value);
        tvGaugeStatus = findViewById(R.id.tv_gauge_status);
        tvGaugeUnit = findViewById(R.id.tv_gauge_unit);

        tvPressureValue = findViewById(R.id.tv_pressure_value);
        tvHumidityValue = findViewById(R.id.tv_humidity_value);
        tvTempValue = findViewById(R.id.tv_temp_value);


        //Sensor selector buttons
        btnNh3 = findViewById(R.id.btn_select_NH3);
        btnH2S = findViewById(R.id.btn_select_H2S);
        btnPm25 = findViewById(R.id.btn_select_PM25);

        tvGaugeLabel = findViewById(R.id.tv_gauge_label);

        //under the gauge
        tvLevelLow      = findViewById(R.id.tv_level_low);
        tvLevelModerate = findViewById(R.id.tv_level_moderate);
        tvLevelHigh     = findViewById(R.id.tv_level_high);

        //Button  set on click listeners
        btnNh3.setOnClickListener(v -> {
            selectedSensor = "nh3";
            tvGaugeLabel.setText("Ammonia Reading");
            updateSelectedButton(btnNh3);
            updateGaugeDisplay(monitor.getLatest("nh3"));
        });
        btnH2S.setOnClickListener(v -> {
            selectedSensor = "h2s";
            tvGaugeLabel.setText("Hydrogen Sulfide Reading");
            updateSelectedButton(btnH2S);
            updateGaugeDisplay(monitor.getLatest("h2s"));
        });
        btnPm25.setOnClickListener(v -> {
            selectedSensor = "pm25";
            tvGaugeLabel.setText("Dust Reading");
            updateSelectedButton(btnPm25);
            updateGaugeDisplay(monitor.getLatest("pm25"));
        });

        setupGaugeRanges();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("MainActivity", " on start");
    }



    private void setupGaugeRanges() {


        SharedPreferences savedPrefs = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE);
        String preset = savedPrefs.getString(ThresholdLevels.KEY_SENSITIVITY, "Normal");
        float nh3C  = savedPrefs.getFloat(ThresholdLevels.KEY_NH3_CAUTION,  ThresholdLevels.NORMAL.nh3Caution);
        float nh3A  = savedPrefs.getFloat(ThresholdLevels.KEY_NH3_ALARM,    ThresholdLevels.NORMAL.nh3Alarm);
        float h2sC  = savedPrefs.getFloat(ThresholdLevels.KEY_H2S_CAUTION,  ThresholdLevels.NORMAL.h2sCaution);
        float h2sA  = savedPrefs.getFloat(ThresholdLevels.KEY_H2S_ALARM,    ThresholdLevels.NORMAL.h2sAlarm);
        float pm25C = savedPrefs.getFloat(ThresholdLevels.KEY_PM25_CAUTION, ThresholdLevels.NORMAL.pm25Caution);
        float pm25A = savedPrefs.getFloat(ThresholdLevels.KEY_PM25_ALARM,   ThresholdLevels.NORMAL.pm25Alarm);
        monitor.updateThresholds(ThresholdLevels.fromPreference(preset, nh3C, nh3A, h2sC, h2sA, pm25C, pm25A));

        ThresholdLevels.Thresholds t = ThresholdLevels.fromPreference(preset, nh3C, nh3A, h2sC, h2sA, pm25C, pm25A);
        monitor.updateThresholds(t);
        gaugeMain.applyPreset(t, "NH3");
        updateSelectedButton(btnNh3);

        //opening app starts with 0.0 so it doesn't look broke and inconsisten
        updateGaugeDisplay(0f);
    }

    private void updateGaugeDisplay(float value) {
        SharedPreferences savedPrefs = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE);
        String preset = savedPrefs.getString(ThresholdLevels.KEY_SENSITIVITY, "Normal");
        ThresholdLevels.Thresholds t = ThresholdLevels.fromPreference(preset,
                savedPrefs.getFloat(ThresholdLevels.KEY_NH3_CAUTION,  ThresholdLevels.NORMAL.nh3Caution),
                savedPrefs.getFloat(ThresholdLevels.KEY_NH3_ALARM,    ThresholdLevels.NORMAL.nh3Alarm),
                savedPrefs.getFloat(ThresholdLevels.KEY_H2S_CAUTION,  ThresholdLevels.NORMAL.h2sCaution),
                savedPrefs.getFloat(ThresholdLevels.KEY_H2S_ALARM,    ThresholdLevels.NORMAL.h2sAlarm),
                savedPrefs.getFloat(ThresholdLevels.KEY_PM25_CAUTION, ThresholdLevels.NORMAL.pm25Caution),
                savedPrefs.getFloat(ThresholdLevels.KEY_PM25_ALARM,   ThresholdLevels.NORMAL.pm25Alarm)
        );

        switch (selectedSensor) {
            case "nh3":
                gaugeMain.applyPreset(t,"NH3");
                tvGaugeUnit.setText("ppm");
                tvGaugeValue.setText(String.format("%.1f", value));
                break;
            case "h2s":
                gaugeMain.applyPreset(t,"H2S");
                tvGaugeUnit.setText("ppm");
                tvGaugeValue.setText(String.format("%.1f", value));
                break;
            case "pm25":
                gaugeMain.applyPreset(t,"PM25");
                tvGaugeUnit.setText("µg/m³");
                tvGaugeValue.setText(String.format("%.4f", value));
                break;
        };
        applyStatusStyle(monitor.getStatus(selectedSensor));
        float clampedValue= Math.min(value,gaugeMain.getMaxValue());
        gaugeMain.setValue(clampedValue);

        updateLevelPills(selectedSensor);

    }

private void applyStatusStyle(AirQualityMonitor.StatusLevel status) {
    switch (status) {
        case ALARM:
            tvGaugeStatus.setText("Alarm");
            tvGaugeStatus.setTextColor(getColor(R.color.danger));
            tvGaugeStatus.setBackgroundResource(R.drawable.bg_pill_danger_dim);
            break;
        case CAUTION:
            tvGaugeStatus.setText("Caution");
            tvGaugeStatus.setTextColor(getColor(R.color.warning));
            tvGaugeStatus.setBackgroundResource(R.drawable.bg_pill_warning_dim);
            break;
        default:
            tvGaugeStatus.setText("Good");
            tvGaugeStatus.setTextColor(getColor(R.color.safe));
            tvGaugeStatus.setBackgroundResource(R.drawable.bg_pill_safe);
            break;
    }
}
    private void updateSelectedButton(MaterialButton active) {
        btnNh3.setStrokeColorResource(R.color.textMuted);
        btnH2S.setStrokeColorResource(R.color.textMuted);
        btnPm25.setStrokeColorResource(R.color.textMuted);

        btnNh3.setTextColor(getColor(R.color.textMuted));
        btnH2S.setTextColor(getColor(R.color.textMuted));
        btnPm25.setTextColor(getColor(R.color.textMuted));

        //default stroke
        btnNh3.setStrokeWidth(2);
        btnH2S.setStrokeWidth(2);
        btnPm25.setStrokeWidth(2);

        //for the selected button
        active.setStrokeColorResource(R.color.accent);
        active.setTextColor(getColor(R.color.accent));
        active.setStrokeWidth(6);
    }

    private void updateLevelPills(String sensor) {
        float caution = monitor.getCautionThreshold(sensor);
        float alarm   = monitor.getAlarmThreshold(sensor);
        String unit   = sensor.equals("pm25") ? "µg/m³" : "ppm";

        tvLevelLow.setText(String.format("< %.1f %s", caution, unit));
        tvLevelModerate.setText(String.format("%.1f–<%.1f %s", caution, alarm, unit));
        tvLevelHigh.setText(String.format("≥ %.1f %s", alarm, unit));
    }
    private void connectToHiveMQ() {
        String broker = BuildConfig.MQTT_BROKER;
        String clientId = "android-" + System.currentTimeMillis();

        try {
            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setUserName(BuildConfig.MQTT_USERNAME);
            options.setPassword(BuildConfig.MQTT_PASSWORD.toCharArray());


            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("MQTT", "Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    Log.d("MQTT", "Payload: " + payload);
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(payload);
                        float nh3  = (float) json.optDouble("ammonia");
                        float h2s  = (float) json.optDouble("hydrogen_sulfide");
                        float pm25 = (float) json.optDouble("dust");

                        float humidity=(float) json.optDouble("humidity");
                        float pressure=(float) json.optDouble("pressure",0.0);
                        float temp=(float) json.optDouble("temperature");


                        runOnUiThread(() -> {
                            tvPressureValue.setText(String.format("%.1f", pressure));
                            tvHumidityValue.setText(String.format("%.1f", humidity));
                            tvTempValue.setText(String.format("%.1f", temp));
                            monitor.update(nh3, h2s, pm25);
                            alertManager.onNewReading(monitor);
                            switch (selectedSensor) {
                                case "nh3":
                                    updateGaugeDisplay(monitor.getLatest("nh3"));
                                    break;
                                case "h2s":
                                    updateGaugeDisplay(monitor.getLatest("h2s"));
                                    break;
                                case "pm25":
                                    updateGaugeDisplay(monitor.getLatest("pm25"));
                                    break;
                            }
                        });
                    } catch (org.json.JSONException e) {
                        Log.e("MQTT", "Failed to parse payload: " + payload);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            new Thread(() -> {
                try {
                    mqttClient.connect(options);
                    Log.d("MQTT", "Connected to HiveMQ!");
                    mqttClient.subscribe("qualiair/data", 1);//qualiair/data for real reading,
                } catch (MqttException e) {
                    Log.e("MQTT", "Connection failed", e);
                }
            }).start();

        } catch (MqttException e) {
            Log.e("MQTT", "Client init failed", e);
        }
    }

        @Override
        protected void onResume () {
            super.onResume();

            SharedPreferences devicesSP = getSharedPreferences("QualiAirDevices", MODE_PRIVATE);
            if (devicesSP.getAll().isEmpty()) {
                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                startActivity(intent);
                return;
            }

            NavigationHelper.setupBottomNavigation(this, R.id.nav_home);
            setupGaugeRanges();
            connectToHiveMQ();
        }
        @Override
        protected void onPause () {
            super.onPause();
            if(mqttClient != null && mqttClient.isConnected()){
                try{
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    Log.e("MQTT", "Disconnect error", e);
                }
            }
        }

}

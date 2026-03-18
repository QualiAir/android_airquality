package com.concordia.qualiair;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Button;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private GaugeView gaugeMain;
    private TextView tvGaugeValue;
    private TextView tvGaugeStatus;
    private TextView tvGaugeUnit;
    private Button btnNh3;
    private Button btnH2S;
    private Button btnPm25;

    private MqttClient mqttClient;

    //initialize latest value from InfluxDB
    private float latestNH3 = 0f;
    private float latestH2S = 0f;
    private float latestPM25 = 0f;

    // --- Currently selected sensor ---
    private String selectedSensor = "nh3"; // default to NH3

    // Thresholds
    // NH3 (ppm)
    private static final float NH3_CAUTION = 25f;
    private static final float NH3_ALARM = 35f;
    // H2S (ppm)
    private static final float H2S_CAUTION = 1f;
    private static final float H2S_ALARM = 5f;
    // PM2.5 (µg/m³)
    private static final float PM25_CAUTION = 102f;
    private static final float PM25_ALARM = 200f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 1. Force the Home icon to be highlighted when this activity starts
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // We are already on Home. Just return true to keep the highlight.
                return true;
            } else if (itemId == R.id.nav_faq) {
                startActivity(new Intent(MainActivity.this, FAQActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_devices) {
                startActivity(new Intent(MainActivity.this, DeviceActivity.class));
                return true;
            }
            return false;
        });


        gaugeMain = findViewById(R.id.gauge_main);
        tvGaugeValue = findViewById(R.id.tv_gauge_value);
        tvGaugeStatus = findViewById(R.id.tv_gauge_status);
        tvGaugeUnit = findViewById(R.id.tv_gauge_unit);

        //Sensor selector buttons
        btnNh3 = findViewById(R.id.btn_select_NH3);
        btnH2S = findViewById(R.id.btn_select_H2S);
        btnPm25 = findViewById(R.id.btn_select_PM25);

        //Button  set on click listeners
        btnNh3.setOnClickListener(v -> {
            selectedSensor = "nh3";
            updateGaugeDisplay(latestNH3);
        });
        btnH2S.setOnClickListener(v -> {
            selectedSensor = "h2s";
            updateGaugeDisplay(latestH2S);
        });
        btnPm25.setOnClickListener(v -> {
            selectedSensor = "pm25";
            updateGaugeDisplay(latestPM25);
        });

        //Setting up gauge range for default sensor (NH3)
        setupGaugeRanges();
    }

    private void setupGaugeRanges() {
        // create UserPreferences
        UserPreferences prefs = new UserPreferences(this);
        prefs.loadAllPreferences();

        int nh3Max = prefs.getNh3HighMax();
        gaugeMain.setMinValue(0);
        if (nh3Max == 0) {
            gaugeMain.setMaxValue(50);
        } else {
            gaugeMain.setMaxValue(nh3Max);
        }
        gaugeMain.setValue(0);
    }

    private void updateGaugeDisplay(float value) {
        //update the gauge range and unit based on the chosen sensor
        float maxValue=250f;
        switch (selectedSensor) {
            case "nh3":
                gaugeMain.setMinValue(0);
                gaugeMain.setMaxValue(35);
                tvGaugeUnit.setText("ppm");
                updateStatus(value, NH3_CAUTION, NH3_ALARM);
                maxValue=35f;
                break;
            case "h2s":
                gaugeMain.setMinValue(0);
                gaugeMain.setMaxValue(10);
                tvGaugeUnit.setText("ppm");
                updateStatus(value, H2S_CAUTION, H2S_ALARM);
                maxValue=10f;
                break;
            case "pm25":
                gaugeMain.setMinValue(0);
                gaugeMain.setMaxValue(250);
                tvGaugeUnit.setText("µg/m³");
                updateStatus(value, PM25_CAUTION, PM25_ALARM);
                maxValue=250;
                break;
        }
        float clampedValue= Math.min(value,maxValue);//so if the value is beyond, it will stay in the red zone far right
        gaugeMain.setValue(clampedValue);
        tvGaugeValue.setText(String.format("%.1f", value));

    }

    private void updateStatus(float value, float caution, float alarm) {
        if (value >= alarm) {
            tvGaugeStatus.setText("High");
            tvGaugeStatus.setTextColor(getColor(R.color.danger));
            tvGaugeStatus.setBackgroundResource(R.drawable.bg_pill_danger_dim);
        } else if (value >= caution) {
            tvGaugeStatus.setText("Moderate");
            tvGaugeStatus.setTextColor(getColor(R.color.warning));
            tvGaugeStatus.setBackgroundResource(R.drawable.bg_pill_warning_dim);
        } else {
            tvGaugeStatus.setText("Good");
            tvGaugeStatus.setTextColor(getColor(R.color.safe));
            tvGaugeStatus.setBackgroundResource(R.drawable.bg_pill_safe);
        }
    }
    private void connectToHiveMQ() {
        String broker = BuildConfig.MQTT_BROKER;
        String clientId = "android-" + System.currentTimeMillis();

        try {
            mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

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
                        float nh3  = (float) json.getDouble("ammonia");
                        float h2s  = (float) json.getDouble("hydrogen_sulfide");
                        float pm25 = (float) json.getDouble("dust");

                        runOnUiThread(() -> {
                            latestNH3  = nh3;
                            latestH2S  = h2s;
                            latestPM25 = pm25;
                            switch (selectedSensor) {
                                case "nh3":  updateGaugeDisplay(latestNH3);  break;
                                case "h2s":  updateGaugeDisplay(latestH2S);  break;
                                case "pm25": updateGaugeDisplay(latestPM25); break;
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
                    mqttClient.subscribe("qualiair/test", 1);
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
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
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
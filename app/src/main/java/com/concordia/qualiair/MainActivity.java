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

    private TextView tvPressureValue;
    private TextView tvHumidityValue;
    private TextView tvTempValue;

    private MqttClient mqttClient;

    // --- Currently selected sensor ---
    private String selectedSensor = "nh3"; // default to NH3

    private AirQualityMonitor monitor = new AirQualityMonitor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        tvPressureValue = findViewById(R.id.tv_pressure_value);
        tvHumidityValue = findViewById(R.id.tv_humidity_value);
        tvTempValue = findViewById(R.id.tv_temp_value);


        //Sensor selector buttons
        btnNh3 = findViewById(R.id.btn_select_NH3);
        btnH2S = findViewById(R.id.btn_select_H2S);
        btnPm25 = findViewById(R.id.btn_select_PM25);

        //Button  set on click listeners
        btnNh3.setOnClickListener(v -> {
            selectedSensor = "nh3";
            updateGaugeDisplay(monitor.getLatest("nh3"));
        });
        btnH2S.setOnClickListener(v -> {
            selectedSensor = "h2s";
            updateGaugeDisplay(monitor.getLatest("h2s"));
        });
        btnPm25.setOnClickListener(v -> {
            selectedSensor = "pm25";
            updateGaugeDisplay(monitor.getLatest("pm25"));
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
                maxValue=35f;
                break;
            case "h2s":
                gaugeMain.setMinValue(0);
                gaugeMain.setMaxValue(10);
                tvGaugeUnit.setText("ppm");
                maxValue=10f;
                break;
            case "pm25":
                gaugeMain.setMinValue(0);
                gaugeMain.setMaxValue(250);
                tvGaugeUnit.setText("µg/m³");
                maxValue=250;
                break;
        }
        applyStatusStyle(monitor.getStatus(selectedSensor));
        float clampedValue= Math.min(value,maxValue);//so if the value is beyond, it will stay in the red zone far right
        gaugeMain.setValue(clampedValue);
        tvGaugeValue.setText(String.format("%.1f", value));

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

                        float humidity=(float) json.getDouble("humidity");
                        float pressure=(float) json.getDouble("pressure");
                        float temp=(float) json.getDouble("temperature");


                        runOnUiThread(() -> {
                            tvPressureValue.setText(String.format("%.0f", pressure));
                            tvHumidityValue.setText(String.format("%.0f", humidity));
                            tvTempValue.setText(String.format("%.0f", temp));
                            monitor.update(nh3, h2s, pm25);
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
                    mqttClient.subscribe("qualiair/gauge_test", 1);//qualiair/gauge_test
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
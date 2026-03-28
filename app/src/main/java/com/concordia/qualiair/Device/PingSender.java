package com.concordia.qualiair.Device;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
* Sends pings to devices in device activity to confirm that they are online
* */


    public class PingSender {

        private static final String TAG = "DeviceInfoPoller";
        private static final int POLL_INTERVAL_MS = 30000; // every 30 seconds

        public interface DeviceInfoCallback {
            void onReceived(JSONObject deviceInfo);
            void onError(String reason);
        }

        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean isRunning = false;
        private  String deviceIp;
        private  DeviceInfoCallback callback;

        public PingSender(String deviceIp, DeviceInfoCallback callback) {
            this.deviceIp = deviceIp;
            this.callback = callback;
        }

        public void start() {
            isRunning = true;
            poll();
        }

        public void stop() {
            isRunning = false;
            handler.removeCallbacksAndMessages(null);
        }

        private void poll() {
            if (!isRunning) return;

            // Run network call on background thread
            new Thread(() -> {
                try {
                    URL url = new URL("http://" + deviceIp + "/device-info");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject json = new JSONObject(response.toString());
                        handler.post(() -> callback.onReceived(json));
                    } else {
                        handler.post(() -> callback.onError("HTTP error: " + responseCode));
                    }
                    connection.disconnect();

                } catch (Exception e) {
                    Log.e(TAG, "Poll failed: " + e.getMessage());
                    handler.post(() -> callback.onError(e.getMessage()));
                }

                // Schedule next poll
                if (isRunning) {
                    handler.postDelayed(this::poll, POLL_INTERVAL_MS);
                }
            }).start();
        }
    }

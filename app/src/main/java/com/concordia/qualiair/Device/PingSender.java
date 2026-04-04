package com.concordia.qualiair.Device;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/*
* Sends pings to devices in device activity to confirm that they are online
* Still havent implemented using ip address.
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

            new Thread(() -> {
                try {
                    //only works if phone and esp32 are in the same LAN(same home wifi as phone, same hotspot as phone)
                    InetAddress address = InetAddress.getByName(deviceIp);
                    boolean reachable = address.isReachable(5000); // 5 second timeout

                    if (reachable) {
                        handler.post(() -> callback.onReceived(null)); // device is online
                    } else {
                        handler.post(() -> callback.onError("Device unreachable"));
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Ping failed: " + e.getMessage());
                    handler.post(() -> callback.onError(e.getMessage()));

                } finally {
                    if (isRunning) {
                        handler.postDelayed(this::poll, POLL_INTERVAL_MS);
                    }
                }
            }).start();

            
        }
        
    }

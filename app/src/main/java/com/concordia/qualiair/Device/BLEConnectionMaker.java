package com.concordia.qualiair.Device;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.ResponseListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class BLEConnectionMaker {

    private static final String TAG = "BLEConnectionMaker";
    private static final String PROV_SERVICE_UUID = "1775244d-6b43-439b-877c-060f2d9bed07";

    public interface ProvisionCallback {
        void onSuccess(String ipAddress, String deviceId);
        void onFailure(String reason);
    }

    private final Context context;
    private ESPProvisionManager provisionManager;
    private ESPDevice espDevice;
    private ProvisionCallback pendingCallback;

    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private BluetoothDevice currentDevice;
    private String pendingSsid;
    private String pendingPassword;

    public BLEConnectionMaker(Context context) {
        this.context = context;
        provisionManager = ESPProvisionManager.getInstance(context);
        // Register EventBus so we receive connection events
        EventBus.getDefault().register(this);
    }

    public void sendCredentials(BluetoothDevice bleDevice,
                                String ssid,
                                String password,
                                ProvisionCallback callback) {
        this.currentDevice = bleDevice;
        this.pendingSsid = ssid;
        this.pendingPassword = password;
        this.pendingCallback = callback;
        this.retryCount = 0;

        connectToDevice();
    }

    private void connectToDevice() {
        Log.d(TAG, "Connecting, attempt " + (retryCount + 1) + "/" + MAX_RETRIES);

        espDevice = provisionManager.createESPDevice(
                ESPConstants.TransportType.TRANSPORT_BLE,
                ESPConstants.SecurityType.SECURITY_1
        );

        espDevice.setProofOfPossession("TEAM1abcd1234"); // match your ESP32 PoP

        espDevice.connectBLEDevice(currentDevice, PROV_SERVICE_UUID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectionEvent(DeviceConnectionEvent event) {
        Log.d(TAG, "Connection event: " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_CONNECTED:
                retryCount = 0;
                Log.d(TAG, "Connected, sending credentials...");
                provision();
                break;

            case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.d(TAG, "Connection failed, retrying " + retryCount + "/" + MAX_RETRIES);
                    new android.os.Handler(context.getMainLooper())
                            .postDelayed(this::connectToDevice, 2000);
                } else {
                    retryCount = 0;
                    if (pendingCallback != null) {
                        pendingCallback.onFailure("Could not connect after " + MAX_RETRIES + " attempts");
                    }
                }
                break;

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                Log.d(TAG, "Device disconnected");
                break;
        }
    }

    private void fetchDeviceInfoWithRetry(int retriesLeft) {
        espDevice.sendDataToCustomEndPoint(
                "device_info",
                new byte[0],
                new ResponseListener() {
                    @Override
                    public void onSuccess(byte[] returnData) {
                        try {
                            String responseStr = new String(returnData, StandardCharsets.UTF_8).trim();
                            Log.i(TAG, "Got device info: " + responseStr);

                            JSONObject json = new JSONObject(responseStr);
                            String ip = json.optString("ip", null);
                            String deviceId = json.optString("device_id", null);

                            // If IP is not ready yet, retry
                            if (ip == null || ip.equals("0.0.0.0")) {
                                Log.w(TAG, "IP not ready yet, retrying... (" + retriesLeft + " left)");
                                if (retriesLeft > 0) {
                                    new android.os.Handler(android.os.Looper.getMainLooper())
                                            .postDelayed(() -> fetchDeviceInfoWithRetry(retriesLeft - 1), 2000);
                                } else {
                                    if (pendingCallback != null)
                                        pendingCallback.onFailure("ESP32 never got a valid IP after retries");
                                }
                                return;
                            }

                            Log.i(TAG, "IP: " + ip + " | Device ID: " + deviceId);

                            // sends "OK" to ESP32 to complete the handshake
                            espDevice.sendDataToCustomEndPoint(
                                    "device_info",
                                    "OK".getBytes(StandardCharsets.UTF_8),
                                    new ResponseListener() {
                                        @Override
                                        public void onSuccess(byte[] ackData) {
                                            Log.i(TAG, "ESP32 acknowledged provisioning");
                                            if (pendingCallback != null)
                                            {
                                                pendingCallback.onSuccess(ip,deviceId);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            // ACK failed = ESP32 didn't complete handshake, treat as real failure
                                            Log.e(TAG, "Failed to send OK to ESP32: " + e.getMessage());
                                            if (pendingCallback != null)
                                                pendingCallback.onFailure("Handshake ACK failed: " + e.getMessage());
                                        }
                                    }
                            );

                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse device info JSON: " + e.getMessage());
                            if (pendingCallback != null)
                                pendingCallback.onFailure("Invalid response from device: " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to reach device_info endpoint: " + e.getMessage());
                        if (pendingCallback != null)
                            pendingCallback.onFailure("Could not reach device: " + e.getMessage());
                    }
                }
        );
    }

    private void provision() {
        espDevice.provision(pendingSsid, pendingPassword, new ProvisionListener() {

            @Override
            public void createSessionFailed(Exception e) {
                Log.e(TAG, "createSessionFailed: " + e.getMessage());
                if (pendingCallback != null)
                    pendingCallback.onFailure("Session failed: " + e.getMessage());
            }

            @Override
            public void wifiConfigSent() {
                Log.i(TAG, "wifiConfigSent");
            }

            @Override
            public void wifiConfigFailed(Exception e) {
                Log.e(TAG, "wifiConfigFailed: " + e.getMessage());
                if (pendingCallback != null)
                    pendingCallback.onFailure("Config failed: " + e.getMessage());
            }

            @Override
            public void wifiConfigApplied() {
                Log.i(TAG, "wifiConfigApplied");
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {
                Log.e(TAG, "wifiConfigApplyFailed: " + e.getMessage());
                if (pendingCallback != null)
                    pendingCallback.onFailure("Apply failed: " + e.getMessage());
            }

            @Override
            public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason reason) {
                Log.e(TAG, "provisioningFailedFromDevice: " + reason);
                if (pendingCallback != null)
                    pendingCallback.onFailure("Device rejected: " + reason);
            }

            @Override
            public void deviceProvisioningSuccess() {
                Log.i(TAG, "Provisioning success, fetching device info...");
                // Start with 5 retries, 2 seconds apart = up to 10 seconds to get IP
                fetchDeviceInfoWithRetry(5);
            }

            @Override
            public void onProvisioningFailed(Exception e) {
                Log.e(TAG, "onProvisioningFailed: " + e.getMessage());
                if (pendingCallback != null)
                    pendingCallback.onFailure("Provisioning error: " + e.getMessage());
            }
        });
    }

//    private void provision() {
//        espDevice.provision(pendingSsid, pendingPassword, new ProvisionListener() {
//
//            @Override
//            public void createSessionFailed(Exception e) {
//                Log.e(TAG, "createSessionFailed: " + e.getMessage());
//                if (pendingCallback != null)
//                    pendingCallback.onFailure("Session failed: " + e.getMessage());
//            }
//
//            @Override
//            public void wifiConfigSent() {
//                Log.i(TAG, "wifiConfigSent");
//            }
//
//            @Override
//            public void wifiConfigFailed(Exception e) {
//                Log.e(TAG, "wifiConfigFailed: " + e.getMessage());
//                if (pendingCallback != null)
//                    pendingCallback.onFailure("Config failed: " + e.getMessage());
//            }
//
//            @Override
//            public void wifiConfigApplied() {
//                Log.i(TAG, "wifiConfigApplied");
//            }
//
//            @Override
//            public void wifiConfigApplyFailed(Exception e) {
//                Log.e(TAG, "wifiConfigApplyFailed: " + e.getMessage());
//                if (pendingCallback != null)
//                    pendingCallback.onFailure("Apply failed: " + e.getMessage());
//            }
//
//            @Override
//            public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason reason) {
//                Log.e(TAG, "provisioningFailedFromDevice: " + reason);
//                if (pendingCallback != null)
//                    pendingCallback.onFailure("Device rejected: " + reason);
//            }
//
//            @Override
//            public void deviceProvisioningSuccess() {
//                Log.i(TAG, "Provisioning success, fetching device info package...");
//
//                // Wait 2 seconds for ESP32 to get its IP from router
//                new android.os.Handler(android.os.Looper.getMainLooper())
//                        .postDelayed(() -> {
//                            espDevice.sendDataToCustomEndPoint(
//                                    "device_info",
//                                    new byte[0], // no input needed
//                                    new ResponseListener() {
//                                        @Override
//                                        public void onSuccess(byte[] returnData) {
//                                            try {
//                                                String responseStr = new String(returnData).trim();
//                                                Log.i(TAG, "Got device info: " + responseStr);
//
//                                                org.json.JSONObject json = new org.json.JSONObject(responseStr);
//                                                String ip = json.optString("ip", null);
//                                                String deviceId = json.optString("device_id", null);
//
//                                                Log.i(TAG, "IP: " + ip + " Device ID: " + deviceId);
//
//                                                // Send "OK" back to ESP32 to confirm provisioning
//                                                espDevice.sendDataToCustomEndPoint(
//                                                        "device_info",
//                                                        "OK".getBytes(),
//                                                        new ResponseListener() {
//                                                            @Override
//                                                            public void onSuccess(byte[] ackData) {
//                                                                Log.i(TAG, "ESP32 acknowledged provisioning");
//                                                                if (pendingCallback != null)
//                                                                    pendingCallback.onSuccess(ip);
//                                                            }
//
//                                                            @Override
//                                                            public void onFailure(Exception e) {
//                                                                Log.e(TAG, "Failed to send OK: " + e.getMessage());
//                                                                // Still call success since we got the IP
//                                                                if (pendingCallback != null)
//                                                                    pendingCallback.onSuccess(ip);
//                                                            }
//                                                        }
//                                                );
//
//                                            } catch (org.json.JSONException e) {
//                                                Log.e(TAG, "Failed to parse JSON: " + e.getMessage());
//                                                if (pendingCallback != null)
//                                                    pendingCallback.onSuccess(null);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onFailure(Exception e) {
//                                            Log.e(TAG, "Failed to get IP: " + e.getMessage());
//                                            // Still call success, just without IP
//                                            if (pendingCallback != null)
//                                                pendingCallback.onSuccess(null);
//                                        }
//                                    }
//                            );
//                        }, 2000); // 2 second delay
//            }
//
//            @Override
//            public void onProvisioningFailed(Exception e) {
//                Log.e(TAG, "onProvisioningFailed: " + e.getMessage());
//                if (pendingCallback != null)
//                    pendingCallback.onFailure("Provisioning error: " + e.getMessage());
//            }
//        });
//    }

    public void disconnect() {
        if (espDevice != null) {
            espDevice.disconnectDevice();
        }
        // Unregister EventBus when done to avoid memory leaks
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}

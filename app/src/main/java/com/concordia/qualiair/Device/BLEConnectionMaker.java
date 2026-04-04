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
    private boolean infoFetchStarted = false;

    public BLEConnectionMaker(Context context) {
        this.context = context;
        provisionManager = ESPProvisionManager.getInstance(context);
        // Register EventBus so we receive connection events
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
        this.infoFetchStarted = false;

        connectToDevice();
    }

    private void connectToDevice() {
        Log.d(TAG, "Connecting to " + (currentDevice != null ? currentDevice.getName() : "unknown") + ", attempt " + (retryCount + 1) + "/" + MAX_RETRIES);

        espDevice = provisionManager.createESPDevice(
                ESPConstants.TransportType.TRANSPORT_BLE,
                ESPConstants.SecurityType.SECURITY_1
        );

        espDevice.setProofOfPossession("TEAM1abcd1234"); // match your ESP32 PoP

        espDevice.connectBLEDevice(currentDevice, PROV_SERVICE_UUID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectionEvent(DeviceConnectionEvent event) {
        // Safety check: ignore events if this instance is not actively managing a device
        if (espDevice == null && event.getEventType() != ESPConstants.EVENT_DEVICE_CONNECTED) {
            return;
        }

        Log.d(TAG, "Connection event received: " + event.getEventType());

        switch (event.getEventType()) {

            case ESPConstants.EVENT_DEVICE_CONNECTED:
                retryCount = 0;
                Log.d(TAG, "Connected successfully, initiating provisioning...");
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
                Log.d(TAG, "Device disconnected (Event 3)");
                // Clear the device reference so we don't try to use a dead connection
                espDevice = null;
                break;
        }
    }

    private void fetchDeviceInfoWithRetry(int retriesLeft) {
        if (espDevice == null) {
            Log.w(TAG, "fetchDeviceInfoWithRetry: Device is disconnected, stopping.");
            return;
        }

        Log.d(TAG, "Requesting device_info... (Retries remaining: " + retriesLeft + ")");

        espDevice.sendDataToCustomEndPoint(
                "device_info",
                "REQUEST_INFO".getBytes(StandardCharsets.UTF_8),
                new ResponseListener() {
                    @Override
                    public void onSuccess(byte[] returnData) {
                        try {
                            if (returnData == null || returnData.length == 0) {
                                Log.w(TAG, "Received empty response from device_info");
                                retryFetch();
                                return;
                            }

                            String responseStr = new String(returnData, StandardCharsets.UTF_8).trim();
                            Log.i(TAG, "Device info response: " + responseStr);

                            JSONObject json = new JSONObject(responseStr);
                            String ip = json.optString("ip", "");
                            String deviceId = json.optString("device_id", "");

                            // If IP is not ready yet, retry
                            if (ip.isEmpty() || ip.equals("0.0.0.0")) {
                                Log.w(TAG, "IP not ready yet (0.0.0.0), retrying...");
                                retryFetch();
                                return;
                            }

                            Log.i(TAG, "Success! IP: " + ip + " | ID: " + deviceId);

                            // Sends "OK" to ESP32 to finalize the handshake
                            if (espDevice != null) {
                                espDevice.sendDataToCustomEndPoint(
                                        "device_info",
                                        "OK".getBytes(StandardCharsets.UTF_8),
                                        new ResponseListener() {
                                            @Override
                                            public void onSuccess(byte[] ackData) {
                                                Log.i(TAG, "ESP32 acknowledged provisioning finalization");
                                                if (pendingCallback != null) {
                                                    pendingCallback.onSuccess(ip, deviceId);
                                                }
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e(TAG, "Failed to send final OK ACK: " + e.getMessage());
                                                if (pendingCallback != null) {
                                                    pendingCallback.onSuccess(ip, deviceId);
                                                }
                                            }
                                        }
                                );
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            retryFetch();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Endpoint unreachable: " + e.getMessage());
                        retryFetch();
                    }

                    private void retryFetch() {
                        if (retriesLeft > 0 && espDevice != null) {
                            new android.os.Handler(android.os.Looper.getMainLooper())
                                    .postDelayed(() -> fetchDeviceInfoWithRetry(retriesLeft - 1), 2000);
                        } else {
                            if (pendingCallback != null) {
                                pendingCallback.onFailure("Failed to get device IP/ID. Please check if device joined WiFi.");
                            }
                        }
                    }
                }
        );
    }

    private void provision() {
        if (espDevice == null) return;

        Log.i(TAG, "Sending WiFi credentials...");

        espDevice.provision(pendingSsid, pendingPassword, new ProvisionListener() {

            @Override
            public void createSessionFailed(Exception e) {
                Log.e(TAG, "Session failed: " + e.getMessage());
                if (pendingCallback != null) pendingCallback.onFailure("Session failed: " + e.getMessage());
            }

            @Override
            public void wifiConfigSent() {
                Log.i(TAG, "WiFi config sent.");
            }

            @Override
            public void wifiConfigFailed(Exception e) {
                Log.e(TAG, "WiFi config failed: " + e.getMessage());
                if (pendingCallback != null) pendingCallback.onFailure("Config failed: " + e.getMessage());
            }

            @Override
            public void wifiConfigApplied() {
                Log.i(TAG, "WiFi config applied. Waiting for device to connect to network...");
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {
                Log.e(TAG, "Apply failed: " + e.getMessage());
                if (pendingCallback != null) pendingCallback.onFailure("Apply failed: " + e.getMessage());
            }

            @Override
            public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason reason) {
                Log.e(TAG, "Device error: " + reason);
                if (pendingCallback != null) pendingCallback.onFailure("Device error: " + reason);
            }

            @Override
            public void deviceProvisioningSuccess() {
                Log.i(TAG, "Provisioning Success! SDK confirmed WiFi connection.");
                if (!infoFetchStarted) {
                    infoFetchStarted = true;
                    // Wait 1 second before poking to allow the ESP32 to settle
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(() -> fetchDeviceInfoWithRetry(10), 1000);
                }
            }

            @Override
            public void onProvisioningFailed(Exception e) {
                Log.e(TAG, "Provisioning failed: " + e.getMessage());
                // If the device reboots, it disconnects BLE, which triggers this failure.
                // We only report it as a hard failure if we haven't already started the IP fetch.
                if (!infoFetchStarted && pendingCallback != null) {
                    pendingCallback.onFailure("Provisioning failed: " + e.getMessage());
                }
            }
        });
    }

    public void disconnect() {
        if (espDevice != null) {
            espDevice.disconnectDevice();
            espDevice = null;
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        pendingCallback = null;
    }
}
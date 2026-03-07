package com.concordia.qualiair;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.espressif.provisioning.DeviceConnectionEvent;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.google.android.material.appbar.MaterialToolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProvisioningActivity extends AppCompatActivity {

    private ESPProvisionManager provisionManager;
    private ESPDevice espDevice;
    private TextView tvStatus;
    private static final String TAG = "ProvisioningActivity";
    private static final String PROV_SERVICE_UUID = "1775244d-6b43-439b-877c-060f2d9bed07";

    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private BluetoothDevice currentDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_provisioning);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        tvStatus = findViewById(R.id.tvStatus);
        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());

        // Disable button until connected
        findViewById(R.id.btnProvision).setEnabled(false);

        currentDevice = getIntent().getParcelableExtra("device");

        connectToDevice(currentDevice);

        findViewById(R.id.btnProvision).setOnClickListener(v -> {
            String ssid = ((EditText) findViewById(R.id.etSSID))
                    .getText().toString().trim();
            String password = ((EditText) findViewById(R.id.etPassword))
                    .getText().toString().trim();

            if (ssid.isEmpty()) {
                Toast.makeText(this, "Please enter SSID", Toast.LENGTH_SHORT).show();
                return;
            }

            sendCredentials(ssid, password);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void connectToDevice(BluetoothDevice bluetoothDevice) {
        tvStatus.setText("Status: Connecting... (attempt " + (retryCount + 1) + "/" + MAX_RETRIES + ")");

        // Always recreate espDevice fresh on each attempt
        espDevice = provisionManager.createESPDevice(
                ESPConstants.TransportType.TRANSPORT_BLE,
                ESPConstants.SecurityType.SECURITY_1
        );

        // Set PoP if your ESP32 uses one — change or remove if not needed
        espDevice.setProofOfPossession("TEAM1abcd1234");

        espDevice.connectBLEDevice(bluetoothDevice, PROV_SERVICE_UUID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)//DO NOT COMMENT IT IS USED
    public void onDeviceConnectionEvent(DeviceConnectionEvent event) {
        Log.d(TAG, "Connection event: " + event.getEventType());

        switch (event.getEventType()) {
            case ESPConstants.EVENT_DEVICE_CONNECTED:
                retryCount = 0;
                tvStatus.setText("Status: Connected ✓ — enter credentials");
                findViewById(R.id.btnProvision).setEnabled(true);
                break;

            case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    tvStatus.setText("Status: Failed, retrying... ("
                            + retryCount + "/" + MAX_RETRIES + ")");
                    new android.os.Handler(getMainLooper()).postDelayed(
                            () -> connectToDevice(currentDevice), 2000);
                } else {
                    retryCount = 0;
                    tvStatus.setText("Status: Could not connect after "
                            + MAX_RETRIES + " attempts.\nCheck UUID and PoP.");
                    Toast.makeText(this,
                            "Connection failed — check serial monitor for UUID",
                            Toast.LENGTH_LONG).show();
                }
                break;

            case ESPConstants.EVENT_DEVICE_DISCONNECTED:
                tvStatus.setText("Status: Disconnected");
                findViewById(R.id.btnProvision).setEnabled(false);
                break;
        }
    }

    private void sendCredentials(String ssid, String password) {
        tvStatus.setText("Status: Sending credentials...");
        findViewById(R.id.btnProvision).setEnabled(false);

        espDevice.provision(ssid, password, new ProvisionListener() {

            @Override
            public void createSessionFailed(Exception e) {
                Log.e(TAG, "createSessionFailed: " + e.getMessage());
                runOnUiThread(() -> {
                    tvStatus.setText("Status: Session failed ✗");
                    findViewById(R.id.btnProvision).setEnabled(true);
                });
            }

            @Override
            public void wifiConfigSent() {
                Log.i(TAG, "wifiConfigSent");
                runOnUiThread(() -> tvStatus.setText("Status: Config sent ✓"));
            }

            @Override
            public void wifiConfigFailed(Exception e) {
                Log.e(TAG, "wifiConfigFailed: " + e.getMessage());
                runOnUiThread(() -> {
                    tvStatus.setText("Status: Config failed ✗");
                    findViewById(R.id.btnProvision).setEnabled(true);
                });
            }

            @Override
            public void wifiConfigApplied() {
                Log.i(TAG, "wifiConfigApplied");
                runOnUiThread(() -> tvStatus.setText("Status: Config applied ✓"));
            }

            @Override
            public void wifiConfigApplyFailed(Exception e) {
                Log.e(TAG, "wifiConfigApplyFailed: " + e.getMessage());
                runOnUiThread(() -> {
                    tvStatus.setText("Status: Apply failed ✗");
                    findViewById(R.id.btnProvision).setEnabled(true);
                });
            }

            @Override
            public void provisioningFailedFromDevice(
                    ESPConstants.ProvisionFailureReason reason) {
                Log.e(TAG, "provisioningFailedFromDevice: " + reason.toString());
                runOnUiThread(() -> {
                    tvStatus.setText("Status: Failed - " + reason);
                    findViewById(R.id.btnProvision).setEnabled(true);
                });
            }

            @Override
            public void deviceProvisioningSuccess() {
                Log.i(TAG, "deviceProvisioningSuccess");
                runOnUiThread(() -> {
                    tvStatus.setText("Status: Success! ✓");
                    Toast.makeText(ProvisioningActivity.this,
                            "ESP32 provisioned successfully!", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onProvisioningFailed(Exception e) {
                Log.e(TAG, "onProvisioningFailed: " + e.getMessage());
                runOnUiThread(() -> {
                    tvStatus.setText("Status: Error - " + e.getMessage());
                    findViewById(R.id.btnProvision).setEnabled(true);
                });
            }
        });
    }
}
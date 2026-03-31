package com.concordia.qualiair.Device;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.concordia.qualiair.R;
import com.google.android.material.appbar.MaterialToolbar;

public class CredentialsActivity2 extends AppCompatActivity {

    private EditText etCustomName, etSSID, etPassword;
    private Button btnProvision;
    private TextView tvStatus;
    private BLEConnectionMaker bleConnectionMaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credentials2);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etCustomName = findViewById(R.id.etSSID2);
        etSSID       = findViewById(R.id.etSSID);
        etPassword   = findViewById(R.id.etPassword);
        btnProvision = findViewById(R.id.btnProvision);
        tvStatus     = findViewById(R.id.tvStatus);

        // Get data passed from DiscoveryActivity2
        String bleDeviceName = getIntent().getStringExtra("device_name");
        BluetoothDevice bleDevice = getIntent().getParcelableExtra("ble_device");

        bleConnectionMaker = new BLEConnectionMaker(this);

        btnProvision.setOnClickListener(v -> {
            String customName = etCustomName.getText().toString().trim();
            String ssid       = etSSID.getText().toString().trim();
            String password   = etPassword.getText().toString().trim();

            if (ssid.isEmpty() || password.isEmpty()) {
                tvStatus.setText("Status: Please fill in the missing fields.");
                return;
            }

            if (customName.isEmpty()) {
                customName = bleDeviceName;
            }

            if (bleDevice == null) {
                tvStatus.setText("Status: No device found. Go back and scan again.");
                return;
            }

            // Disable button while sending
            btnProvision.setEnabled(false);
            tvStatus.setText("Status: Sending credentials to device...");

            // Need final reference for use inside callback
            final String finalCustomName = customName;

            // Send credentials to ESP32 via BLE
            bleConnectionMaker.sendCredentials(
                    bleDevice,
                    ssid,
                    password,
                    new BLEConnectionMaker.ProvisionCallback() {
                        @Override
                        public void onSuccess(String ipAddress, String deviceIDESP32) {
                            Device device = new Device(finalCustomName);
                            device.setSsid(ssid);
                            device.setPassword(password);
                            device.setStatusTrue();
                            device.setDeviceIDESP32(deviceIDESP32);

                            // Save IP if we got one
                            if (ipAddress != null && !ipAddress.equals("0.0.0.0")) {
                                device.setIpAddress(ipAddress);
                                Log.d("Credentials", "Device IP saved: " + ipAddress);
                            }

                            DeviceList deviceList = new DeviceList(CredentialsActivity2.this);
                            deviceList.saveDevice(device);

                            runOnUiThread(() -> {
                                tvStatus.setText("Status: Success! ✓");
                                Toast.makeText(CredentialsActivity2.this,
                                        finalCustomName + " provisioned!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(CredentialsActivity2.this, DeviceActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            });
                        }

                        @Override
                        public void onFailure(String reason) {
                            runOnUiThread(() -> {
                                tvStatus.setText("Status: Failed — " + reason);
                                btnProvision.setEnabled(true);
                            });
                        }
                    }
            );
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleConnectionMaker != null) {
            bleConnectionMaker.disconnect();
        }
    }
}
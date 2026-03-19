package com.concordia.qualiair;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;

import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.BleScanListener;

public class DeviceActivity extends AppCompatActivity {

    private ESPProvisionManager provisionManager;
    private static final int REQUEST_PERMISSIONS = 1;
    private static final String TAG = "DeviceActivity";
    private boolean deviceFound = false;//to solve problem of not being able to visit provisioning twice

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device2);

        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());

        //requests for location and usage of bluetooth if it hasnt been done
        if (!hasPermissions() && savedInstanceState == null) {
            requestPermissions();
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnScan).setOnClickListener(v -> {
            if (hasPermissions()) {
                try {
                    provisionManager.stopBleScan();//stop an already ongoing scan
                } catch (SecurityException e) {
                    Log.e(TAG, "Permission error stopping scan: " + e.getMessage());
                }
                startScan();
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
                requestPermissions();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceFound = false; // reset flag when returning to activity
        try {
            provisionManager.stopBleScan(); // stop any lingering scan
        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping scan on resume: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            provisionManager.stopBleScan(); // stop scan when leaving activity
        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping scan on pause: " + e.getMessage());
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permissions are required for BLE scanning",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startScan() {
        deviceFound=false;
        // checking to see state of permissions
        if (!hasPermissions()) {
            Toast.makeText(this, "Missing permissions", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            //searches for qualiAir automatically
            provisionManager.searchBleEspDevices("QualiAir Link", new BleScanListener() {//"QualiAir Link", "110057_5808" or "110059_E4F4"

                @Override
                public void scanStartFailed() {
                    runOnUiThread(() -> Toast.makeText(DeviceActivity.this,
                                    "Scan failed to start", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                    if (deviceFound) return;
                    deviceFound = true;
                    // Check permission before calling getName() or stopBleScan()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ContextCompat.checkSelfPermission(DeviceActivity.this,
                                Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted");
                            return;
                        }
                    }

                    Log.i(TAG, "Found device: " + device.getName());

                    try {
                        provisionManager.stopBleScan();
                    } catch (SecurityException e) {
                        Log.e(TAG, "Error stopping scan: " + e.getMessage());
                    }

                    runOnUiThread(() -> {
                        Intent intent = new Intent(DeviceActivity.this, ProvisioningActivity.class);
                        intent.putExtra("device", device);//sending found device object to provisioning activity
                        startActivity(intent);
                    });
                }

                @Override
                public void scanCompleted() {
                    runOnUiThread(() -> {findViewById(R.id.btnScan).setEnabled(true);
                        if (!deviceFound) {
                            Toast.makeText(DeviceActivity.this,
                                    "No QualiAir device found nearby", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Scan error: " + e.getMessage());
                    runOnUiThread(() ->
                            Toast.makeText(DeviceActivity.this,
                                    "Scan error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException during scan: " + e.getMessage());
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
package com.concordia.qualiair.Device;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.concordia.qualiair.R;
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.BleScanListener;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryActivity2 extends AppCompatActivity {

    private static final String TAG = "DiscoveryActivity2";
    private static final int REQUEST_PERMISSIONS = 1;
    private static final String DEVICE_NAME_PREFIX = "QualiAir Link"; // match name

    private ESPProvisionManager provisionManager;
    private boolean isScanning = false;

    private List<Device> foundDevices = new ArrayList<>();
    private DiscoveredDeviceAdapter adapter;

    private Button btnScan;
    private TextView tvScanStatus;
    private TextView tvFoundLabel;
    private RecyclerView recyclerView;

    private Object device_info_object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery2);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        btnScan = findViewById(R.id.btnScanAgain);
        tvScanStatus = findViewById(R.id.tvScanStatus);
        tvFoundLabel = findViewById(R.id.tvFoundLabel);
        recyclerView = findViewById(R.id.recyclerViewFound);

        // Espressif provision manager
        provisionManager = ESPProvisionManager.getInstance(getApplicationContext());

        // RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DiscoveredDeviceAdapter(foundDevices, device -> {
            // User tapped a found device — pass it to CredentialsActivity2
            stopScan();
            Intent intent = new Intent(this, CredentialsActivity2.class);
            intent.putExtra("device_name", device.getName());
            intent.putExtra("ble_device", device.getBleDevice()); // pass the BluetoothDevice
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Start scanning immediately
        requestPermissionsAndScan();

        btnScan.setOnClickListener(v -> {
            if (isScanning) {
                stopScan();
            } else {
                foundDevices.clear();
                adapter.notifyDataSetChanged();
                tvFoundLabel.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                requestPermissionsAndScan();
            }
        });
    }

    private void requestPermissionsAndScan() {
        if (!hasPermissions()) {
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
        } else {
            startScan();
        }
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

    private void startScan() {
        if (!hasPermissions()) {
            Toast.makeText(this, "Missing permissions", Toast.LENGTH_SHORT).show();
            return;
        }

        isScanning = true;
        btnScan.setText("Stop Scan");
        tvScanStatus.setText("Looking for nearby QualiAir devices...");

        try {
            provisionManager.stopBleScan(); // stop any lingering scan first
        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping previous scan: " + e.getMessage());
        }

        try {
            provisionManager.searchBleEspDevices(DEVICE_NAME_PREFIX, new BleScanListener() {

                @Override
                public void scanStartFailed() {
                    runOnUiThread(() -> {
                        tvScanStatus.setText("Scan failed to start");
                        isScanning = false;
                        btnScan.setText("Scan Again");
                    });
                }

                @Override
                public void onPeripheralFound(BluetoothDevice bleDevice, ScanResult scanResult) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ContextCompat.checkSelfPermission(DiscoveryActivity2.this,
                                Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "BLUETOOTH_CONNECT not granted");
                            return;
                        }
                    }

                    String name = bleDevice.getName();
                    if (name == null) return;

                    // Avoid duplicates
                    for (Device d : foundDevices) {
                        if (d.getName().equals(name)) return;
                    }

                    Log.i(TAG, "Found: " + name);

                    // Build Device object and store the BluetoothDevice inside it
                    Device device = new Device(name);
                    device.setRssi(scanResult.getRssi());
                    device.setBleDevice(bleDevice); // store for later use in credentials
                    foundDevices.add(device);

                    runOnUiThread(() -> {
                        tvFoundLabel.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyItemInserted(foundDevices.size() - 1);
                    });
                }

                @Override
                public void scanCompleted() {
                    Log.i(TAG, "Scan completed");
                    runOnUiThread(() -> {
                        isScanning = false;
                        btnScan.setText("Scan Again");
                        if(foundDevices.isEmpty()){
                            tvScanStatus.setText("Scan complete: No device found :(");
                        }
                        else{
                        tvScanStatus.setText("Scan complete: "+foundDevices.size()+" device(s) found");
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Scan error: " + e.getMessage());
                    runOnUiThread(() -> {
                        isScanning = false;
                        btnScan.setText("Scan Again");
                        tvScanStatus.setText("Scan error: " + e.getMessage());
                    });
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScan() {
        if (!isScanning) return;
        isScanning = false;
        btnScan.setText("Scan Again");
        tvScanStatus.setText("Scan stopped");
        try {
            provisionManager.stopBleScan();
        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping scan: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (hasPermissions()) {
                startScan();
            } else {
                Toast.makeText(this, "Bluetooth permissions required",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
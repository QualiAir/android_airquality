package com.concordia.qualiair.Device;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.concordia.qualiair.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceActivity extends AppCompatActivity {

    private List<Device> devices;
    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private DeviceList deviceList;
    private BLEConnectionMaker bleConnectionMaker;

    // One poller per device
    private final List<PingSender> pollers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Devices");
        }

        bleConnectionMaker = new BLEConnectionMaker(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deviceList = new DeviceList(this);
        devices = deviceList.getAllDevices();

        deviceAdapter = new DeviceAdapter(devices,device -> {});
        recyclerView.setAdapter(deviceAdapter);

        Button btnAdd = findViewById(R.id.btnAddDevice);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiscoveryActivity2.class);
            startActivity(intent);
        });
    }

    private void startPollingAllDevices() {
        stopPollingAllDevices(); // clear any existing pollers first

        for (Device device : devices) {//pings all devices inDeviceActivity
            if (device.getIpAddress() == null || device.getIpAddress().isEmpty()) continue;

            PingSender poller = new PingSender(
                    device.getIpAddress(),
                    new PingSender.DeviceInfoCallback() {
                        @Override
                        public void onReceived(JSONObject deviceInfo) {
                            // Device responded => set to online
                            boolean wasOnline = device.isOnline();
                            device.setStatusTrue();
                            deviceList.saveDevice(device);//no risk of duplicate

                            // refresh UI if status actually changed
                            if (!wasOnline) {
                                refreshList();
                            }
                        }

                        @Override
                        public void onError(String reason) {
                            // Device did not respond — mark offline
                            boolean wasOnline = device.isOnline();
                            device.setStatusFalse();
                            deviceList.saveDevice(device);

                            if (wasOnline) {
                                refreshList();
                            }
                        }
                    }
            );

            poller.start();
            pollers.add(poller);
        }
    }

    private void stopPollingAllDevices() {
        for (PingSender poller : pollers) {
            poller.stop();
        }
        pollers.clear();
    }

    private void refreshList() {
        runOnUiThread(() -> {
            devices.clear();
            devices.addAll(deviceList.getAllDevices());
            if (deviceAdapter != null) deviceAdapter.notifyDataSetChanged();
        });
    }

    private void showConnectDialog(Device device) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_connect_device, null);

        TextView tvDeviceName = dialogView.findViewById(R.id.tvDialogDeviceName);
        TextView tvDeviceSSID = dialogView.findViewById(R.id.tvDialogSSID);
        TextView tvStatus     = dialogView.findViewById(R.id.tvDialogStatus);
        Button btnConnect     = dialogView.findViewById(R.id.btnDialogConnect);
        Button btnCancel      = dialogView.findViewById(R.id.btnDialogCancel);

        tvDeviceName.setText(device.getName());
        tvDeviceSSID.setText("Network: " + device.getSsid());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConnect.setOnClickListener(v -> {
            tvStatus.setText("Connecting...");
            btnConnect.setEnabled(false);

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bleDevice = null;

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            for (BluetoothDevice paired : bluetoothAdapter.getBondedDevices()) {
                if (paired.getName() != null && paired.getName().equals(device.getName())) {
                    bleDevice = paired;
                    break;
                }
            }

            if (bleDevice == null) {
                tvStatus.setText("Device not found nearby. Make sure it is on.");
                btnConnect.setEnabled(true);
                return;
            }

            bleConnectionMaker.sendCredentials(
                    bleDevice,
                    device.getSsid(),
                    device.getPassword(),
                    new BLEConnectionMaker.ProvisionCallback() {
                        @Override
                        public void onSuccess(String ipAddress) {
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                device.setStatusTrue();
                                deviceList.saveDevice(device);
                                if (ipAddress != null && !ipAddress.equals("0.0.0.0")) {
                                    device.setIpAddress(ipAddress);
                                }
                                refreshList();
                                // Restart pollers since a new device is now online
                                startPollingAllDevices();
                            });
                        }

                        @Override
                        public void onFailure(String reason) {
                            runOnUiThread(() -> {
                                tvStatus.setText("Failed: " + reason);
                                btnConnect.setEnabled(true);
                            });
                        }
                    }
            );
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        devices.clear();
        devices.addAll(deviceList.getAllDevices());
        if (deviceAdapter != null) deviceAdapter.notifyDataSetChanged();
        startPollingAllDevices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPollingAllDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleConnectionMaker.disconnect();
        stopPollingAllDevices();
    }
}
package com.concordia.qualiair.Device;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Devices");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bleConnectionMaker = new BLEConnectionMaker(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deviceList = new DeviceList(this);
        devices = deviceList.getAllDevices();

        deviceAdapter = new DeviceAdapter(devices,device -> showDeviceDetailDialog(device));
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

    private void showDeviceDetailDialog(Device device) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_device_detail, null);

        TextView tvDeviceName = dialogView.findViewById(R.id.tvDialogDeviceName);
        TextView tvDeviceSSID = dialogView.findViewById(R.id.tvDialogSSID);
        TextView tvDeviceIP = dialogView.findViewById(R.id.tvDialogStatus);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnDeleteDevice = dialogView.findViewById(R.id.btnDeleteDevice);

        // Populate fields
        tvDeviceName.setText(device.getName());
        tvDeviceSSID.setText("Network: " + device.getSsid());

        String ip = device.getIpAddress();
        if (ip != null && !ip.isEmpty()) {
            tvDeviceIP.setText("IP: " + ip);
        } else {
            tvDeviceIP.setText("IP: Not available");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDeleteDevice.setOnClickListener(v -> {
            deviceList.deleteDevice(device.getName());  // remove from SharedPreferences
            devices.remove(device);           // remove from the list in memory
            deviceAdapter.notifyDataSetChanged(); // refresh the RecyclerView
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        devices.clear();
        devices.addAll(deviceList.getAllDevices());
        if (deviceAdapter != null) deviceAdapter.notifyDataSetChanged();
        if(devices.size()!=0){}
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
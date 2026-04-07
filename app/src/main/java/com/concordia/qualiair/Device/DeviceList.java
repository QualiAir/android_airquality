package com.concordia.qualiair.Device;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceList {
    private SharedPreferences devices_SP;//holds all devices persistently
    private Gson gson;

    public DeviceList(Context context) {
        devices_SP = context.getSharedPreferences("QualiAirDevices", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Save a single device
    public void saveDevice(Device device) {
        String json = gson.toJson(device);
        devices_SP.edit().putString(device.getName(), json).apply();
    }

    // Get a single device by name
    public Device getDevice(String deviceName) {
        String json = devices_SP.getString(deviceName, null);//key = deviceName, value = deviceobject
        if (json == null) return null;
        return gson.fromJson(json, Device.class);
    }

    // Get all devices as an ArrayList
    public List<Device> getAllDevices() {
        List<Device> deviceList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : devices_SP.getAll().entrySet()) {
            String json = (String) entry.getValue();
            Device device = gson.fromJson(json, Device.class);
            deviceList.add(device);
        }
        return deviceList;
    }

    // Delete a device by name
    public void deleteDevice(String deviceName) {
        devices_SP.edit().remove(deviceName).apply();
    }
}
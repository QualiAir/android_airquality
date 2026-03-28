package com.concordia.qualiair.Device;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.concordia.qualiair.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
    }

    private final List<Device> deviceList;
    private final OnDeviceClickListener listener;

    public DeviceAdapter(List<Device> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_device_elements, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceStatus.setText(device.getAppropriateIcon());

        if (device.isOnline()) {
            holder.deviceStatus.setTextColor(Color.parseColor("#00D4AA"));
        } else {
            holder.deviceStatus.setTextColor(Color.parseColor("#6B7280"));
        }

        // Click triggers the dialog
        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, deviceStatus;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceStatus = itemView.findViewById(R.id.deviceStatus);
        }
    }
}
package com.concordia.qualiair.Device;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.concordia.qualiair.R;

import java.util.List;

public class DiscoveredDeviceAdapter extends RecyclerView.Adapter<DiscoveredDeviceAdapter.ViewHolder> {

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
    }

    private final List<Device> deviceList;
    private final OnDeviceClickListener listener;

    public DiscoveredDeviceAdapter(List<Device> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_discovery_elements, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.tvName.setText(device.getName());
        holder.tvRssi.setText("Signal: " + device.getRssi() + " dBm");
        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRssi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoundDeviceName);
            tvRssi = itemView.findViewById(R.id.tvFoundDeviceRssi);
        }
    }
}

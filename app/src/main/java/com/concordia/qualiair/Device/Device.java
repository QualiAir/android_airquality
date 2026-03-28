package com.concordia.qualiair.Device;

import android.bluetooth.BluetoothDevice;

public class Device {
    private String name;
    private boolean status=false;//online=true, offline=false
    private String ssid;
    private String password;
    private int rssi; //signal strength
    private String ipAddress;

    private transient BluetoothDevice bleDevice;//transient wont be saved in sharedpref


    Device(String name){
        this.name = name;
    }

    public void setPassword(String password){
        this.password=password;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
    public void setStatusTrue(){
        this.status=true;
    }
    public void setStatusFalse(){
        this.status=false;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setBleDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public boolean isOnline(){
        return status;
    }

    public String getName(){
        return name;
    }

    public boolean getStatus(){
        return status;
    }
    public String getAppropriateIcon(){
        if(status){
            return "\uD83D\uDFE2 Online";
        }
        return"\uD83D\uDD34 Offline";
    }
    public int getRssi() { return rssi; }
    public void setRssi(int rssi) { this.rssi = rssi; }

    public String getSsid() {
        return ssid;
    }

    public String getPassword() {
        return password;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public BluetoothDevice getBleDevice() { return bleDevice; }
}

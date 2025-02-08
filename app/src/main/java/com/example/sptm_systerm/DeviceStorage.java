package com.example.sptm_systerm;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceStorage {
    private SharedPreferences sharedPreferences;

    public DeviceStorage(Context context) {
        sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE);
    }

    // Store device information
    public void saveDeviceInfo(String devId, String productId, String homeId, String deviceName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("devId", devId);
        editor.putString("productId", productId);
        editor.putString("homeId", homeId);
        editor.putString("deviceName", deviceName);
        editor.apply();
    }

    // Retrieve device information
    public String getDevId() {
        return sharedPreferences.getString("devId", null);
    }

    public String getProductId() {
        return sharedPreferences.getString("productId", null);
    }

    public String getHomeId() {
        return sharedPreferences.getString("homeId", null);
    }

    public String getDeviceName() {
        return sharedPreferences.getString("deviceName", "Device");
    }
}


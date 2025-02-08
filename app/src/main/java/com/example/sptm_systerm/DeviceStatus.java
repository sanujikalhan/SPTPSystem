package com.example.sptm_systerm;

import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.scene.api.IResultCallback;
import com.thingclips.smart.sdk.api.IDevListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class DeviceStatus {
    private DeviceActivity deviceActivity; // reference to the Activity

    // Constructor to pass the activity reference
    public DeviceStatus(DeviceActivity activity) {
        this.deviceActivity = activity;
    }
    public void fetchDeviceStatus(String deviceId) {

        // Subscribe to device updates
        ThingHomeSdk.newDeviceInstance(deviceId).registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                try {
                    JSONObject dpJson = new JSONObject(dpStr);
                    if (dpJson.has("cur_voltage")) {
                        double voltage = dpJson.getDouble("cur_voltage") / 10; // Convert from integer to volts
                        System.out.println("Voltage: " + voltage + " V");
                        deviceActivity.updateVoltage(voltage);
                    }

                    if (dpJson.has("cur_current")) {
                        double current = dpJson.getDouble("cur_current") / 1000; // Convert from milliamps to amps
                        System.out.println("Current: " + current + " A");
                        deviceActivity.updateVoltage(current);
                    }

                    if (dpJson.has("cur_power")) {
                        double power = dpJson.getDouble("cur_power") / 10; // Convert from integer to watts
                        System.out.println("Power: " + power + " W");
                        deviceActivity.updateVoltage(power);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onRemoved(String devId) {
                System.out.println("Device removed: " + devId);
            }

            @Override
            public void onStatusChanged(String devId, boolean online) {
                System.out.println("Device " + devId + " status changed: " + (online ? "Online" : "Offline"));
            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {

            }
        });
}
    }
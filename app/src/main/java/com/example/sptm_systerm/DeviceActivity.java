package com.example.sptm_systerm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IDevListener;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceActivity extends AppCompatActivity {
    private TextView voltageTextView, currentTextView, powerTextView;
    private DeviceStatus deviceStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device);

        voltageTextView = findViewById(R.id.voltageTextView);
        currentTextView = findViewById(R.id.currentTextView);
        powerTextView = findViewById(R.id.powerTextView);

        // Initialize DeviceStatus and fetch status
       // deviceStatus = new DeviceStatus(this);
        //SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        //String devId = sharedPreferences.getString("devId", "defaultDevId");
        //deviceStatus.fetchDeviceStatus(devId);
        DeviceStorage deviceStorage = new DeviceStorage(this);
        fetchDeviceStatus(deviceStorage.getDevId());
    }

    public void updateVoltage(double voltage) {
        // Update voltage on the UI (must run on the main thread)
        runOnUiThread(() -> voltageTextView.setText("Voltage: " + voltage + " V"));
    }

    // Similarly, you can define updateCurrent and updatePower methods.
    public void updateCurrent(double current) {
        runOnUiThread(() -> currentTextView.setText("Current: " + current + " A"));
    }

    public void updatePower(double power) {
        runOnUiThread(() -> powerTextView.setText("Power: " + power + " W"));
    }
    public void fetchDeviceStatus(String deviceId) {

        // Subscribe to device updates
        ThingHomeSdk.newDeviceInstance(deviceId).registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                try {
                    JSONObject dpJson = new JSONObject(dpStr);
                    if (dpJson.has("20")) {
                        double voltage = dpJson.getDouble("20") / 10; // Convert from integer to volts
                        System.out.println("Voltage: " + voltage + " V");
                        updateVoltage(voltage);
                    }

                    if (dpJson.has("18")) {
                        double current = dpJson.getDouble("18") / 1000; // Convert from milliamps to amps
                        System.out.println("Current: " + current + " A");
                        updateCurrent(current);
                    }

                    if (dpJson.has("19")) {
                        double power = dpJson.getDouble("19") / 10; // Convert from integer to watts
                        System.out.println("Power: " + power + " W");
                        updatePower(power);
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
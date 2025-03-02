package com.example.sptm_systerm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.*;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.protobuf.StringValue;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeviceActivity extends AppCompatActivity {
    private TextView voltageTextView, currentTextView, powerTextView, timeDisplay, usageText;
    private static final String TAG = "DeviceActivity";
    private IThingDevice mDevice;
    private String deviceId;
    private Handler retryHandler;
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 10;
    private static final long INITIAL_RETRY_DELAY = 5000;
    private static final long UPDATE_INTERVAL = 2000;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Global variable to control stopwatch
    private boolean isTimeCounting = false;

    // BarChart for plotting the data
    private BarChart powerBarChart;
    private List<BarEntry> voltageEntries = new ArrayList<>();
    private List<BarEntry> currentEntries = new ArrayList<>();
    private List<BarEntry> powerEntries = new ArrayList<>();
    private long changedTime = 0;
    private DeviceStorage deviceStorage;

    // Stopwatch variables
    private long startTime = 0;
    private long startTime2 = 0;
    private long usage = 0;
    private boolean running = false;
    private Handler stopwatchHandler = new Handler();
    private EditText threshold;
    private FirebaseService firebaseService;
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                int milliseconds = (int) (elapsedTime % 1000);
                timeDisplay.setText(String.format("%02d:%02d:%03d", minutes, seconds, milliseconds));
                usageText.setText("Total Power Usage : " + usage);
                stopwatchHandler.postDelayed(this, 10); // Update every 10 milliseconds
                long thresholdValue = Long.parseLong(threshold.getText().toString());
                if (usage >= thresholdValue) {
                    String dpsKey = "1";

                    Map<String, Object> dps = new HashMap<>();
                    dps.put(dpsKey, false);
                    ThingHomeSdk.newDeviceInstance(deviceId).publishDps(
                            new JSONObject(dps).toString(),
                            new IResultCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(DeviceActivity.this, "Plug turned " + (false ? "on" : "off"), Toast.LENGTH_SHORT).show();
                                    stopStopwatch();
                                }

                                @Override
                                public void onError(String errorCode, String errorMessage) {
                                    Toast.makeText(DeviceActivity.this, "Failed to toggle plug: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        voltageTextView = findViewById(R.id.voltageTextView);
        currentTextView = findViewById(R.id.currentTextView);
        powerTextView = findViewById(R.id.powerTextView);
        powerBarChart = findViewById(R.id.powerBarChart);  // Initialize the BarChart
        timeDisplay = findViewById(R.id.timeDisplay); // Stopwatch display
        usageText = findViewById(R.id.usage);
        threshold = findViewById(R.id.limit);
        firebaseService = new FirebaseService();

        sharedPreferences = getSharedPreferences("DeviceTiming", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isTimeCounting = sharedPreferences.getBoolean("countdown", false);
        threshold.setText(String.valueOf(1000));

        handlerThread = new HandlerThread("DeviceUpdateThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
        retryHandler = new Handler(Looper.getMainLooper());

        deviceStorage = new DeviceStorage(this);
        deviceId = deviceStorage.getDevId();

        fetchDeviceStatus(); // Start fetching device data

        // Start stopwatch automatically if isTimeCounting is true
        if (isTimeCounting) {
            startStopwatch();
        }
    }

    private void startStopwatch() {
        if (!running) {
            startTime = System.currentTimeMillis() - (startTime % 1000); // Keep track of elapsed time
            stopwatchHandler.post(updateTimeRunnable);
            running = true;
        }
    }

    private void stopStopwatch() {
        if (running) {
            stopwatchHandler.removeCallbacks(updateTimeRunnable);
            running = false;
        }
    }

    private void resetStopwatch() {
        startTime = 0;
        timeDisplay.setText("00:00:000");
        if (running) {
            stopwatchHandler.removeCallbacks(updateTimeRunnable);
            running = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Activity Resumed - Restarting Device Listener");
        backgroundHandler.removeCallbacksAndMessages(null); // Clear any existing callbacks
        fetchDeviceStatus(); // Restart data fetching when returning

        // Automatically start stopwatch when activity resumes and isTimeCounting is true
        if (isTimeCounting) {
            startStopwatch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Activity Paused - Unregistering Device Listener");
        if (mDevice != null) {
            mDevice.unRegisterDevListener();
        }
        backgroundHandler.removeCallbacksAndMessages(null);
    }

    private void fetchDeviceStatus() {
        if (deviceId == null || deviceId.isEmpty()) {
            Log.e(TAG, "Device ID is null or empty.");
            showToast("Device ID is missing");
            return;
        }

        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching device status...");

                if (mDevice == null) {
                    mDevice = ThingHomeSdk.newDeviceInstance(deviceId);
                    if (mDevice == null) {
                        Log.e(TAG, "Device instance is null. SDK might not be initialized.");
                        retryRegisterListener();
                        return;
                    }
                }

                // Unregister existing listener before re-registering
                mDevice.unRegisterDevListener();
                registerDeviceListener();

                // Schedule periodic updates
                backgroundHandler.removeCallbacksAndMessages(null); // Prevent multiple duplicate tasks
                backgroundHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        });
    }

    private void registerDeviceListener() {
        if (mDevice == null) {
            Log.e(TAG, "Cannot register listener. Device is null.");
            return;
        }

        Log.i(TAG, "Registering device listener...");

        mDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                JSONObject dpJson = null;
                double power = 0;
                try {
                    dpJson = new JSONObject(dpStr);
                    power = dpJson.getDouble("19") / 10;
                    firebaseService.addDeviceReading(
                            deviceStorage.getHomeId(),               // Room Name
                            deviceId,             // Device Name
                            getCurrentDateTime(),  // Date and Time (formatted)
                            power,                 // Value (double)
                            aVoid -> {
                                // Success Callback
                                Log.d("Firebase", "Reading added successfully!");
                            },
                            e -> {
                                // Failure Callback
                                Log.e("Firebase", "Error adding reading: " + e.getMessage());
                            }
                    );
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                long elapsedTime = System.currentTimeMillis() - startTime2;
                int seconds = (int) (elapsedTime / 1000);
                seconds = seconds % 60;
                usage += (long) (power * seconds);
                changedTime = System.currentTimeMillis();
                Log.d(TAG, "Received DP Update: " + dpStr);
                showToast("DP Update: " + dpStr);
                retryCount = 0;
                backgroundHandler.post(() -> parseDeviceData(dpStr));
                startTime2 = System.currentTimeMillis();
            }

            @Override
            public void onRemoved(String devId) {
                Log.w(TAG, "Device removed: " + devId);
                showToast("Device removed");
            }

            @Override
            public void onStatusChanged(String devId, boolean online) {
                Log.i(TAG, "Device status changed: " + (online ? "Online" : "Offline"));
                showToast("Device is " + (online ? "Online" : "Offline"));
                if (!online) {
                    retryRegisterListener();
                }
            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {
                Log.d(TAG, "Network status changed: " + (status ? "Connected" : "Disconnected"));
                showToast("Network: " + (status ? "Connected" : "Disconnected"));
                if (!status) {
                    retryRegisterListener();
                }
            }

            @Override
            public void onDevInfoUpdate(String devId) {
                Log.i(TAG, "Device info updated: " + devId);
                showToast("Device info updated");
            }
        });
    }
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }
    private void retryRegisterListener() {
        if (retryCount >= MAX_RETRIES) {
            Log.e(TAG, "Max retries reached. Stopping reconnection attempts.");
            return;
        }

        long retryDelay = INITIAL_RETRY_DELAY * (retryCount + 1);
        Log.i(TAG, "Retrying connection in " + retryDelay / 1000 + " seconds... (Attempt " + (retryCount + 1) + ")");
        retryHandler.postDelayed(() -> {
            mDevice = null; // Reset device instance
            fetchDeviceStatus();
        }, retryDelay);

        retryCount++;
    }

    private void parseDeviceData(String dpStr) {
        try {
            JSONObject dpJson = new JSONObject(dpStr);
            updateValue(dpJson, "20", 10.0, voltageTextView, "Voltage", "V", voltageEntries);
            updateValue(dpJson, "18", 1000.0, currentTextView, "Current", "A", currentEntries);
            updateValue(dpJson, "19", 10.0, powerTextView, "Power", "W", powerEntries);

            // After updating the lists, update the chart
            runOnUiThread(() -> updateChart());

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing DP JSON", e);
        }
    }

    private void updateValue(JSONObject dpJson, String key, double divisor, TextView textView, String label, String unit, List<BarEntry> entries) {
        if (dpJson.has(key)) {
            try {
                double value = dpJson.getDouble(key) / divisor;
                runOnUiThread(() -> textView.setText(value + " " + unit));
                Log.d(TAG, label + " updated: " + value + " " + unit);

                // Add the value to the entries list with time (for simplicity, let's use an incrementing index as the time)
                entries.add(new BarEntry(entries.size(), (float) value));

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing " + label, e);
            }
        }
    }

    private void updateChart() {
        // Create BarDataSet for power only
        BarDataSet powerDataSet = new BarDataSet(powerEntries, "Power Consumption (W)");

        // Combine all data sets (we only have one for power here)
        BarData data = new BarData(powerDataSet);

        // Set colors for the dataset
        powerDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        // Set the data to the chart and refresh it
        powerBarChart.setData(data);

        // Set the X-Axis label to represent time
        powerBarChart.getXAxis().setLabelCount(powerEntries.size());  // Adjust label count based on entries

        // Y-Axis label for power
        powerBarChart.getAxisLeft().setLabelCount(6, true);
        powerBarChart.getAxisLeft().setAxisMinimum(0); // Min value for Y-axis

        // X-Axis labels
        powerBarChart.getXAxis().setGranularity(1f); // Ensure there's a gap between bars
        powerBarChart.getXAxis().setLabelRotationAngle(45); // Rotate the X labels for better visibility

        // Refresh the chart
        powerBarChart.invalidate();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(DeviceActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity Destroyed - Unregistering Device Listener");

        if (mDevice != null) {
            mDevice.unRegisterDevListener();
        }

        retryHandler.removeCallbacksAndMessages(null);
        backgroundHandler.removeCallbacksAndMessages(null);
        handlerThread.quitSafely();
    }
}

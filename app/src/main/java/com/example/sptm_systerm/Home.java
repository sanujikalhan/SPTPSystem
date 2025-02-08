package com.example.sptm_systerm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.bean.HomeBean;
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder;
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.enums.ActivatorEZStepCode;
import com.thingclips.smart.sdk.enums.ActivatorModelEnum;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity {
    String HomeName = "myhome";
    String[] rooms= {"room1","room2","room3","room4"};
    private HomeBean currentHomeBeam;
    private String ssid = "Redmi Note 13"; //"NACK899""Redmi Note 13"
    private String password = "chathurikarich"; //"54844bF1"
    private DeviceBean currentDeviceBeam;
    private IThingActivator mThingActivator;
    private boolean isPlugOn = false;
    private ArrayList<String> roomList = new ArrayList<>(); // Initialize here
    private ProgressBar progressBar;
    private int progress = 0;
    private TextView deviceName, deviceId, productId;
    private DeviceStorage deviceStorage;
    private String devId;
    private String productI;
    private static long homeId;
    private String deviceNam;
    private LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Initialize the TextViews for device information

        deviceName = findViewById(R.id.devicename);
        deviceId = findViewById(R.id.deviceid);
        productId = findViewById(R.id.productid);
        progressBar = findViewById(R.id.progressBar);
        Button search = findViewById(R.id.search);
        Button on = findViewById(R.id.ON);
        Button off = findViewById(R.id.OFF);
        Button deleteThisAccount = findViewById(R.id.OFF6);
        roomList.addAll(Arrays.asList(rooms));
        //createHome(HomeName, roomList);
        progressBar.setVisibility(View.INVISIBLE);
        linearLayout= findViewById(R.id.linearL);
        deviceStorage = new DeviceStorage(this);

        if(deviceStorage.getDevId() !=null){
            queryHomeDetail();
            devId = deviceStorage.getDevId();
            productI = deviceStorage.getProductId();
            homeId = Long.parseLong(deviceStorage.getHomeId());
            deviceNam = deviceStorage.getDeviceName();
            deviceName.setText("Device Name: " + deviceNam);
            deviceId.setText("Device ID: " + devId);
            productId.setText("Product ID: " + productI);
            queryHomeDetail();


        }
        else {
        // Proceed with binding
        createHome(HomeName, roomList);
        }


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mThingActivator != null) {
                    mThingActivator.start();
                    progress = 5;
                    progressBar.setProgress(progress);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDeviceBeam != null) {
                    toggleSmartPlug(currentDeviceBeam.devId);
                } else {
                    Toast.makeText(Home.this, "Device not found", Toast.LENGTH_SHORT).show();
                }
            }
        });



        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDeviceBeam != null) {
                    removeDevice(currentDeviceBeam.getDevId());
                } else {
                    Toast.makeText(Home.this, "No device to remove", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteThisAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount delete_acc = new deleteAccount(Home.this);
                delete_acc.cancel();
            }
        });

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent linear = new Intent(Home.this,DeviceActivity.class);
                startActivity(linear);
            }
        });

    }

    private void createHome(String home, List<String> roomList) {
        ThingHomeSdk.getHomeManagerInstance().createHome(home, 0, 0, "", roomList, new IThingHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                currentHomeBeam = bean;
                Log.d("HomeCreation", "Home creation successfully.");
                getRegistrationToken();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Log.e("ValidationCodeError", "Error code: " + errorCode + ", message: " + errorMsg);
                Toast.makeText(Home.this, "Failed to create home: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRegistrationToken() {
        if (currentHomeBeam == null) {
            Toast.makeText(this, "Home is not initialized.", Toast.LENGTH_SHORT).show();
            return;
        }
        long homeId = currentHomeBeam.getHomeId();
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId, new IThingActivatorGetToken() {
            @Override
            public void onSuccess(String token) {
                SearchDevices(token);
            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {
                Toast.makeText(Home.this, "Token retrieval failed: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SearchDevices(String token) {
        if (token == null || token.isEmpty()) {
            Log.e("SearchDevices", "Token is null or empty.");
            Toast.makeText(this, "Invalid token, cannot search devices.", Toast.LENGTH_SHORT).show();
            return;
        }
        progress = 25;
        progressBar.setProgress(progress);
        progressBar.setVisibility(View.VISIBLE);
        mThingActivator = ThingHomeSdk.getActivatorInstance().newMultiActivator(new ActivatorBuilder()
                .setSsid(ssid)
                .setContext(this)
                .setPassword(password)
                .setActivatorModel(ActivatorModelEnum.THING_EZ)
                .setTimeOut(100)
                .setToken(token)
                .setListener(new IThingSmartActivatorListener() {

                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Toast.makeText(Home.this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onActiveSuccess(DeviceBean devResp) {
                        progress = 100;
                        progressBar.setProgress(progress);
                        currentDeviceBeam = devResp;


                        // Display device information
                        deviceName.setText("Device Name: " + currentDeviceBeam.getName());
                        deviceId.setText("Device ID: " + currentDeviceBeam.getDevId());
                        productId.setText("Product ID: " + currentDeviceBeam.getProductId());
                        onDeviceBound(currentDeviceBeam);
                        progressBar.setVisibility(View.INVISIBLE);
                        mThingActivator.stop();
                    }

                    @Override
                    public void onStep(String step, Object data) {
                        switch (step) {
                            case ActivatorEZStepCode.DEVICE_BIND_SUCCESS:
                                progress = 85;
                                progressBar.setProgress(progress);
                                Toast.makeText(Home.this, "Bind Success", Toast.LENGTH_SHORT).show();
                                break;
                            case ActivatorEZStepCode.DEVICE_FIND:
                                progress = 85;
                                progressBar.setProgress(progress);
                                Toast.makeText(Home.this, "Device Found", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                })
        );

            mThingActivator.start(); // Start the activator here

    }

    private void toggleSmartPlug(String devId) {
        isPlugOn = !isPlugOn;
        String dpsKey = "1";

        Map<String, Object> dps = new HashMap<>();
        dps.put(dpsKey, isPlugOn);

        ThingHomeSdk.newDeviceInstance(devId).publishDps(
                new JSONObject(dps).toString(),
                new IResultCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(Home.this, "Plug turned " + (isPlugOn ? "on" : "off"), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(Home.this, "Failed to toggle plug: " + errorMessage, Toast.LENGTH_SHORT).show();
                        isPlugOn = !isPlugOn;
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mThingActivator != null) {
            mThingActivator.onDestroy();
        }
    }

    private void removeDevice(String devId) {
        if (devId == null) {
            Toast.makeText(this, "No device to remove", Toast.LENGTH_SHORT).show();
            deviceStorage.saveDeviceInfo(null, null, null, null);
            return;
        }

        ThingHomeSdk.newDeviceInstance(devId).removeDevice(new IResultCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(Home.this, "Device removed successfully", Toast.LENGTH_SHORT).show();
                currentDeviceBeam = null; // Clear the reference to the removed device
                deviceName.setText("");
                deviceId.setText("");
                productId.setText("");
                deviceStorage.saveDeviceInfo(null, null, null, null);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(Home.this, "Failed to remove device: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void onDeviceBound(DeviceBean device) {
        // Assuming 'device' is the DeviceBean object returned after successful binding
        deviceStorage.saveDeviceInfo(device.getDevId(), device.getProductId(), currentHomeBeam.getHomeId() + "", device.getName());

        // Set the retrieved device name in UI
        deviceName.setText("Device Name: " + device.getName());
    }
    private void queryHomeDetail() {

        ThingHomeSdk.newHomeInstance(Long.parseLong(deviceStorage.getHomeId())).getHomeDetail(new IThingHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                // do something
                List<DeviceBean> deviceList = bean.getDeviceList();
                if (!deviceList.isEmpty()) {
                    currentDeviceBeam = deviceList.get(0);
                     //onDeviceBound(currentDeviceBeam);  // Update UI and save device info
                } else {
                    Toast.makeText(Home.this, "No device found in the home", Toast.LENGTH_SHORT).show();
                    createHome(HomeName, roomList);
                }
            }
            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(Home.this, "Failed to query home details: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

    }


}

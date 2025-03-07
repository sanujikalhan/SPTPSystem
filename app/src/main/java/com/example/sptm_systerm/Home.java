package com.example.sptm_systerm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
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
    private String ssid = "Redmi Note 13"; //"NACK899""Redmi Note 13" //"Redmi Note 13"
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
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
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
        // new add

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Disable default toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);  // Disable default hamburger icon
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_view_bill) {
                    // Open View Electricity Bill Activity
                    startActivity(new Intent(Home.this, AddBill.class));
                } else if (id == R.id.nav_lodge_complaint) {
                    // Open Lodge Complaint Activity
                    startActivity(new Intent(Home.this, ComplaintStatus.class));
                } else if (id == R.id.nav_user_loyalty_points) {
                    // Open User Loyalty Points Activity
                    startActivity(new Intent(Home.this, LoyaltyPoints.class));
                }else if (id == R.id.nav_user_complaints_list) {
                    startActivity(new Intent(Home.this, UserComplaintHistoryActivity.class));
                }
                else if (id == R.id.nav_payment) {
                    startActivity(new Intent(Home.this, Payment.class));
                }
                else if (id == R.id.nav_logout) {
                    // Show confirmation dialog before logging out
                    new AlertDialog.Builder(Home.this)
                            .setTitle("Logout Confirmation")
                            .setMessage("Are you sure you want to log out?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(Home.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                                    // Redirect to Login Activity
                                    Intent intent = new Intent(Home.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("Cancel", null) // Closes the dialog if "Cancel" is clicked
                            .show();
                }


                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });        //new add

        hideMenuItemsForRoles();

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
    /** Hide navigation menu items dynamically based on user role */
    private void hideMenuItemsForRoles() {
        Menu menu = navigationView.getMenu();

        // Hide all menu items first
        menu.findItem(R.id.nav_home).setVisible(false);
        menu.findItem(R.id.nav_view_bill).setVisible(false);
        menu.findItem(R.id.nav_lodge_complaint).setVisible(false);
        menu.findItem(R.id.nav_user_complaints_list).setVisible(false);
        menu.findItem(R.id.nav_user_loyalty_points).setVisible(false);
        menu.findItem(R.id.nav_add_biill).setVisible(false);
        menu.findItem(R.id.nav_admin_complaint).setVisible(false);
        menu.findItem(R.id.nav_tech_complaint).setVisible(false);
        menu.findItem(R.id.nav_complaint_status).setVisible(false);
        menu.findItem(R.id.nav_profile).setVisible(false);
        menu.findItem(R.id.nav_settings).setVisible(false);
        menu.findItem(R.id.nav_payment).setVisible(false);

        // Enable menu items based on user role
        if (GlobalVariable.userRole.equals("User")) {
            menu.findItem(R.id.nav_home).setVisible(true);
            menu.findItem(R.id.nav_view_bill).setVisible(true);
            menu.findItem(R.id.nav_lodge_complaint).setVisible(true);
            menu.findItem(R.id.nav_user_complaints_list).setVisible(true);
            menu.findItem(R.id.nav_user_loyalty_points).setVisible(true);
        } else if (GlobalVariable.userRole.equals("Admin")) {
            menu.findItem(R.id.nav_admin_complaint).setVisible(true);
            menu.findItem(R.id.nav_user_loyalty_points).setVisible(true);
        } else if (GlobalVariable.userRole.equals("Reader")) {
            menu.findItem(R.id.nav_add_biill).setVisible(true);
        } else if (GlobalVariable.userRole.equals("Technician")) {
            menu.findItem(R.id.nav_tech_complaint).setVisible(true);
            menu.findItem(R.id.nav_complaint_status).setVisible(true);
        }

        // Always show Logout
        menu.findItem(R.id.nav_logout).setVisible(true);
    }

}

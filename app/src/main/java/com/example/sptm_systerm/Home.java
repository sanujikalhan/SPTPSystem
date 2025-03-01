package com.example.sptm_systerm;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
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
    private String ssid = "ERANDA"; //"NACK899""Redmi Note 13"
    private String password = "19765320"; //"54844bF1"
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
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        firebaseService = new FirebaseService();
        firebaseService.getUserRole(
                role -> {
                    // Successfully retrieved role
                    GlobalVariable.userRole = role;
                    Log.d(TAG, "User Role: " + role);
                },
                e -> {
                    // Failed to retrieve role
                    Log.e(TAG, "Error getting user role", e);
                    Toast.makeText(Home.this, "Failed to get user role", Toast.LENGTH_SHORT).show();
                }
        );


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
        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 🔹 Check if navigationView is NULL before accessing it
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = headerView.findViewById(R.id.nav_header_title);
            TextView navEmail = headerView.findViewById(R.id.nav_header_email);

            navUsername.setText(GlobalVariable.subscriptionNo);
            navEmail.setText(GlobalVariable.userRole);
        } else {
            Log.e("NavigationView", "NavigationView is null! Check XML layout.");
        }
        // Disable default toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);  // Disable default hamburger icon
        toggle.syncState();

        // Set custom icon for the toggle button
        if (getSupportActionBar() != null) {
            Drawable drawable = getResources().getDrawable(R.drawable.menu);
            Drawable resizedDrawable = resizeDrawable(drawable, 24, 24); // Resize if needed
            getSupportActionBar().setHomeAsUpIndicator(resizedDrawable);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Handle Navigation Item Clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_view_bill) {
                    startActivity(new Intent(Home.this, AddBill.class));
                } else if (id == R.id.nav_lodge_complaint) {
                    startActivity(new Intent(Home.this, ComplaintStatus.class));
                }else if (id == R.id.nav_user_loyalty_points) {
                    if (!GlobalVariable.userRole.equals("User")) {
                        startActivity(new Intent(Home.this, LoyaltyConfigActivity.class));
                    } else {
                        startActivity(new Intent(Home.this, LoyalityPoints.class));
                    }
                } else if (id == R.id.nav_complaint_status) {
                    startActivity(new Intent(Home.this, ComplaintStatus.class));
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(Home.this, "Logged out", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_add_biill) {
                    startActivity(new Intent(Home.this, AddBill.class));
                } else if (id == R.id.nav_admin_complaint) {
                    startActivity(new Intent(Home.this, AdminComplaintListActivity.class));
                } else if (id == R.id.nav_tech_complaint) {
                    startActivity(new Intent(Home.this, TechnicianComplaintListActivity.class));
                } else if (id == R.id.nav_user_complaints_list) {
                    startActivity(new Intent(Home.this, UserComplaintHistoryActivity.class));
                }
                else if (id == R.id.nav_payment) {
                    startActivity(new Intent(Home.this, Payment.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


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
        hideMenuItemsForRoles();
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

    private Drawable resizeDrawable(Drawable image, int width, int height) {
        if (image instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            return new BitmapDrawable(getResources(), resizedBitmap);
        } else {
            return image; // Return original if not BitmapDrawable
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                .setTimeOut(1000)
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
                        sharedPreferences = getSharedPreferences("DeviceTiming", Context.MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putBoolean("countdown", true);
                        editor.apply();
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

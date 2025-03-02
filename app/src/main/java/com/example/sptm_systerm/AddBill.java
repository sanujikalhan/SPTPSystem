package com.example.sptm_systerm;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AddBill extends AppCompatActivity {

    private EditText subscriptionNumber;
    private TextView datePickerText;
    private Button submitBtn;
    private String selectedDate = "";
    private FirebaseService firebaseService;

    // Navigation Drawer Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        // Initialize UI components
        subscriptionNumber = findViewById(R.id.subscriptionNumber);
        datePickerText = findViewById(R.id.datePickerText);
        submitBtn = findViewById(R.id.submitBtn);

        // Initialize Firebase Service
        firebaseService = new FirebaseService();

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Setup Custom Toggle Button
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set Custom Icon for Toggle Button
        if (getSupportActionBar() != null) {
            Drawable drawable = getResources().getDrawable(R.drawable.menu);
            Drawable resizedDrawable = resizeDrawable(drawable, 24, 24);
            getSupportActionBar().setHomeAsUpIndicator(resizedDrawable);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle Navigation Drawer Item Clicks
        setupNavigationDrawer();

        // Hide menu items based on user roles
        hideMenuItemsForRoles();

        // Set Date Picker Click Listener
        datePickerText.setOnClickListener(v -> showDatePickerDialog());

        // Set Submit Button Click Listener
        submitBtn.setOnClickListener(v -> submitBill());
    }

    /** Function to Show the Date Picker Dialog */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    datePickerText.setText(selectedDate);
                },
                year, month, day
        );

        // If the user is a Reader, disable past dates
        if (GlobalVariable.userRole.equals("Reader")) {
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    /** Function to Handle Bill Submission */
    /** Function to Handle Bill Submission */
    private void submitBill() {
        String subscriptionNo = subscriptionNumber.getText().toString().trim();

        // Validate Subscription Number
        if (subscriptionNo.isEmpty()) {
            Toast.makeText(this, "Please enter Subscription Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!subscriptionNo.matches("^[0-9]+$")) { // Ensure it contains only numbers
            Toast.makeText(this, "Subscription Number must be numeric!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Date Selection
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please pick a date", Toast.LENGTH_SHORT).show();
            return;
        }

        GlobalVariable.pickedDate = selectedDate;

        // Call Firebase to retrieve meter readings
        firebaseService.subscribeToMeterReadings(subscriptionNo,
                readingsList -> {
                    if (readingsList == null || readingsList.isEmpty()) {
                        Toast.makeText(AddBill.this, "No meter readings found for this subscription!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<MeterReading> meterReadings = new ArrayList<>();
                    for (Map<String, Object> reading : readingsList) {
                        String date = reading.get("date").toString();
                        Object value = reading.get("value");

                        if (value == null) {
                            Toast.makeText(AddBill.this, "Invalid reading value detected!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        meterReadings.add(new MeterReading(date, value));
                    }

                    // Store meter readings globally and navigate to Bill Generation
                    GlobalVariable.meterReadings = meterReadings;
                    Intent intent = new Intent(AddBill.this, BillGeneration.class);
                    startActivity(intent);
                },
                e -> {
                    Log.e("FirebaseError", "Failed to retrieve meter readings", e);
                    Toast.makeText(AddBill.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    /** Function to Setup Navigation Drawer Clicks */
    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_view_bill) {
                startActivity(new Intent(AddBill.this, AddBill.class));
            } else if (id == R.id.nav_lodge_complaint) {
                startActivity(new Intent(AddBill.this, ComplaintStatus.class));
            }else if (id == R.id.nav_user_loyalty_points) {
                if (!GlobalVariable.userRole.equals("User")) {
                    startActivity(new Intent(AddBill.this, LoyaltyConfigActivity.class));
                } else {
                    startActivity(new Intent(AddBill.this, LoyalityPoints.class));
                }
            } else if (id == R.id.nav_complaint_status) {
                startActivity(new Intent(AddBill.this, ComplaintStatus.class));
            } else if (id == R.id.nav_logout) {
                Toast.makeText(AddBill.this, "Logged out", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_add_biill) {
                startActivity(new Intent(AddBill.this, AddBill.class));
            } else if (id == R.id.nav_admin_complaint) {
                startActivity(new Intent(AddBill.this, AdminComplaintListActivity.class));
            } else if (id == R.id.nav_tech_complaint) {
                startActivity(new Intent(AddBill.this, TechnicianComplaintListActivity.class));
            } else if (id == R.id.nav_user_complaints_list) {
                startActivity(new Intent(AddBill.this, UserComplaintHistoryActivity.class));
            }
            else if (id == R.id.nav_payment) {
                startActivity(new Intent(AddBill.this, Payment.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /** Function to Hide Navigation Menu Items Based on User Role */
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
    /** Function to Resize the Toggle Button Icon */
    private Drawable resizeDrawable(Drawable image, int width, int height) {
        if (image instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            return new BitmapDrawable(getResources(), resizedBitmap);
        } else {
            return image;
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
}

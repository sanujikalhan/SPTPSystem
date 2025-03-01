package com.example.sptm_systerm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class AdminComplaintListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintsAdapter adapter;
    private FirebaseService firebaseService;

    // Navigation Drawer Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaint_list);

        // Initialize Firebase Service
        firebaseService = new FirebaseService();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.adminComplaintsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

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

        // Load Complaints
        loadComplaints();
    }

    /** Function to Load Complaints from Firebase */
    private void loadComplaints() {
        firebaseService.getAllComplaints(
                complaints -> adapter.setComplaintList(complaints),
                e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    /** Function to Setup Navigation Drawer Clicks */
    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(AdminComplaintListActivity.this, MainActivity.class));
            } else if (id == R.id.nav_view_bill) {
                startActivity(new Intent(AdminComplaintListActivity.this, ElectricityBill.class));
            } else if (id == R.id.nav_lodge_complaint) {
                startActivity(new Intent(AdminComplaintListActivity.this, LodgeComplaint.class));
            } else if (id == R.id.nav_generate_bill) {
                startActivity(new Intent(AdminComplaintListActivity.this, BillGeneration.class));
            } else if (id == R.id.nav_user_loyalty_points) {
                startActivity(new Intent(AdminComplaintListActivity.this, LoyaltyPoints.class));
            } else if (id == R.id.nav_complaint_status) {
                startActivity(new Intent(AdminComplaintListActivity.this, ComplaintStatus.class));
            } else if (id == R.id.nav_logout) {
                Toast.makeText(AdminComplaintListActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_admin_complaint) {
                startActivity(new Intent(AdminComplaintListActivity.this, AdminComplaintListActivity.class));
            } else if (id == R.id.nav_tech_complaint) {
                startActivity(new Intent(AdminComplaintListActivity.this, TechnicianComplaintListActivity.class));
            } else if (id == R.id.nav_user_complaints_list) {
                startActivity(new Intent(AdminComplaintListActivity.this, UserComplaintHistoryActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /** Function to Hide Navigation Menu Items Based on User Role */
    private void hideMenuItemsForRoles() {
        Menu menu = navigationView.getMenu();

        if (!GlobalVariable.userRole.equals("Reader")) {
            menu.findItem(R.id.nav_generate_bill).setVisible(false);
            menu.findItem(R.id.nav_add_biill).setVisible(false);
        }

        if (!GlobalVariable.userRole.equals("User")) {
            menu.findItem(R.id.nav_user_loyalty_points).setVisible(false);
            menu.findItem(R.id.nav_user_complaints_list).setVisible(false);
        }

        if (!GlobalVariable.userRole.equals("Admin")) {
            menu.findItem(R.id.nav_admin_complaint).setVisible(false);
        }

        if (!GlobalVariable.userRole.equals("Technician")) {
            menu.findItem(R.id.nav_tech_complaint).setVisible(false);
        }
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

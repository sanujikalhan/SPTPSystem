package com.example.sptm_systerm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private FirebaseService firebaseService;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseService = new FirebaseService();

        // 🔹 Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 🔹 Enable Custom Navigation Icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set Custom Drawer Icon
        Drawable drawable = getResources().getDrawable(R.drawable.menu);  // Use your own drawable
        Drawable resizedDrawable = resizeDrawable(drawable,  24, 24);  // Resize if needed
        getSupportActionBar().setHomeAsUpIndicator(resizedDrawable);

        // 🔹 Initialize Drawer Layout & Navigation View
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // 🔹 Load User Data in Header
        setupNavigationHeader();

        // 🔹 Handle Navigation Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_view_bill) {
                startActivity(new Intent(MainActivity.this, ElectricityBill.class));
            } else if (id == R.id.nav_lodge_complaint) {
                startActivity(new Intent(MainActivity.this, LodgeComplaint.class));
            } else if (id == R.id.nav_generate_bill) {
                startActivity(new Intent(MainActivity.this, BillGeneration.class));
            } else if (id == R.id.nav_user_loyalty_points) {
                if (!GlobalVariable.userRole.equals("User")) {
                    startActivity(new Intent(MainActivity.this, LoyaltyConfigActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoyaltyPoints.class));
                }
            } else if (id == R.id.nav_complaint_status) {
                startActivity(new Intent(MainActivity.this, ComplaintStatus.class));
            } else if (id == R.id.nav_logout) {
                Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_add_biill) {
                startActivity(new Intent(MainActivity.this, AddBill.class));
            } else if (id == R.id.nav_admin_complaint) {
                startActivity(new Intent(MainActivity.this, AdminComplaintListActivity.class));
            } else if (id == R.id.nav_tech_complaint) {
                startActivity(new Intent(MainActivity.this, TechnicianComplaintListActivity.class));
            } else if (id == R.id.nav_user_complaints_list) {
                startActivity(new Intent(MainActivity.this, UserComplaintHistoryActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 🔹 Initialize RecyclerView for User List
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
        hideMenuItemsForRoles();
    }

    // 🔹 Load Users into RecyclerView
    private void loadUsers() {
        firebaseService.getAllUsers(new OnSuccessListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                userAdapter = new UserAdapter(MainActivity.this, users);
                recyclerViewUsers.setAdapter(userAdapter);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firebase", "Failed to get users", e);
                Toast.makeText(MainActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
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
    // 🔹 Setup User Info in Navigation Header
    private void setupNavigationHeader() {
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = headerView.findViewById(R.id.nav_header_title);
            TextView navEmail = headerView.findViewById(R.id.nav_header_email);

            firebaseService.getUserRole(
                    role -> {
                        GlobalVariable.userRole = role;
                        Log.d("User Role", "Role: " + role);
                        navUsername.setText(GlobalVariable.subscriptionNo);
                        navEmail.setText(GlobalVariable.userRole);
                    },
                    e -> {
                        Log.e("NavigationView", "Error getting user role", e);
                        Toast.makeText(MainActivity.this, "Failed to get user role", Toast.LENGTH_SHORT).show();
                    }
            );
        } else {
            Log.e("NavigationView", "NavigationView is null! Check XML layout.");
        }
    }

    // 🔹 Open Drawer when Custom Toggle is Clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {  // Custom toggle click event
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 🔹 Handle Back Press to Close Drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // 🔹 Resize Custom Drawer Icon
    private Drawable resizeDrawable(Drawable image, int width, int height) {
        if (image instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            return new BitmapDrawable(getResources(), resizedBitmap);
        } else {
            return image; // Return original if not BitmapDrawable
        }
    }
}

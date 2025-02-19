package com.example.sptm_systerm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        setContentView(R.layout.activity_main);

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Setup ActionBar Toggle
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open, // String resource for "open" description
                R.string.close // String resource for "close" description
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Enable the "up" button (hamburger icon) in the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle Navigation Item Clicks
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int itemId = item.getItemId();
//                if (itemId == R.id.nav_home) {
//                     Intent intent = new Intent(MainActivity.this, Home.class);
//                     startActivity(intent);
//                } else if (itemId == R.id.nav_profile) {
//                    // Handle Profile click
//                    // Example: Start a new activity or fragment
//                    // Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//                    // startActivity(intent);
//                } else if (itemId == R.id.nav_settings) {
//                    // Handle Settings click
//                    // Example: Start a new activity or fragment
//                    // Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                    // startActivity(intent);
//                } else if (itemId == R.id.nav_logout) {
//                    // Handle Logout click
//                    // Example: Perform logout logic
//                    // finish(); // Close the current activity
//                }
//
//                // Close the drawer after handling the click
//                drawerLayout.closeDrawers();
//                return true;
//            }
//        });
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        // Handle ActionBar toggle clicks
//        if (toggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
    }
}
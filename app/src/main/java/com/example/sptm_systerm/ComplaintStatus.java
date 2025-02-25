package com.example.sptm_systerm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ComplaintStatus extends AppCompatActivity implements OnMapReadyCallback {

    private EditText subscriptionNumber;
    private DatePicker datePickerText;
    private Button openMapButton;
    private MapView mapView3;
    private EditText complaintText;
    private Button submitComplaintBtn;

    // For the map
    private GoogleMap googleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    // For location
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_REQUEST_CODE = 1001;

    // Date & location variables
    private int selectedDay, selectedMonth, selectedYear;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    // Firebase
    private FirebaseService firebaseService;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        // Initialize Firestore & FirebaseService
        db = FirebaseFirestore.getInstance();
        firebaseService = new FirebaseService();

        // Initialize FusedLocationProviderClient for location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Find views
        subscriptionNumber = findViewById(R.id.subscriptionNumber);
        datePickerText = findViewById(R.id.datePickerText);
        openMapButton = findViewById(R.id.openMapButton);
        mapView3 = findViewById(R.id.mapView3);
        complaintText = findViewById(R.id.complaintText);
        submitComplaintBtn = findViewById(R.id.submitComplaintBtn);

        // Initialize MapView
        initMapView(savedInstanceState);

        // Set current date in DatePicker & store the selected date
        Calendar calendar = Calendar.getInstance();
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);

        datePickerText.init(selectedYear, selectedMonth, selectedDay,
                (view, year, monthOfYear, dayOfMonth) -> {
                    selectedDay = dayOfMonth;
                    selectedMonth = monthOfYear;
                    selectedYear = year;
                });

        // Check location permission when "Select Location" button is clicked
        openMapButton.setOnClickListener(v -> checkLocationPermission());

        // Submit complaint
        submitComplaintBtn.setOnClickListener(v -> {
            String subNo = subscriptionNumber.getText().toString().trim();
            String complaintMsg = complaintText.getText().toString().trim();

            if (subNo.isEmpty() || complaintMsg.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Format the selected date (day, month, year) into a string
            String formattedDate = selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear;

            // Combine lat & lon into a single string
            String locationString = selectedLatitude + "," + selectedLongitude;

            // Create a Complaint object with matching fields
            Complaint complaint = new Complaint(
                    subNo,              // subscriptionNo
                    complaintMsg,       // complaint text
                    formattedDate,      // date
                    locationString,     // location
                    "Pending",          // technicianStatus
                    "Not Fixed yet"     // userStatus
            );

            // Add the complaint to Firestore
            firebaseService.addComplaint(
                    complaint,
                    documentReference -> {
                        Toast.makeText(this,
                                "Complaint added with ID: " + documentReference.getId(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    e -> {
                        Toast.makeText(this,
                                "Error adding complaint: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
            );
        });
    }

    /** Check location permissions, request if not granted. */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
        } else {
            // If permission is already granted, enable MyLocation on the map
            enableMyLocationOnMap();
        }
    }

    /** Initialize the MapView and request async map callback. */
    private void initMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView3.onCreate(mapViewBundle);
        mapView3.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapsInitializer.initialize(this);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // If permissions are granted, enable MyLocation and move camera to current location
        checkLocationPermission();

        // Listen for long-clicks on the map; user can tap to select a location
        googleMap.setOnMapLongClickListener(latLng -> {
            // Clear any previous markers
            googleMap.clear();

            // Place a marker at the selected location
            googleMap.addMarker(new MarkerOptions().position(latLng));

            // Update selectedLatitude, selectedLongitude
            selectedLatitude = latLng.latitude;
            selectedLongitude = latLng.longitude;

            Toast.makeText(this,
                    "Selected location: " + selectedLatitude + ", " + selectedLongitude,
                    Toast.LENGTH_SHORT).show();
        });
    }

    /** Enable the MyLocation layer and move camera to current location if available. */
    private void enableMyLocationOnMap() {
        if (googleMap == null) return;

        // Check permission again to be safe
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return; // Permission not granted
        }

        // Show the user's blue dot on the map
        googleMap.setMyLocationEnabled(true);

        // Get the last known location and move the camera there
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        selectedLatitude = location.getLatitude();
                        selectedLongitude = location.getLongitude();

                        LatLng userLatLng = new LatLng(selectedLatitude, selectedLongitude);

                        // Move the camera to the user's current location
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to get current location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Forward MapView lifecycle events
    @Override
    public void onResume() {
        super.onResume();
        mapView3.onResume();
    }

    @Override
    public void onPause() {
        mapView3.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView3.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView3.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView3.onSaveInstanceState(mapViewBundle);
        super.onSaveInstanceState(outState);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            // Check if permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationOnMap();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

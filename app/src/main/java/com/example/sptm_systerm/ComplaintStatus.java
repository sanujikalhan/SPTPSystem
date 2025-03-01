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

    private EditText subscriptionNumber, complaintText;
    private DatePicker datePickerText;
    private Button openMapButton, submitComplaintBtn;
    private MapView mapView3;
    private GoogleMap googleMap;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_REQUEST_CODE = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    private FirebaseService firebaseService;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);

        db = FirebaseFirestore.getInstance();
        firebaseService = new FirebaseService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Views
        subscriptionNumber = findViewById(R.id.subscriptionNumber);
        datePickerText = findViewById(R.id.datePickerText);
        openMapButton = findViewById(R.id.openMapButton);
        mapView3 = findViewById(R.id.mapView3);
        complaintText = findViewById(R.id.complaintText);
        submitComplaintBtn = findViewById(R.id.submitComplaintBtn);

        // Initialize MapView
        initMapView(savedInstanceState);

        // Initialize DatePicker
        Calendar calendar = Calendar.getInstance();
        datePickerText.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Date Selection Handled
                });

        // Location selection
        openMapButton.setOnClickListener(v -> checkLocationPermission());

        // Submit complaint
        submitComplaintBtn.setOnClickListener(v -> submitComplaint());
    }

    /** Checks location permissions before enabling location selection. */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE
            );
        } else {
            enableMyLocationOnMap();
        }
    }

    /** Initializes the map and assigns the async callback. */
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
        checkLocationPermission();

        // User taps on the map to set location
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            selectedLatitude = latLng.latitude;
            selectedLongitude = latLng.longitude;
            Toast.makeText(this, "Location Selected!", Toast.LENGTH_SHORT).show();
        });
    }

    /** Enables MyLocation feature on the map if permissions are granted. */
    private void enableMyLocationOnMap() {
        if (googleMap == null) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                selectedLatitude = location.getLatitude();
                selectedLongitude = location.getLongitude();
                LatLng userLatLng = new LatLng(selectedLatitude, selectedLongitude);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
            }
        });
    }

    /** Submits the complaint with the selected details. */
    private void submitComplaint() {
        String subNo = subscriptionNumber.getText().toString().trim();
        String complaintMsg = complaintText.getText().toString().trim();

        if (subNo.isEmpty() || complaintMsg.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedDate = datePickerText.getDayOfMonth() + "-" +
                (datePickerText.getMonth() + 1) + "-" +
                datePickerText.getYear();
        String locationString = selectedLatitude + "," + selectedLongitude;

        Complaint complaint = new Complaint(subNo, complaintMsg, formattedDate, locationString, "Pending", "Not Fixed yet");

        firebaseService.addComplaint(complaint,
                documentReference -> {
                    Toast.makeText(this, "Complaint added!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // Handle lifecycle events for MapView
    @Override public void onResume() { super.onResume(); mapView3.onResume(); }
    @Override public void onPause() { mapView3.onPause(); super.onPause(); }
    @Override public void onDestroy() { mapView3.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView3.onLowMemory(); }

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

    /** Handles location permission request results. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationOnMap();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

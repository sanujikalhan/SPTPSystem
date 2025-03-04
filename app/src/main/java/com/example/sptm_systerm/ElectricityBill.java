package com.example.sptm_systerm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElectricityBill extends AppCompatActivity {
    private Spinner monthSpinner;
    List<MeterReading> meterReadings = new ArrayList<>();
    private TextView viewBillText;
    private FirebaseFirestore db;
    private List<Map<String, Object>> readingsList;
    private List<String> datesList;
    private FirebaseService firebaseService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_electricity_bill);
        firebaseService = new FirebaseService();
        monthSpinner = findViewById(R.id.monthSpinner);
        viewBillText = findViewById(R.id.viewBillText);
        firebaseService.subscribeToMeterReadings(GlobalVariable.subscriptionNo,
                readingsList -> {
                    if (readingsList == null || readingsList.isEmpty()) {
                        Toast.makeText(this, "No meter readings found for this subscription!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    datesList= new ArrayList<String>();
                    for (Map<String, Object> reading : readingsList) {
                        String date = reading.get("date").toString();
                        Object value = reading.get("value");

                        if (value == null) {
                            Toast.makeText(this, "Invalid reading value detected!", Toast.LENGTH_SHORT).show();
                            return;

                        }

                        meterReadings.add(new MeterReading(date, value));
                        datesList.add(date);
                    }

                    // Store meter readings globally and navigate to Bill Generation
                    GlobalVariable.meterReadings = meterReadings;
                    populateSpinner(datesList);

                },
                e -> {
                    Log.e("FirebaseError", "Failed to retrieve meter readings", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
        );

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (meterReadings == null || meterReadings.isEmpty()) {
                    Toast.makeText(ElectricityBill.this, "No meter readings available!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Set the bill text based on the selected position
                viewBillText.setText(meterReadings.get(position).getValue().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle case when nothing is selected
            }
        });


    }

    private void populateSpinner(List<String> dates) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_item, dates);
        monthSpinner.setAdapter(adapter);


    }
}
package com.example.sptm_systerm;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill); // Link to XML UI

        // Initialize UI components
        subscriptionNumber = findViewById(R.id.subscriptionNumber);
        datePickerText = findViewById(R.id.datePickerText);
        submitBtn = findViewById(R.id.submitBtn);

        // Set OnClickListener for the date picker (TextView)
        datePickerText.setOnClickListener(v -> showDatePickerDialog());

        // Set OnClickListener for Submit button
        submitBtn.setOnClickListener(v -> {
            if (GlobalVariable.userRole.equals("Reader")) {
                if (selectedDate.isEmpty()) {
                    Toast.makeText(this, "Please pick a date", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (subscriptionNumber.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Please pick a date", Toast.LENGTH_SHORT).show();
                    return;
                }
                GlobalVariable.pickedDate = selectedDate;
                GlobalVariable.subscriptionNo = subscriptionNumber.getText().toString().trim();
                submitBill();
                Intent intent = new Intent(AddBill.this, BillGeneration.class);
                startActivity(intent);
            } else if (GlobalVariable.userRole.equals("User")) {
                submitBill();
            }
        });
        firebaseService = new FirebaseService();
    }

    // Function to show DatePicker Dialog
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
        if (GlobalVariable.userRole.equals("Reader")) {
            // This ensures the user cannot pick a date before "today"
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    // Function to handle the Submit button click
    private void submitBill() {
        String subscriptionNo = subscriptionNumber.getText().toString().trim();

        if (subscriptionNo.isEmpty()) {
            Toast.makeText(this, "Please enter Subscription Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please pick a date", Toast.LENGTH_SHORT).show();
            return;
        }
        GlobalVariable.pickedDate = selectedDate;

        firebaseService.subscribeToMeterReadings(subscriptionNo,
                readingsList -> {
                    List<MeterReading> meterReadings = new ArrayList<>();

                    // Convert each map entry to a MeterReading object
                    for (Map<String, Object> reading : readingsList) {
                        String date = reading.get("date").toString();
                        Object value = reading.get("value");
                        meterReadings.add(new MeterReading(date, value));
                    }

                    // Convert the list to an array
                    MeterReading[] meterReadingArray = meterReadings.toArray(new MeterReading[0]);

                    // You can now use meterReadingArray as needed (for example, log the results)
                    for (MeterReading mr : meterReadingArray) {
                        Log.d("MeterReading", mr.toString());
                    }
                    GlobalVariable.meterReadings = meterReadings;
                    // Redirect to another activity
                    Intent intent = new Intent(AddBill.this, BillGeneration.class);
                    startActivity(intent);
                },
                e -> {
                    // Handle error here
                    Toast.makeText(AddBill.this, "Failed to retrieve meter readings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}
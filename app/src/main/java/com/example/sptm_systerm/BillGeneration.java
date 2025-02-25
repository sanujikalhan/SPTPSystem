package com.example.sptm_systerm;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillGeneration extends AppCompatActivity {
    private TextView lastMonthReading, newLoyaltyPoints;
    private EditText currentMonthReading;
    private Button generateBillButton;
    private  FirebaseService firebaseService;
    private double currentMonthBill = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bill_generation);

        lastMonthReading = findViewById(R.id.last);
        currentMonthReading = findViewById(R.id.current);
        newLoyaltyPoints = findViewById(R.id.points);
        generateBillButton = findViewById(R.id.generateBillButton);
        firebaseService = new FirebaseService();

        currentMonthReading.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed during text changes
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentText = s.toString();
                String lastText = lastMonthReading.getText().toString();
                if (!currentText.isEmpty() && !lastText.equals("N/A") && !lastText.isEmpty()) {
                    try {
                        double currentVal = Double.parseDouble(currentText);
                        double lastVal = Double.parseDouble(lastText);
                        double loyaltyPoints = LoyaltyCalculator.calculateLoyaltyPoints(lastVal, currentVal);
                        newLoyaltyPoints.setText(String.valueOf(loyaltyPoints));
                    } catch (NumberFormatException e) {
                        newLoyaltyPoints.setText("");
                    }
                } else {
                    newLoyaltyPoints.setText("");
                }
            }
        });

        // Assume GlobalVariable.pickedDate is a Date object representing the user's chosen date
        Date pickedDate = parseDate(GlobalVariable.pickedDate);
        if (GlobalVariable.userRole.equals("User")) {
            // Get the current month's reading based on the picked date
            MeterReading currentReading = getReadingForMonth(GlobalVariable.meterReadings, pickedDate);
            if (currentReading != null) {
                currentMonthBill = Double.parseDouble(currentReading.getValue().toString());
                currentMonthReading.setText(String.valueOf(currentMonthBill));
            } else {
                currentMonthReading.setText("");
            }
        }
        // Get the previous month's reading based on the picked date
        Date lastMonthDate = getPreviousMonth(pickedDate);
        MeterReading previousReading = getReadingForMonth(GlobalVariable.meterReadings, lastMonthDate);
        if (previousReading != null) {
            double lastMonthBill = Double.parseDouble(previousReading.getValue().toString());
            lastMonthReading.setText(String.valueOf(lastMonthBill));
            double loyaltyPoints = LoyaltyCalculator.calculateLoyaltyPoints(lastMonthBill, currentMonthBill);
            newLoyaltyPoints.setText(String.valueOf(loyaltyPoints));
        } else {
            lastMonthReading.setText("N/A");
            newLoyaltyPoints.setText("");
        }

        generateBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalVariable.userRole.equals("Reader")) {
                    firebaseService.addMeterReading(
                            GlobalVariable.subscriptionNo,
                            convertToPreviousFormat(GlobalVariable.pickedDate),
                            currentMonthReading.getText().toString(),
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Meter reading added successfully", Toast.LENGTH_SHORT).show();
                                    // Redirect to another activity
                                    Intent intent = new Intent(BillGeneration.this, AddBill.class);
                                    startActivity(intent);
                                }
                            },
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Failed to add meter reading: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                }
                else if (GlobalVariable.userRole.equals("User")) {
                    Toast.makeText(BillGeneration.this, "Bill Generated!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Returns the MeterReading for the month and year corresponding to the targetDate.
     * If multiple readings exist, the one with the latest date is returned.
     */
    private MeterReading getReadingForMonth(List<MeterReading> readings, Date targetDate) {
        if (readings == null || targetDate == null) return null;
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(targetDate);
        int targetMonth = targetCal.get(Calendar.MONTH);
        int targetYear = targetCal.get(Calendar.YEAR);

        MeterReading latest = null;
        for (MeterReading reading : readings) {
            Date readingDate = parseDateNew(reading.getDate());
            if (readingDate == null) continue; // Skip if date parsing fails

            Calendar readingCal = Calendar.getInstance();
            readingCal.setTime(readingDate);
            int readingMonth = readingCal.get(Calendar.MONTH);
            int readingYear = readingCal.get(Calendar.YEAR);

            if (readingMonth == targetMonth && readingYear == targetYear) {
                if (latest == null || readingDate.after(parseDate(latest.getDate()))) {
                    latest = reading;
                }
            }
        }
        return latest;
    }

    /**
     * Returns a Date representing the same day of the previous month.
     */
    private Date getPreviousMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    /**
     * Parses a date String into a Date object.
     * Adjust the date format ("yyyy-MM-dd") if your date strings use a different pattern.
     */
    private Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    private Date parseDateNew(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String convertToPreviousFormat(String dateStr) {
        // We will try parsing with two patterns:
        //  - "dd/MM/yyyy" (handles leading zeros)
        //  - "d/M/yyyy"   (handles no leading zeros)
        // Then we will format to "dd-MM-yyyy" (with leading zeros).
        SimpleDateFormat parseFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat parseFormat2 = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        SimpleDateFormat desiredFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        Date date = null;
        try {
            // First try to parse with dd/MM/yyyy
            date = parseFormat1.parse(dateStr);
        } catch (ParseException e) {
            // If that fails, try d/M/yyyy
            try {
                date = parseFormat2.parse(dateStr);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }

        // If parsing worked, reformat to dd-MM-yyyy
        if (date != null) {
            return desiredFormat.format(date);
        } else {
            // Fallback if we can't parse
            return dateStr;
        }
    }

}

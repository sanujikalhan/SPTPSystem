package com.example.sptm_systerm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoyaltyConfigActivity extends AppCompatActivity {

    private EditText editTarget, editOnePercent, editFivePercent;
    private Button btnSave, btnCalculate;
    private TextView txtLoyaltyResult;
    private SharedPreferences sharedPreferences;
    private FirebaseService firebaseService;
    int currentMonthValue = 0;
    int lastMonthValue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loyalty_config);

        // Initialize views
        editTarget = findViewById(R.id.editLoyaltyTarget);
        editOnePercent = findViewById(R.id.editOnePercent);
        editFivePercent = findViewById(R.id.editFivePercent);
        btnSave = findViewById(R.id.btnSaveLoyalty);
        btnCalculate = findViewById(R.id.btnCalculateLoyalty);
        txtLoyaltyResult = findViewById(R.id.txtLoyaltyResult);
        firebaseService = new FirebaseService();

        sharedPreferences = getSharedPreferences("LoyaltyPrefs", Context.MODE_PRIVATE);

        // Load saved values
        loadSavedValues();

        // Save new settings
        btnSave.setOnClickListener(v -> saveLoyaltySettings());

        // Calculate loyalty points based on input
        btnCalculate.setOnClickListener(v -> calculateLoyaltyPoints());

        // Call Firebase to retrieve meter readings
        firebaseService.subscribeToMeterReadings(GlobalVariable.subscriptionNo,
                readingsList -> {
                    if (readingsList == null || readingsList.isEmpty()) {
                        Toast.makeText(LoyaltyConfigActivity.this, "No meter readings found for this subscription!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<MeterReading> meterReadings = new ArrayList<>();
                    for (Map<String, Object> reading : readingsList) {
                        String date = reading.get("date").toString();
                        Object value = reading.get("value");

                        if (value == null) {
                            Toast.makeText(LoyaltyConfigActivity.this, "Invalid reading value detected!", Toast.LENGTH_SHORT).show();
                            return;

                        }

                        meterReadings.add(new MeterReading(date, value));
                    }

                    // Store meter readings globally and navigate to Bill Generation
                    GlobalVariable.meterReadings = meterReadings;
                    getCurrentAndLastMonthValues(meterReadings,new Date() );
                },
                e -> {
                    Log.e("FirebaseError", "Failed to retrieve meter readings", e);
                    Toast.makeText(LoyaltyConfigActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
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
    private Date parseDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getCurrentAndLastMonthValues(List<MeterReading> readings, Date targetDate) {
        if (readings == null || targetDate == null) {
            return;
        }

        Calendar targetCal = Calendar.getInstance();
        targetCal.setTime(targetDate);
        int targetMonth = targetCal.get(Calendar.MONTH);
        int targetYear = targetCal.get(Calendar.YEAR);

        // Calculate last month's date
        Calendar lastMonthCal = (Calendar) targetCal.clone();
        lastMonthCal.add(Calendar.MONTH, -1);
        int lastMonth = lastMonthCal.get(Calendar.MONTH);
        int lastYear = lastMonthCal.get(Calendar.YEAR);

        MeterReading currentMonthReading = null;
        MeterReading lastMonthReading = null;

        for (MeterReading reading : readings) {
            Date readingDate = parseDateNew(reading.getDate());
            if (readingDate == null) continue; // Skip if date parsing fails

            Calendar readingCal = Calendar.getInstance();
            readingCal.setTime(readingDate);
            int readingMonth = readingCal.get(Calendar.MONTH);
            int readingYear = readingCal.get(Calendar.YEAR);

            // Check for current month's reading
            if (readingMonth == targetMonth && readingYear == targetYear) {
                if (currentMonthReading == null || readingDate.after(parseDateNew(currentMonthReading.getDate()))) {
                    currentMonthReading = reading;
                }
            }

            // Check for last month's reading
            if (readingMonth == lastMonth && readingYear == lastYear) {
                if (lastMonthReading == null || readingDate.after(parseDateNew(lastMonthReading.getDate()))) {
                    lastMonthReading = reading;
                }
            }
        }

        // Extract values (default to 0 if null)
        int currentMonthValue = (currentMonthReading != null && currentMonthReading.getValue() instanceof Number)
                ? ((Number) currentMonthReading.getValue()).intValue() : 0;
        int lastMonthValue = (lastMonthReading != null && lastMonthReading.getValue() instanceof Number)
                ? ((Number) lastMonthReading.getValue()).intValue() : 0;

        // Print or use these variables as needed
        System.out.println("Current Month Value: " + currentMonthValue);
        System.out.println("Last Month Value: " + lastMonthValue);
    }

    private void saveLoyaltySettings() {
        String target = editTarget.getText().toString();
        String onePercent = editOnePercent.getText().toString();
        String fivePercent = editFivePercent.getText().toString();

        if (TextUtils.isEmpty(target) || TextUtils.isEmpty(onePercent) || TextUtils.isEmpty(fivePercent)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("loyaltyTarget", Float.parseFloat(target));
        editor.putFloat("onePercentThreshold", Float.parseFloat(onePercent));
        editor.putFloat("fivePercentThreshold", Float.parseFloat(fivePercent));
        editor.apply();

        Toast.makeText(this, "Loyalty settings saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedValues() {
        float target = sharedPreferences.getFloat("loyaltyTarget", 0);
        float onePercent = sharedPreferences.getFloat("onePercentThreshold", 1);
        float fivePercent = sharedPreferences.getFloat("fivePercentThreshold", 5);

        editTarget.setText(String.valueOf(target));
        editOnePercent.setText(String.valueOf(onePercent));
        editFivePercent.setText(String.valueOf(fivePercent));
    }

    private void calculateLoyaltyPoints() {
        float target = sharedPreferences.getFloat("loyaltyTarget", 0);
        float onePercent = sharedPreferences.getFloat("onePercentThreshold", 1);
        float fivePercent = sharedPreferences.getFloat("fivePercentThreshold", 5);

        double lastMonthBill = lastMonthValue; // Simulating bill values
        double currentMonthBill = currentMonthValue;

        double loyaltyPoints = LoyaltyCalculator.calculateLoyaltyPoints(lastMonthBill, currentMonthBill);

        String result = "Loyalty Points Earned: " + loyaltyPoints + "\n"
                + "1% Threshold: " + onePercent + "\n"
                + "5% Threshold: " + fivePercent;
        txtLoyaltyResult.setText(result);
    }
}

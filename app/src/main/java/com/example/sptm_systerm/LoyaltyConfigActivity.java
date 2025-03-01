package com.example.sptm_systerm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoyaltyConfigActivity extends AppCompatActivity {

    private EditText editTarget, editOnePercent, editFivePercent;
    private Button btnSave, btnCalculate;
    private TextView txtLoyaltyResult;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences("LoyaltyPrefs", Context.MODE_PRIVATE);

        // Load saved values
        loadSavedValues();

        // Save new settings
        btnSave.setOnClickListener(v -> saveLoyaltySettings());

        // Calculate loyalty points based on input
        btnCalculate.setOnClickListener(v -> calculateLoyaltyPoints());
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

        double lastMonthBill = 1000; // Simulating bill values
        double currentMonthBill = 800;

        double loyaltyPoints = LoyaltyCalculator.calculateLoyaltyPoints(lastMonthBill, currentMonthBill);

        String result = "Loyalty Points Earned: " + loyaltyPoints + "\n"
                + "1% Threshold: " + onePercent + "\n"
                + "5% Threshold: " + fivePercent;
        txtLoyaltyResult.setText(result);
    }
}

package com.example.sptm_systerm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BillGeneration extends AppCompatActivity {
    private TextView lastMonthReading, currentMonthReading, newLoyaltyPoints;
    private Button generateBillButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bill_generation);
        lastMonthReading = findViewById(R.id.lastMonthReading);
        currentMonthReading = findViewById(R.id.currentMonthReading);
        newLoyaltyPoints = findViewById(R.id.newLoyaltyPoints);
        generateBillButton = findViewById(R.id.generateBillButton);

        generateBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate bill logic here
                Toast.makeText(BillGeneration.this, "Bill Generated!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
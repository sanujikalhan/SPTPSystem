package com.example.sptm_systerm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Payment extends AppCompatActivity {
    private CheckBox visaCard, masterCard, sampathViswa, mobileWallet;
    private EditText payAmount;
    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        visaCard = findViewById(R.id.visaCard);
        masterCard = findViewById(R.id.masterCard);
        sampathViswa = findViewById(R.id.sampathViswa);
        mobileWallet = findViewById(R.id.mobileWallet);
        payAmount = findViewById(R.id.payAmount);
        payButton = findViewById(R.id.payButton);

        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the payment amount
                String amount = payAmount.getText().toString();
                if (amount.isEmpty()) {
                    Toast.makeText(Payment.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if at least one payment method is selected
                if (visaCard.isChecked() || masterCard.isChecked() || sampathViswa.isChecked() || mobileWallet.isChecked()) {
                    // Proceed with payment (this part can be customized)
                    Toast.makeText(Payment.this, "Payment of " + amount + " will be processed", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Payment.this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
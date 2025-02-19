package com.example.sptm_systerm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SubscriptionMenu extends AppCompatActivity {
    private EditText subscriptionNumber;
    private DatePicker datePicker;
    private Button submitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subscription_menu);
        subscriptionNumber = findViewById(R.id.subscriptionNumber);
        datePicker = findViewById(R.id.datePicker);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subscription = subscriptionNumber.getText().toString();
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();

                Toast.makeText(SubscriptionMenu.this, "Subscription: " + subscription + " Date: " + day + "/" + month + "/" + year, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
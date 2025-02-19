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

public class Rewards extends AppCompatActivity {
    private Button updateRewardsButton, redeemRewardsButton;
    private TextView currentRewards, nextTarget, loyaltyPoints;
    private int earnedRewards = 0;
    private int targetRewards = 100;
    private int points = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rewards);
        updateRewardsButton = findViewById(R.id.updateRewardsButton);
        redeemRewardsButton = findViewById(R.id.redeemRewardsButton);
        currentRewards = findViewById(R.id.currentRewards);
        nextTarget = findViewById(R.id.nextTarget);
        loyaltyPoints = findViewById(R.id.loyaltyPoints);

        // Update rewards button click
        updateRewardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                earnedRewards += 10; // Increment rewards (for example)
                points += 10; // Increment loyalty points
                updateUI();
                Toast.makeText(Rewards.this, "Rewards updated!", Toast.LENGTH_SHORT).show();
            }
        });

        // Redeem rewards button click
        redeemRewardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (earnedRewards >= targetRewards) {
                    earnedRewards -= targetRewards;
                    points += targetRewards;
                    updateUI();
                    Toast.makeText(Rewards.this, "Rewards redeemed!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Rewards.this, "Not enough rewards to redeem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to update the UI with the current rewards and loyalty points
    private void updateUI() {
        currentRewards.setText("Current earned rewards: " + earnedRewards);
        loyaltyPoints.setText("Loyalty points: " + points);
    }
}
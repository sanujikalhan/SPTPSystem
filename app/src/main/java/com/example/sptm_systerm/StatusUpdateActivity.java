package com.example.sptm_systerm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

public class StatusUpdateActivity extends AppCompatActivity {

    private TextView complaintTextView, locationTextView, dateTextView;
    private RadioGroup statusRadioGroup;
    private RadioButton attendRadioButton, pendingRadioButton, doneRadioButton;
    private Button submitButton;

    private FirebaseFirestore db;
    private Complaint currentComplaint;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        db = FirebaseFirestore.getInstance();
        firebaseService = new FirebaseService();

        // Initialize UI elements
        complaintTextView = findViewById(R.id.complaintTextView);
        locationTextView = findViewById(R.id.locationTextView);
        dateTextView = findViewById(R.id.dateTextView);
        statusRadioGroup = findViewById(R.id.statusRadioGroup);
        attendRadioButton = findViewById(R.id.attendRadioButton);
        pendingRadioButton = findViewById(R.id.pendingRadioButton);
        doneRadioButton = findViewById(R.id.doneRadioButton);
        submitButton = findViewById(R.id.submitButton);

        // Retrieve complaint object from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("complaint")) {
            Serializable complaintSerializable = intent.getSerializableExtra("complaint");
            if (complaintSerializable instanceof Complaint) {
                currentComplaint = (Complaint) complaintSerializable;
                populateUI(currentComplaint);
            } else {
                Toast.makeText(this, "Invalid complaint data!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        // Set click listener for submitting status update
        submitButton.setOnClickListener(v -> confirmUpdateStatus());
    }

    private void populateUI(Complaint complaint) {
        if (complaint != null) {
            complaintTextView.setText("Complaint: " + complaint.getComplaint());
            locationTextView.setText("Location: " + complaint.getLocation());
            dateTextView.setText("Date: " + complaint.getDate());

            // Set existing status
            String status = complaint.getTechnicianStatus();
            if ("Attend".equals(status)) {
                attendRadioButton.setChecked(true);
            } else if ("Pending".equals(status)) {
                pendingRadioButton.setChecked(true);
            } else if ("Done".equals(status)) {
                doneRadioButton.setChecked(true);
            }
        }
    }

    private void confirmUpdateStatus() {
        if (currentComplaint == null) {
            Toast.makeText(this, "No complaint selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected status
        int selectedId = statusRadioGroup.getCheckedRadioButtonId();
        String newStatus = getSelectedStatus(selectedId);

        if (newStatus.isEmpty()) {
            Toast.makeText(this, "Please select a status!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog before updating
        new AlertDialog.Builder(this)
                .setTitle("Confirm Status Update")
                .setMessage("Are you sure you want to update the complaint status to " + newStatus + "?")
                .setPositiveButton("Yes", (dialog, which) -> updateComplaintStatus(newStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getSelectedStatus(int selectedId) {
        if (selectedId == R.id.attendRadioButton) return "Attend";
        else if (selectedId == R.id.pendingRadioButton) return "Pending";
        else if (selectedId == R.id.doneRadioButton) return "Done";
        return "";
    }

    public void updateComplaintStatus(String complaintId, String newTechnicianStatus, String newUserStatus,
                                      OnSuccessListener<Void> successListener,
                                      OnFailureListener failureListener) {
        firebaseService.updateComplaintStatus(complaintId, newTechnicianStatus, newUserStatus, successListener, failureListener);
    }

    private void updateComplaintStatus(String newStatus) {
        firebaseService.updateComplaintStatus(currentComplaint.getDocId(), newStatus, currentComplaint.getUserStatus(),
                aVoid -> {
                    Toast.makeText(StatusUpdateActivity.this, "Status updated successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StatusUpdateActivity.this, AdminComplaintListActivity.class));
                },
                e -> {
                    Toast.makeText(StatusUpdateActivity.this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

}

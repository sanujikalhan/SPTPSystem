package com.example.sptm_systerm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LodgeComplaint extends AppCompatActivity {
    private CheckBox electricityAccountCheckBox, showOnMapCheckBox;
    private Spinner complainTypeSpinner;
    private EditText contactNumberEditText, complaintDetailsEditText;
    private Button submitComplaintButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lodge_complaint);
        electricityAccountCheckBox = findViewById(R.id.electricityAccountCheckBox);
        showOnMapCheckBox = findViewById(R.id.showOnMapCheckBox);
        complainTypeSpinner = findViewById(R.id.complainTypeSpinner);
        contactNumberEditText = findViewById(R.id.contactNumberEditText);
        complaintDetailsEditText = findViewById(R.id.complaintDetailsEditText);
        submitComplaintButton = findViewById(R.id.submitComplaintButton);

        submitComplaintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactNumber = contactNumberEditText.getText().toString();
                String complaintDetails = complaintDetailsEditText.getText().toString();
                Toast.makeText(LodgeComplaint.this, "Complaint Submitted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
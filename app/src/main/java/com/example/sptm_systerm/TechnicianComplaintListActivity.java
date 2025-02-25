package com.example.sptm_systerm;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TechnicianComplaintListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintsAdapter adapter;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_complaint_list);

        recyclerView = findViewById(R.id.techComplaintsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        firebaseService = new FirebaseService();

        // Load all complaints or only "Pending" if you prefer
        firebaseService.getAllComplaints(
                complaints -> {
                    // For instance, filter for "Pending" complaints:
                    // List<Complaint> pendingOnly = new ArrayList<>();
                    // for (Complaint c : complaints) {
                    //     if ("Pending".equals(c.getTechnicianStatus())) {
                    //         pendingOnly.add(c);
                    //     }
                    // }
                    // adapter.setComplaintList(pendingOnly);

                    // For now, show them all
                    adapter.setComplaintList(complaints);
                },
                e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}

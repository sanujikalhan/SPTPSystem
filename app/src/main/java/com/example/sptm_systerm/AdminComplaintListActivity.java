package com.example.sptm_systerm;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdminComplaintListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintsAdapter adapter;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaint_list);

        recyclerView = findViewById(R.id.adminComplaintsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        firebaseService = new FirebaseService();

        // Admin can view everything or apply advanced filtering
        firebaseService.getAllComplaints(
                complaints -> {
                    adapter.setComplaintList(complaints);
                },
                e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}

package com.example.sptm_systerm;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserComplaintListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintsAdapter adapter;
    private FirebaseService firebaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_complaint_list);

        recyclerView = findViewById(R.id.userComplaintsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        firebaseService = new FirebaseService();

        // For demonstration: fetch ALL complaints
        // If you want to filter only the user's complaints, you could do so after retrieving them.
        firebaseService.getAllComplaints(
                complaints -> {
                    // Example: Filter by user subscriptionNo or user ID if needed
                    // For now, show them all
                    adapter.setComplaintList(complaints);
                },
                e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}

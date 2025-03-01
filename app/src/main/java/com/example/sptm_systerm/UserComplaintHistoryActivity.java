package com.example.sptm_systerm;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserComplaintHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintsAdapter adapter;
    private FirebaseService firebaseService;
    private String userSubscriptionNo; // Get from logged-in user

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_complaint_history);

        recyclerView = findViewById(R.id.userComplaintsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ComplaintsAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        firebaseService = new FirebaseService();
        userSubscriptionNo = GlobalVariable.subscriptionNo; // Assume user is logged in

        loadUserComplaints();
    }

    private void loadUserComplaints() {
        firebaseService.getComplaintsForUser(userSubscriptionNo,
                complaints -> {
                    adapter.setComplaintList(complaints);
                    adapter.notifyDataSetChanged();
                },
                e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}

package com.example.sptm_systerm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class FirebaseService {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public FirebaseService() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Get the current user ID
    public String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    // Get the current user email
    public String getUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }
    // Function to create a user document in Firestore
    public void createUser(String userId, String address, String email, String firstname, String lastname,
                           String mobile, String nic, String role,
                           OnSuccessListener<Void> successListener,
                           OnFailureListener failureListener) {
        // Create a HashMap to store user data
        Map<String, Object> user = new HashMap<>();
        user.put("addresses", address);
        user.put("email", email);
        user.put("firstname", firstname);
        user.put("lastname", lastname);
        user.put("mobile", mobile);
        user.put("nic", nic);
        user.put("role", role);

        // Add user data to Firestore
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.set(user)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    // Get the user role from Firestore
    public void getUserRole(OnSuccessListener<String> successListener, OnFailureListener failureListener) {
        String userId = getUserId();
        if (userId == null) {
            failureListener.onFailure(new Exception("User not logged in"));
            return;
        }

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                successListener.onSuccess(role);
            } else {
                failureListener.onFailure(new Exception("Failed to retrieve user role"));
            }
        });

    }
}

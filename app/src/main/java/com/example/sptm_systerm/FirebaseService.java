package com.example.sptm_systerm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.SetOptions;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                GlobalVariable.userRole = role;
                GlobalVariable.subscriptionNo = task.getResult().getString("subscriptionNo");
                successListener.onSuccess(role);
            } else {
                failureListener.onFailure(new Exception("Failed to retrieve user role"));
            }
        });
    }

    public void subscribeToMeterReadings(String meterId, OnSuccessListener<List<Map<String, Object>>> successListener,
                                         OnFailureListener failureListener) {
        db.collection("meter_readings").document(meterId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        failureListener.onFailure(e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Map<String, Object> readings = documentSnapshot.getData();
                        List<Map<String, Object>> readingsList = new ArrayList<>();

                        for (Map.Entry<String, Object> entry : readings.entrySet()) {
                            Map<String, Object> reading = new HashMap<>();
                            reading.put("date", entry.getKey());
                            reading.put("value", entry.getValue());
                            readingsList.add(reading);
                        }
                        successListener.onSuccess(readingsList);
                    } else {
                        failureListener.onFailure(new Exception("No readings found"));
                    }
                });
    }
    // Retrieve all complaints from Firestore
    public void getAllComplaints(OnSuccessListener<List<Complaint>> successListener,
                                 OnFailureListener failureListener) {
        db.collection("complaints")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Complaint> complaintList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Complaint complaint = doc.toObject(Complaint.class);
                        if (complaint != null) {
                            // Store the document ID if needed
                            complaint.setDocId(doc.getId());
                            complaintList.add(complaint);
                        }
                    }
                    successListener.onSuccess(complaintList);
                })
                .addOnFailureListener(failureListener);
    }
    public void getAllUsers(OnSuccessListener<List<User>> successListener, OnFailureListener failureListener) {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> userList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getId();
                        String name = doc.getString("firstname") + " " + doc.getString("lastname");
                        String role = doc.getString("role");
                        userList.add(new User(userId, name, role));
                    }
                    successListener.onSuccess(userList);
                })
                .addOnFailureListener(failureListener);
    }

    public void addComplaint(Complaint complaint,
                             OnSuccessListener<DocumentReference> successListener,
                             OnFailureListener failureListener) {
        db.collection("complaints")
                .add(complaint)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void updateComplaintStatus(String complaintId, String newTechnicianStatus, String newUserStatus,
                                      OnSuccessListener<Void> successListener,
                                      OnFailureListener failureListener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("technicianStatus", newTechnicianStatus);
        updates.put("userStatus", newUserStatus);

        db.collection("complaints").document(complaintId)
                .update(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void getComplaintsForUser(String subscriptionNo,
                                     OnSuccessListener<List<Complaint>> successListener,
                                     OnFailureListener failureListener) {
        db.collection("complaints")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Complaint> complaintList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        if (doc.exists()) {
                            // Retrieve subscriptionNo from inside the document
                            String storedSubscriptionNo = doc.getString("subscriptionNo");
                            if (storedSubscriptionNo != null && storedSubscriptionNo.equals(subscriptionNo)) {
                                Complaint complaint = doc.toObject(Complaint.class);
                                if (complaint != null) {
                                    complaint.setDocId(doc.getId()); // Store doc ID if needed
                                    complaintList.add(complaint);
                                }
                            }
                        }
                    }
                    successListener.onSuccess(complaintList);
                })
                .addOnFailureListener(failureListener);
    }


    public void addMeterReading(String meterId, String date, Object value,
                                OnSuccessListener<Void> successListener,
                                OnFailureListener failureListener) {
        // Create a map for the new reading
        Map<String, Object> newReading = new HashMap<>();
        newReading.put(date, value);

        // Update the document, merging with existing data
        db.collection("meter_readings").document(meterId)
                .set(newReading, SetOptions.merge())
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}

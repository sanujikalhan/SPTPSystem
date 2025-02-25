package com.example.sptm_systerm;

public class Complaint {
    private String docId;             // Firestore doc ID (optional)
    private String subscriptionNo;    // e.g., "SA001"
    private String complaint;         // e.g., "AC not working"
    private String date;             // e.g., "21-03-2025"
    private String location;         // e.g., "6.9271,79.8612" or a URL
    private String technicianStatus; // e.g., "Pending"
    private String userStatus;       // e.g., "Not Fixed yet"

    public Complaint() {
        // Empty constructor needed for Firestore
    }

    public Complaint(String subscriptionNo, String complaint, String date,
                     String location, String technicianStatus, String userStatus) {
        this.subscriptionNo = subscriptionNo;
        this.complaint = complaint;
        this.date = date;
        this.location = location;
        this.technicianStatus = technicianStatus;
        this.userStatus = userStatus;
    }

    public String getDocId() {
        return docId;
    }
    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getSubscriptionNo() {
        return subscriptionNo;
    }
    public void setSubscriptionNo(String subscriptionNo) {
        this.subscriptionNo = subscriptionNo;
    }

    public String getComplaint() {
        return complaint;
    }
    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getTechnicianStatus() {
        return technicianStatus;
    }
    public void setTechnicianStatus(String technicianStatus) {
        this.technicianStatus = technicianStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}

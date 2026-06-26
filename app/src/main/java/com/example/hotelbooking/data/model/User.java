package com.example.hotelbooking.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class User implements Serializable {

    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String partnerStatus;
    private Timestamp createdAt;

    // Firebase Firestore/Realtime Database yêu cầu một constructor rỗng
    public User() {
    }

    public User(String uid, String fullName, String email) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
    }

    public User(String uid, String fullName, String email, String phone, String role, String partnerStatus) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.partnerStatus = partnerStatus;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPartnerStatus() { return partnerStatus; }
    public void setPartnerStatus(String partnerStatus) { this.partnerStatus = partnerStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
package com.example.hotelbooking.data.model;

public class User {

    private String uid;
    private String fullName;
    private String email;

    // Firebase Realtime Database yêu cầu một constructor rỗng
    public User() {
    }

    public User(String uid, String fullName, String email) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
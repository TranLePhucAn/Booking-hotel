package com.example.hotelbooking.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String partnerStatus;
    private String avatarUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {
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

    @PropertyName("full_name")
    public String getFullName() { return fullName; }
    @PropertyName("full_name")
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @PropertyName("partner_status")
    public String getPartnerStatus() { return partnerStatus; }
    @PropertyName("partner_status")
    public void setPartnerStatus(String partnerStatus) { this.partnerStatus = partnerStatus; }

    @PropertyName("avatar_url")
    public String getAvatarUrl() { return avatarUrl; }
    @PropertyName("avatar_url")
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    @PropertyName("created_at")
    public Timestamp getCreatedAt() { return createdAt; }
    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @PropertyName("updated_at")
    public Timestamp getUpdatedAt() { return updatedAt; }
    @PropertyName("updated_at")
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

package com.example.hotelbooking.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class PartnerApplication implements Serializable {
    private String id;
    private String userId;
    private String businessName;
    private String representativeName;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private String description;
    private String verificationFileUrl;
    private String status;
    private String adminNote;
    private Timestamp createdAt;
    private Timestamp reviewedAt;

    public PartnerApplication() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("user_id")
    public String getUserId() { return userId; }
    @PropertyName("user_id")
    public void setUserId(String userId) { this.userId = userId; }

    @PropertyName("business_name")
    public String getBusinessName() { return businessName; }
    @PropertyName("business_name")
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    @PropertyName("representative_name")
    public String getRepresentativeName() { return representativeName; }
    @PropertyName("representative_name")
    public void setRepresentativeName(String representativeName) { this.representativeName = representativeName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @PropertyName("tax_code")
    public String getTaxCode() { return taxCode; }
    @PropertyName("tax_code")
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @PropertyName("verification_file_url")
    public String getVerificationFileUrl() { return verificationFileUrl; }
    @PropertyName("verification_file_url")
    public void setVerificationFileUrl(String verificationFileUrl) { this.verificationFileUrl = verificationFileUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @PropertyName("admin_note")
    public String getAdminNote() { return adminNote; }
    @PropertyName("admin_note")
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    @PropertyName("created_at")
    public Timestamp getCreatedAt() { return createdAt; }
    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @PropertyName("reviewed_at")
    public Timestamp getReviewedAt() { return reviewedAt; }
    @PropertyName("reviewed_at")
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
}

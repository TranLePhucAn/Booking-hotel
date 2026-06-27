package com.example.hotelbooking.data.repository;

import com.example.hotelbooking.data.model.PartnerApplication;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.AppConstants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class PartnerRepository {
    private final FirebaseFirestore db;

    public PartnerRepository() {
        this.db = FirebaseClient.getFirestore();
    }

    public Task<Void> submitApplication(PartnerApplication application) {
        String id = application.getUserId();
        application.setId(id);
        application.setStatus(AppConstants.STATUS_PENDING);
        application.setCreatedAt(com.google.firebase.Timestamp.now());
        return db.collection(AppConstants.COLLECTION_PARTNER_APPLICATIONS).document(id).set(application);
    }

    public Task<QuerySnapshot> getPendingApplications() {
        return db.collection(AppConstants.COLLECTION_PARTNER_APPLICATIONS)
                .whereEqualTo("status", AppConstants.STATUS_PENDING)
                .get();
    }

    public Task<Void> updateApplicationStatus(String applicationId, String status, String adminNote) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("admin_note", adminNote);
        updates.put("reviewed_at", com.google.firebase.Timestamp.now());
        return db.collection(AppConstants.COLLECTION_PARTNER_APPLICATIONS).document(applicationId).update(updates);
    }
}

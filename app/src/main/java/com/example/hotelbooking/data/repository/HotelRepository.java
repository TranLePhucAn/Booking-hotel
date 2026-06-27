package com.example.hotelbooking.data.repository;

import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.AppConstants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class HotelRepository {
    private final FirebaseFirestore db;

    public HotelRepository() {
        this.db = FirebaseClient.getFirestore();
    }

    public Task<QuerySnapshot> getApprovedHotels() {
        return db.collection(AppConstants.COLLECTION_HOTELS)
                .whereEqualTo("approval_status", AppConstants.STATUS_APPROVED)
                .whereEqualTo("is_active", true)
                .get();
    }

    public Task<QuerySnapshot> getPendingHotels() {
        return db.collection(AppConstants.COLLECTION_HOTELS)
                .whereEqualTo("approval_status", AppConstants.STATUS_PENDING)
                .get();
    }

    public Task<Void> updateHotelApproval(String hotelId, String status, boolean isActive, String adminNote) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("approval_status", status);
        updates.put("is_active", isActive);
        updates.put("admin_note", adminNote);
        updates.put("updated_at", com.google.firebase.Timestamp.now());
        
        return db.collection(AppConstants.COLLECTION_HOTELS).document(hotelId).update(updates);
    }

    public Task<Void> addHotel(Hotel hotel) {
        return db.collection(AppConstants.COLLECTION_HOTELS).add(hotel).continueWith(task -> null);
    }
}

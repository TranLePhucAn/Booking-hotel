package com.example.hotelbooking.data.repository;

import com.example.hotelbooking.data.model.User;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.AppConstants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseClient.getFirestore();
    }

    public Task<DocumentSnapshot> getUser(String uid) {
        return db.collection(AppConstants.COLLECTION_USERS).document(uid).get();
    }

    public Task<Void> saveUser(User user) {
        return db.collection(AppConstants.COLLECTION_USERS).document(user.getUid()).set(user);
    }

    public Task<Void> updatePartnerStatus(String uid, String status) {
        return db.collection(AppConstants.COLLECTION_USERS).document(uid)
                .update("partner_status", status);
    }

    public Task<Void> updateRole(String uid, String role) {
        return db.collection(AppConstants.COLLECTION_USERS).document(uid)
                .update("role", role);
    }
}

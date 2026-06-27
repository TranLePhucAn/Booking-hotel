package com.example.hotelbooking.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class WishlistRepository {
    private final FirebaseFirestore db;

    public WishlistRepository() {
        this.db = FirebaseFirestore.getInstance();
    }


    public Task<Void> addToWishlist(String userId, String hotelId, Map<String, Object> hotelInfo) {
        return db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(hotelId)
                .set(hotelInfo);
    }

    public Task<Void> removeFromWishlist(String userId, String hotelId) {
        return db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(hotelId)
                .delete();
    }

    public Task<QuerySnapshot> getWishlist(String userId) {
        return db.collection("users")
                .document(userId)
                .collection("wishlists")
                .get();
    }

    public Task<Boolean> isFavorite(String userId, String hotelId) {
        return db.collection("users")
                .document(userId)
                .collection("wishlists")
                .document(hotelId)
                .get()
                .continueWith(task -> task.getResult().exists());
    }
}
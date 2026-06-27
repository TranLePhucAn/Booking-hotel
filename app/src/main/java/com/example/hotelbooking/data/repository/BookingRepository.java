package com.example.hotelbooking.data.repository;

import com.example.hotelbooking.data.model.Reservation;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.AppConstants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class BookingRepository {
    private final FirebaseFirestore db;

    public BookingRepository() {
        this.db = FirebaseClient.getFirestore();
    }

    public Task<Void> createReservation(Reservation reservation) {
        String id = db.collection(AppConstants.COLLECTION_RESERVATIONS).document().getId();
        reservation.setId(id);
        return db.collection(AppConstants.COLLECTION_RESERVATIONS).document(id).set(reservation);
    }

    public Task<QuerySnapshot> getUserReservations(String userId) {
        return db.collection(AppConstants.COLLECTION_RESERVATIONS)
                .whereEqualTo("customer_id", userId)
                .get();
    }

    public Task<QuerySnapshot> getPartnerReservations(String ownerId) {

        return db.collection(AppConstants.COLLECTION_RESERVATIONS)
                .whereEqualTo("owner_id", ownerId)
                .get();
    }

    public Task<QuerySnapshot> getAllReservations() {
        return db.collection(AppConstants.COLLECTION_RESERVATIONS)
                .get();
    }

    public Task<Void> updateReservationStatus(String reservationId, String status) {
        return db.collection(AppConstants.COLLECTION_RESERVATIONS)
                .document(reservationId)
                .update("status", status);
    }
}

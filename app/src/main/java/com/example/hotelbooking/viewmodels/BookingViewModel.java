package com.example.hotelbooking.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hotelbooking.data.model.Reservation;
import com.example.hotelbooking.data.repository.BookingRepository;

import java.util.List;

public class BookingViewModel extends ViewModel {
    private final BookingRepository bookingRepository;

    private final MutableLiveData<List<Reservation>> _reservations = new MutableLiveData<>();
    public LiveData<List<Reservation>> reservations = _reservations;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    public BookingViewModel() {
        this.bookingRepository = new BookingRepository();
    }

    public void createBooking(Reservation reservation) {
        _isLoading.setValue(true);
        bookingRepository.createReservation(reservation)
                .addOnSuccessListener(aVoid -> _isLoading.setValue(false))
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void fetchUserBookings(String userId) {
        _isLoading.setValue(true);
        bookingRepository.getUserReservations(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    _reservations.setValue(queryDocumentSnapshots.toObjects(Reservation.class));
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void fetchPartnerBookings(String ownerId) {
        _isLoading.setValue(true);
        bookingRepository.getPartnerReservations(ownerId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    _reservations.setValue(queryDocumentSnapshots.toObjects(Reservation.class));
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void fetchAllReservations() {
        _isLoading.setValue(true);
        bookingRepository.getAllReservations()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    _reservations.setValue(queryDocumentSnapshots.toObjects(Reservation.class));
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void updateStatus(String reservationId, String status) {
        bookingRepository.updateReservationStatus(reservationId, status)
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }
}

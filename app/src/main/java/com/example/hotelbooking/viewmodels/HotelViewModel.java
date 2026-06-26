package com.example.hotelbooking.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.repository.HotelRepository;
import com.example.hotelbooking.utils.AppConstants;

import java.util.List;

public class HotelViewModel extends ViewModel {
    private final HotelRepository hotelRepository;
    
    private final MutableLiveData<List<Hotel>> _hotels = new MutableLiveData<>();
    public LiveData<List<Hotel>> hotels = _hotels;

    private final MutableLiveData<List<Hotel>> _pendingHotels = new MutableLiveData<>();
    public LiveData<List<Hotel>> pendingHotels = _pendingHotels;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    public HotelViewModel() {
        this.hotelRepository = new HotelRepository();
    }

    public void fetchApprovedHotels() {
        _isLoading.setValue(true);
        hotelRepository.getApprovedHotels()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    _hotels.setValue(queryDocumentSnapshots.toObjects(Hotel.class));
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void fetchPendingHotels() {
        _isLoading.setValue(true);
        hotelRepository.getPendingHotels()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    _pendingHotels.setValue(queryDocumentSnapshots.toObjects(Hotel.class));
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void approveHotel(String hotelId, String adminNote) {
        hotelRepository.updateHotelApproval(hotelId, AppConstants.STATUS_APPROVED, true, adminNote)
                .addOnSuccessListener(aVoid -> fetchPendingHotels())
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }

    public void rejectHotel(String hotelId, String adminNote) {
        hotelRepository.updateHotelApproval(hotelId, AppConstants.STATUS_REJECTED, false, adminNote)
                .addOnSuccessListener(aVoid -> fetchPendingHotels())
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }
}

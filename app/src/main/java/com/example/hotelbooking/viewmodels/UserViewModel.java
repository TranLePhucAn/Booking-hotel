package com.example.hotelbooking.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hotelbooking.data.model.User;
import com.example.hotelbooking.data.repository.UserRepository;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> _userData = new MutableLiveData<>();
    public LiveData<User> userData = _userData;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public UserViewModel() {
        this.userRepository = new UserRepository();
    }

    public void fetchUserData(String uid) {
        userRepository.getUser(uid)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        _userData.setValue(documentSnapshot.toObject(User.class));
                    } else {
                        _error.setValue("Người dùng không tồn tại");
                    }
                })
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }

    public void updatePartnerStatus(String uid, String status) {
        userRepository.updatePartnerStatus(uid, status)
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }
}

package com.example.hotelbooking.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hotelbooking.data.repository.AuthRepository;
import com.example.hotelbooking.data.repository.UserRepository;
import com.example.hotelbooking.data.model.User;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<FirebaseUser> _userSession = new MutableLiveData<>();
    public LiveData<FirebaseUser> userSession = _userSession;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
        _userSession.setValue(authRepository.getCurrentUser());
    }

    public void login(String email, String password) {
        _isLoading.setValue(true);
        authRepository.login(email, password)
                .addOnSuccessListener(authResult -> {
                    _userSession.setValue(authResult.getUser());
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void register(String email, String password, String fullName, String phone) {
        _isLoading.setValue(true);
        authRepository.register(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        User newUser = new User(firebaseUser.getUid(), fullName, email, phone, AppConstants.ROLE_USER, null);
                        userRepository.saveUser(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    _userSession.setValue(firebaseUser);
                                    _isLoading.setValue(false);
                                })
                                .addOnFailureListener(e -> {
                                    _error.setValue(e.getMessage());
                                    _isLoading.setValue(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void logout() {
        authRepository.logout();
        _userSession.setValue(null);
    }
}

package com.example.hotelbooking.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hotelbooking.data.repository.UserRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;

    private final MutableLiveData<Map<String, Object>> _userData = new MutableLiveData<>();
    public LiveData<Map<String, Object>> userData = _userData;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Boolean> _updateSuccess = new MutableLiveData<>();
    public LiveData<Boolean> updateSuccess = _updateSuccess;

    private final MutableLiveData<String> _uploadedAvatarUrl = new MutableLiveData<>();
    public LiveData<String> uploadedAvatarUrl = _uploadedAvatarUrl;

    public UserViewModel() {
        this.userRepository = new UserRepository();
    }

    public void fetchUser(String uid) {
        _isLoading.setValue(true);
        userRepository.getUser(uid)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        _userData.setValue(documentSnapshot.getData());
                    } else {
                        _error.setValue("Tài khoản không tồn tại");
                    }
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void updateUserProfile(String uid, Map<String, Object> updates) {
        _isLoading.setValue(true);
        userRepository.updateUser(uid, updates)
                .addOnSuccessListener(aVoid -> {
                    _updateSuccess.setValue(true);
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void uploadAvatar(String uid, Uri imageUri, Map<String, Object> data) {
        _isLoading.setValue(true);
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("avatars/" + uid + ".jpg");
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            _uploadedAvatarUrl.setValue(url);
                            data.put("avatarUrl", url);
                            updateUserProfile(uid, data);
                        })
                        .addOnFailureListener(e -> {
                            _error.setValue("Không lấy được link ảnh sau khi upload: " + e.getMessage());
                            _isLoading.setValue(false);
                        }))
                .addOnFailureListener(e -> {
                    _error.setValue("Upload ảnh thất bại: " + e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void clearState() {
        _updateSuccess.setValue(false);
        _error.setValue(null);
    }
}

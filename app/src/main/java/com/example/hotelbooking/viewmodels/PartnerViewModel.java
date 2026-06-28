package com.example.hotelbooking.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hotelbooking.data.model.PartnerApplication;
import com.example.hotelbooking.data.repository.PartnerRepository;
import com.example.hotelbooking.data.repository.UserRepository;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PartnerViewModel extends ViewModel {
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<List<PartnerApplication>> _applications = new MutableLiveData<>();
    public LiveData<List<PartnerApplication>> applications = _applications;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    public PartnerViewModel() {
        this.partnerRepository = new PartnerRepository();
        this.userRepository = new UserRepository();
    }

    public void fetchPendingApplications() {
        _isLoading.setValue(true);
        partnerRepository.getPendingApplications()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    _applications.setValue(toSortedApplications(queryDocumentSnapshots.getDocuments()));
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void approveApplication(PartnerApplication app, String adminNote, Runnable onSuccess) {
        if (app.getUserId() == null || app.getUserId().isEmpty()) {
            _error.setValue("Lỗi: Không tìm thấy ID người dùng trong hồ sơ này!");
            return;
        }

        _isLoading.setValue(true);
        partnerRepository.updateApplicationStatus(app.getId(), AppConstants.STATUS_APPROVED, adminNote)
                .addOnSuccessListener(aVoid -> {
                    userRepository.updateRole(app.getUserId(), AppConstants.ROLE_PARTNER)
                            .addOnSuccessListener(v -> {
                                userRepository.updatePartnerStatus(app.getUserId(), AppConstants.STATUS_APPROVED)
                                        .addOnSuccessListener(v2 -> {
                                            sendNotification(app.getUserId(), "Approved", "Đăng ký cộng sự của bạn đã được duyệt.");
                                            _isLoading.setValue(false);
                                            // GỌI HÀM KẾT THÚC KHI ĐÃ HOÀN TẤT
                                            if (onSuccess != null) onSuccess.run();
                                        })
                                        .addOnFailureListener(e -> {
                                            _error.setValue("Lỗi update trạng thái user: " + e.getMessage());
                                            _isLoading.setValue(false);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                _error.setValue("Lỗi update quyền user: " + e.getMessage());
                                _isLoading.setValue(false);
                            });
                })
                .addOnFailureListener(e -> {
                    _error.setValue("Lỗi duyệt hồ sơ: " + e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void rejectApplication(String appId, String userId, String adminNote, Runnable onSuccess) {
        if (userId == null || userId.isEmpty()) {
            _error.setValue("Lỗi: Không tìm thấy ID người dùng để từ chối!");
            return;
        }

        _isLoading.setValue(true);
        partnerRepository.updateApplicationStatus(appId, AppConstants.STATUS_REJECTED, adminNote)
                .addOnSuccessListener(aVoid -> {
                    java.util.Map<String, Object> updates = new java.util.HashMap<>();
                    updates.put("partnerStatus", AppConstants.STATUS_REJECTED);
                    updates.put("admin_note", adminNote);
                    userRepository.updateUser(userId, updates)
                            .addOnSuccessListener(v -> {
                                sendNotification(userId, "Rejected", "Đăng ký cộng sự đã bị từ chối. Lý do: " + adminNote);
                                _isLoading.setValue(false);
                                // GỌI HÀM KẾT THÚC KHI ĐÃ HOÀN TẤT
                                if (onSuccess != null) onSuccess.run();
                            })
                            .addOnFailureListener(e -> {
                                _error.setValue("Lỗi update user khi từ chối: " + e.getMessage());
                                _isLoading.setValue(false);
                            });
                })
                .addOnFailureListener(e -> {
                    _error.setValue("Lỗi từ chối hồ sơ: " + e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    private void sendNotification(String userId, String title, String message) {
        java.util.Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("user_id", userId);
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("time", com.google.firebase.Timestamp.now());
        notification.put("read", false);
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_NOTIFICATIONS)
                .add(notification);
    }

    private List<PartnerApplication> toSortedApplications(List<DocumentSnapshot> documents) {
        List<PartnerApplication> result = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            PartnerApplication application = document.toObject(PartnerApplication.class);
            if (application != null) {
                application.setId(document.getId());

                // QUAN TRỌNG: Gắn userId bằng tay phòng hờ model chưa map được
                if (application.getUserId() == null) {
                    application.setUserId(document.getString("user_id"));
                }

                result.add(application);
            }
        }
        result.sort(Comparator.comparingLong(this::createdAtMillis));
        return result;
    }

    private long createdAtMillis(PartnerApplication application) {
        return application.getCreatedAt() == null ? 0 : application.getCreatedAt().toDate().getTime();
    }
}

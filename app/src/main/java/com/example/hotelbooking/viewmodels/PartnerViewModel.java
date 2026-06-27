package com.example.hotelbooking.viewmodels;

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

    public void approveApplication(PartnerApplication app, String adminNote) {
        _isLoading.setValue(true);
        partnerRepository.updateApplicationStatus(app.getId(), AppConstants.STATUS_APPROVED, adminNote)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật role của user
                    userRepository.updateRole(app.getUserId(), AppConstants.ROLE_PARTNER)
                            .addOnSuccessListener(v -> {
                                userRepository.updatePartnerStatus(app.getUserId(), AppConstants.STATUS_APPROVED)
                                        .addOnSuccessListener(v2 -> {
                                            fetchPendingApplications();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    public void rejectApplication(String appId, String userId, String adminNote) {
        _isLoading.setValue(true);
        partnerRepository.updateApplicationStatus(appId, AppConstants.STATUS_REJECTED, adminNote)
                .addOnSuccessListener(aVoid -> {
                    userRepository.updatePartnerStatus(userId, AppConstants.STATUS_REJECTED)
                            .addOnSuccessListener(v -> fetchPendingApplications());
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    private List<PartnerApplication> toSortedApplications(List<DocumentSnapshot> documents) {
        List<PartnerApplication> result = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            PartnerApplication application = document.toObject(PartnerApplication.class);
            if (application != null) {
                application.setId(document.getId());
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

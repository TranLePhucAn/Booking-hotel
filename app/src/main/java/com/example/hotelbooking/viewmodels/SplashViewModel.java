package com.example.hotelbooking.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.utils.RoleRouter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashViewModel extends ViewModel {
    private final FirebaseAuth auth = FirebaseClient.getAuth();
    private final FirebaseFirestore db = FirebaseClient.getFirestore();

    public MutableLiveData<String> navigationTarget = new MutableLiveData<>();

    public void checkUserSession() {
        if (auth.getCurrentUser() == null) {
            navigationTarget.setValue("HOME");
            return;
        }

        db.collection(AppConstants.COLLECTION_USERS)
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String partnerStatus = documentSnapshot.getString("partnerStatus");
                        String accountStatus = documentSnapshot.getString("status");
                        if (AppConstants.STATUS_BLOCKED.equalsIgnoreCase(accountStatus)) {
                            auth.signOut();
                            navigationTarget.setValue("HOME");
                            return;
                        }
                        navigationTarget.setValue(RoleRouter.getRoute(role, partnerStatus));
                    } else {
                        navigationTarget.setValue("HOME");
                    }
                })
                .addOnFailureListener(e -> navigationTarget.setValue("HOME"));
    }
}

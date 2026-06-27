package com.example.hotelbooking.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.hotelbooking.ui.admin.AdminDashboardActivity;
import com.example.hotelbooking.ui.home.HomeActivity;
import com.example.hotelbooking.ui.partner.PartnerDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RoleRouter {

    public static final String ROLE_ADMIN = AppConstants.ROLE_ADMIN;
    public static final String ROLE_PARTNER = AppConstants.ROLE_PARTNER;
    public static final String ROLE_USER = AppConstants.ROLE_USER;

    private RoleRouter() {}

    public static void routeCurrentUser(Activity activity, LoadingDialog loadingDialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (loadingDialog != null) loadingDialog.dismiss();
            return;
        }

        FirebaseFirestore.getInstance().collection(AppConstants.COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    String status = documentSnapshot.getString("partnerStatus");
                    navigateByRole(activity, role, status);
                    if (loadingDialog != null) loadingDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    if (loadingDialog != null) loadingDialog.dismiss();
                    Toast.makeText(activity, "Lỗi phân quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static String getRoute(String role, String partnerStatus) {
        if (role == null) return "HOME";

        // Thay bằng equalsIgnoreCase
        if (AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            return "ADMIN_DASHBOARD";
        } else if (AppConstants.ROLE_PARTNER.equalsIgnoreCase(role)) {
            return "PARTNER_DASHBOARD";
        } else {
            return "HOME";
        }
    }

    public static void navigateByRole(Activity activity, String role, String partnerStatus) {
        Intent intent;

        // Thay bằng equalsIgnoreCase
        if (AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(activity, AdminDashboardActivity.class);
        } else if (AppConstants.ROLE_PARTNER.equalsIgnoreCase(role)) {
            intent = new Intent(activity, PartnerDashboardActivity.class);
        } else {
            intent = new Intent(activity, HomeActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
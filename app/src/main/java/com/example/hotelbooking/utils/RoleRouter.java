package com.example.hotelbooking.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.ui.home.HomeActivity;
import com.example.hotelbooking.ui.owner.AdminDashboardActivity;
import com.example.hotelbooking.ui.owner.OwnerDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RoleRouter {

    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_OWNER = "owner";
    public static final String ROLE_ADMIN = "admin";

    private RoleRouter() {
    }

    public static void routeCurrentUser(Activity activity, LoadingDialog loadingDialog) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            if (loadingDialog != null) {
                loadingDialog.dismiss();
            }
            Toast.makeText(activity, "Chua co tai khoan dang nhap", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }

                    if (!documentSnapshot.exists()) {
                        createDefaultCustomerRole(firebaseUser);
                        openCustomer(activity);
                        return;
                    }

                    String role = documentSnapshot.getString("role");
                    if (ROLE_ADMIN.equalsIgnoreCase(role)) {
                        openAdmin(activity);
                    } else if (ROLE_OWNER.equalsIgnoreCase(role)) {
                        checkPartnerApproval(activity, firebaseUser.getUid());
                    } else {
                        openCustomer(activity);
                    }
                })
                .addOnFailureListener(e -> {
                    if (loadingDialog != null) {
                        loadingDialog.dismiss();
                    }
                    Toast.makeText(activity, "Khong doc duoc phan quyen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    openCustomer(activity);
                });
    }

    private static void createDefaultCustomerRole(FirebaseUser firebaseUser) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("email", firebaseUser.getEmail());
        userData.put("fullName", firebaseUser.getDisplayName());
        userData.put("role", ROLE_CUSTOMER);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(firebaseUser.getUid())
                .set(userData);
    }

    private static void openOwner(Activity activity) {
        Intent intent = new Intent(activity, OwnerDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void checkPartnerApproval(Activity activity, String uid) {
        FirebaseFirestore.getInstance()
                .collection("businesses")
                .document(uid)
                .get()
                .addOnSuccessListener(partnerDoc -> {
                    String status = partnerDoc.getString("status");
                    if ("approved".equalsIgnoreCase(status)) {
                        openOwner(activity);
                    } else if ("rejected".equalsIgnoreCase(status)) {
                        signOutToLogin(activity, "Tai khoan cua ban khong duoc duyet. Vui long lien he quan tri vien.");
                    } else {
                        signOutToLogin(activity, "Tai khoan cua ban dang cho quan tri vien xet duyet.");
                    }
                })
                .addOnFailureListener(e -> {
                    signOutToLogin(activity, "Khong kiem tra duoc trang thai doi tac: " + e.getMessage());
                });
    }

    private static void openAdmin(Activity activity) {
        Intent intent = new Intent(activity, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void openCustomer(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void signOutToLogin(Activity activity, String message) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}

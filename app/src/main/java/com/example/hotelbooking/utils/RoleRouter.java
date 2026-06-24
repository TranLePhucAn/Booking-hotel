package com.example.hotelbooking.utils;

import android.app.Activity;
import android.content.Intent;
import com.example.hotelbooking.ui.admin.AdminDashboardActivity;
import com.example.hotelbooking.ui.home.HomeActivity;
import com.example.hotelbooking.ui.partner.PartnerDashboardActivity;

public class RoleRouter {

    private RoleRouter() {}

    public static String getRoute(String role, String partnerStatus) {
        if (role == null) return "LOGIN";

        switch (role) {
            case AppConstants.ROLE_ADMIN:
                return "ADMIN_DASHBOARD";
            case AppConstants.ROLE_PARTNER:
                return "PARTNER_DASHBOARD";
            case AppConstants.ROLE_USER:
            default:
                return "HOME";
        }
    }

    public static void navigateByRole(Activity activity, String role, String partnerStatus) {
        Intent intent;
        if (AppConstants.ROLE_ADMIN.equals(role)) {
            intent = new Intent(activity, AdminDashboardActivity.class);
        } else if (AppConstants.ROLE_PARTNER.equals(role)) {
            intent = new Intent(activity, PartnerDashboardActivity.class);
        } else {
            intent = new Intent(activity, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}

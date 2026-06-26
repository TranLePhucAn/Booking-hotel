package com.example.hotelbooking.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.admin.AdminDashboardActivity;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.ui.home.HomeActivity;
import com.example.hotelbooking.ui.partner.PartnerDashboardActivity;
import com.example.hotelbooking.viewmodels.SplashViewModel;

public class SplashActivity extends AppCompatActivity {
    private SplashViewModel splashViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        splashViewModel.navigationTarget.observe(this, target -> {
            if (target != null) {
                Intent intent;
                switch (target) {
                    case "ADMIN_DASHBOARD":
                        intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                        break;
                    case "PARTNER_DASHBOARD":
                        intent = new Intent(SplashActivity.this, PartnerDashboardActivity.class);
                        break;
                    case "HOME":
                        intent = new Intent(SplashActivity.this, HomeActivity.class);
                        break;
                    case "LOGIN":
                    default:
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                        break;
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashViewModel.checkUserSession();
        }, 1200);
    }
}

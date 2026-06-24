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
    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        viewModel.navigationTarget.observe(this, target -> {
            Class<?> destination = LoginActivity.class;
            if ("ADMIN_DASHBOARD".equals(target)) destination = AdminDashboardActivity.class;
            else if ("PARTNER_DASHBOARD".equals(target)) destination = PartnerDashboardActivity.class;
            else if ("HOME".equals(target)) destination = HomeActivity.class;

            startActivity(new Intent(this, destination));
            finish();
        });

        new Handler(Looper.getMainLooper()).postDelayed(viewModel::checkUserSession, 1200);
    }
}
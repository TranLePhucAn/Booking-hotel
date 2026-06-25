package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private TextView tvName, tvEmail, tvPartnerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initViews();
        
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        userViewModel.userData.observe(this, user -> {
            if (user != null) {
                tvName.setText(user.getFullName());
                tvEmail.setText(user.getEmail());
                if (AppConstants.STATUS_PENDING.equals(user.getPartnerStatus())) {
                    tvPartnerStatus.setVisibility(View.VISIBLE);
                    tvPartnerStatus.setText("Hồ sơ cộng sự đang chờ duyệt");
                } else if (AppConstants.STATUS_REJECTED.equals(user.getPartnerStatus())) {
                    tvPartnerStatus.setVisibility(View.VISIBLE);
                    tvPartnerStatus.setText("Yêu cầu cộng sự bị từ chối");
                    tvPartnerStatus.setTextColor(getColor(R.color.error));
                } else {
                    tvPartnerStatus.setVisibility(View.GONE);
                }
            }
        });

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userViewModel.fetchUserData(uid);
        }
    }

    private void initViews() {
        ImageView btnBackProfile = findViewById(R.id.btnBackProfile);
        tvName = findViewById(R.id.tvProfileNameDisplay);
        tvEmail = findViewById(R.id.tvProfileEmailDisplay);
        tvPartnerStatus = findViewById(R.id.tvPartnerStatus);

        LinearLayout menuEditAccount = findViewById(R.id.menuEditAccount);
        LinearLayout menuBookingHistory = findViewById(R.id.menuBookingHistory);
        LinearLayout menuLogout = findViewById(R.id.menuLogout);

        if (btnBackProfile != null) {
            btnBackProfile.setOnClickListener(v -> finish());
        }

        if (menuEditAccount != null) {
            menuEditAccount.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            });
        }

        if (menuBookingHistory != null) {
            menuBookingHistory.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, BookingHistoryActivity.class));
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                // Quay về Trang chủ thay vì Login
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}

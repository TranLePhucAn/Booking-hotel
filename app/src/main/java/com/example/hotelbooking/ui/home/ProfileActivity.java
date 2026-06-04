package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.ui.auth.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initViews();
    }

    private void initViews() {
        ImageView btnBackProfile = findViewById(R.id.btnBackProfile);
        TextView tvProfileNameDisplay = findViewById(R.id.tvProfileNameDisplay);
        TextView tvProfileEmailDisplay = findViewById(R.id.tvProfileEmailDisplay);

        // Ánh xạ thêm nút chỉnh sửa tài khoản từ XML của bạn
        LinearLayout menuEditAccount = findViewById(R.id.menuEditAccount);
        LinearLayout menuBookingHistory = findViewById(R.id.menuBookingHistory);
        LinearLayout menuLogout = findViewById(R.id.menuLogout);

        // Hiển thị thông tin user từ Firebase
        if (FirebaseClient.getAuth().getCurrentUser() != null) {
            String name = FirebaseClient.getAuth().getCurrentUser().getDisplayName();
            String email = FirebaseClient.getAuth().getCurrentUser().getEmail();

            if (name != null && !name.isEmpty()) tvProfileNameDisplay.setText(name);
            if (email != null && !email.isEmpty()) tvProfileEmailDisplay.setText(email);
        }

        // Quay lại trang Home
        if (btnBackProfile != null) {
            btnBackProfile.setOnClickListener(v -> finish());
        }

        // Bấm chuyển sang trang Chỉnh sửa thông tin
        if (menuEditAccount != null) {
            menuEditAccount.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            });
        }

        // Bấm chuyển sang trang Lịch sử đặt phòng
        if (menuBookingHistory != null) {
            menuBookingHistory.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, BookingHistoryActivity.class));
            });
        }

        // Xử lý Đăng xuất
        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                FirebaseClient.getAuth().signOut();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
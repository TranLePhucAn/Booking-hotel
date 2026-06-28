package com.example.hotelbooking.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        CardView btnApprovePartner = findViewById(R.id.btnApprovePartner);
        CardView btnApproveHotel = findViewById(R.id.btnApproveHotel);
        CardView btnManageAllBookings = findViewById(R.id.btnManageAllBookings);
        CardView btnGoToHome = findViewById(R.id.btnGoToHome);
        Button btnLogout = findViewById(R.id.btnLogoutAdmin);

        btnApprovePartner.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminPartnerApprovalActivity.class));
        });

        btnApproveHotel.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminHotelApprovalActivity.class));
        });

        btnManageAllBookings.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminBookingManagementActivity.class));
        });

        CardView btnManageUsers = findViewById(R.id.btnManageUsers);
        CardView btnManageHotels = findViewById(R.id.btnManageHotels);

        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Chức năng Quản lý người dùng của thành viên khác đang phát triển", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        if (btnManageHotels != null) {
            btnManageHotels.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Chức năng Quản lý khách sạn của thành viên khác đang phát triển", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        if (btnGoToHome != null) {
            btnGoToHome.setOnClickListener(v -> {
                startActivity(new Intent(this, HomeActivity.class));
            });
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // CHỈNH SỬA: Quay về Trang chủ thay vì Login
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}

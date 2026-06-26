package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileNameDisplay;
    private TextView tvProfileEmailDisplay;
    private TextView tvProfilePhoneDisplay;
    private TextView tvProfileGenderDisplay;
    private TextView tvProfileDobDisplay;
    private TextView tvProfileCountryDisplay;
    private TextView tvPartnerStatus;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initViews();

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        observeViewModel();

        loadUserData();
    }

    private void initViews() {
        ImageView btnBackProfile = findViewById(R.id.btnBackProfile);

        tvProfileNameDisplay = findViewById(R.id.tvProfileNameDisplay);
        tvProfileEmailDisplay = findViewById(R.id.tvProfileEmailDisplay);
        tvProfilePhoneDisplay = findViewById(R.id.tvProfilePhoneDisplay);
        tvProfileGenderDisplay = findViewById(R.id.tvProfileGenderDisplay);
        tvProfileDobDisplay = findViewById(R.id.tvProfileDobDisplay);
        tvProfileCountryDisplay = findViewById(R.id.tvProfileCountryDisplay);
        tvPartnerStatus = findViewById(R.id.tvPartnerStatus);

        LinearLayout menuEditAccount = findViewById(R.id.menuEditAccount);
        LinearLayout menuBookingHistory = findViewById(R.id.menuBookingHistory);
        LinearLayout menuLogout = findViewById(R.id.menuLogout);

        // Nút quay lại
        if (btnBackProfile != null) {
            btnBackProfile.setOnClickListener(v -> finish());
        }

        // Chuyển sang trang Chỉnh sửa thông tin
        if (menuEditAccount != null) {
            menuEditAccount.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            });
        }

        // Chuyển sang trang Lịch sử đặt phòng
        if (menuBookingHistory != null) {
            menuBookingHistory.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, BookingHistoryActivity.class));
            });
        }

        // Đăng xuất
        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                FirebaseClient.getAuth().signOut();

                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void observeViewModel() {
        userViewModel.userData.observe(this, data -> {
            if (data != null) {
                String fullName = (String) data.get("fullName");
                String email = (String) data.get("email");
                String phone = (String) data.get("phone");
                String gender = (String) data.get("gender");
                String dob = (String) data.get("date_of_birth");
                String country = (String) data.get("country");
                String partnerStatus = (String) data.get("partnerStatus");
                String role = (String) data.get("role");

                if (fullName != null && !fullName.isEmpty()) {
                    tvProfileNameDisplay.setText(fullName);
                } else {
                    tvProfileNameDisplay.setText("Chưa cập nhật tên");
                }

                if (email != null && !email.isEmpty()) {
                    tvProfileEmailDisplay.setText(email);
                }

                tvProfilePhoneDisplay.setText("Số điện thoại: " + (phone != null && !phone.isEmpty() ? phone : "---"));
                tvProfileGenderDisplay.setText("Giới tính: " + (gender != null && !gender.isEmpty() ? gender : "---"));
                tvProfileDobDisplay.setText("Ngày sinh: " + (dob != null && !dob.isEmpty() ? dob : "---"));
                tvProfileCountryDisplay.setText("Quốc tịch: " + (country != null && !country.isEmpty() ? country : "---"));

                if (tvPartnerStatus != null) {
                    if ("pending".equals(partnerStatus)) {
                        tvPartnerStatus.setVisibility(android.view.View.VISIBLE);
                        tvPartnerStatus.setText("Hồ sơ cộng sự đang chờ duyệt");
                    } else if ("rejected".equals(partnerStatus)) {
                        tvPartnerStatus.setVisibility(android.view.View.VISIBLE);
                        String adminNote = (String) data.get("admin_note");
                        if (adminNote != null && !adminNote.isEmpty()) {
                            tvPartnerStatus.setText("Hồ sơ cộng sự bị từ chối: " + adminNote);
                        } else {
                            tvPartnerStatus.setText("Hồ sơ cộng sự bị từ chối");
                        }
                    } else if ("approved".equals(partnerStatus) || "partner".equals(role)) {
                        tvPartnerStatus.setVisibility(android.view.View.VISIBLE);
                        tvPartnerStatus.setText("Trạng thái: Đối tác / Cộng sự");
                        tvPartnerStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        tvPartnerStatus.setVisibility(android.view.View.GONE);
                    }
                }
            }
        });

        userViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(ProfileActivity.this, "Không thể tải thông tin: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseClient.getAuth().getCurrentUser();
        if (currentUser != null) {
            userViewModel.fetchUser(currentUser.getUid());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}
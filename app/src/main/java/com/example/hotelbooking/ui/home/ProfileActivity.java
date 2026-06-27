package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.ui.auth.PartnerRegisterActivity;
import com.example.hotelbooking.ui.partner.PartnerDashboardActivity;
import com.example.hotelbooking.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileNameDisplay;
    private TextView tvProfileEmailDisplay;
    private TextView tvProfilePhoneDisplay;
    private TextView tvProfileGenderDisplay;
    private TextView tvProfileDobDisplay;
    private TextView tvProfileCountryDisplay;

    private LinearLayout layoutPartnerStatus;
    private TextView tvPartnerStatus;
    private TextView tvAdminNote;
    private Button btnPartnerAction;
    private Button btnRegisterPartner;

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

        layoutPartnerStatus = findViewById(R.id.layoutPartnerStatus);
        tvPartnerStatus = findViewById(R.id.tvPartnerStatus);
        tvAdminNote = findViewById(R.id.tvAdminNote);
        btnPartnerAction = findViewById(R.id.btnPartnerAction);
        btnRegisterPartner = findViewById(R.id.btnRegisterPartner);

        LinearLayout menuEditAccount = findViewById(R.id.menuEditAccount);
        LinearLayout menuBookingHistory = findViewById(R.id.menuBookingHistory);
        LinearLayout menuWishlist = findViewById(R.id.menuWishlist);
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

        // Chuyển sang trang Khách sạn yêu thích
        if (menuWishlist != null) {
            menuWishlist.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, WishlistActivity.class));
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

        if (btnRegisterPartner != null) {
            btnRegisterPartner.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, PartnerRegisterActivity.class));
            });
        }
    }

    private void observeViewModel() {
        userViewModel.userData.observe(this, data -> {
            if (data != null) {
                updateBasicInfo(data);
                updatePartnerStatusUI(data);
            }
        });

        userViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(ProfileActivity.this, "Không thể tải thông tin: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBasicInfo(Map<String, Object> data) {
        String fullName = (String) data.get("fullName");
        String email = (String) data.get("email");
        String phone = (String) data.get("phone");
        String gender = (String) data.get("gender");
        String dob = (String) data.get("date_of_birth");
        String country = (String) data.get("country");

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
    }

    private void updatePartnerStatusUI(Map<String, Object> data) {
        String partnerStatus = (String) data.get("partnerStatus");
        String role = (String) data.get("role");
        String adminNote = (String) data.get("admin_note");

        // Reset visibility
        layoutPartnerStatus.setVisibility(View.GONE);
        btnRegisterPartner.setVisibility(View.GONE);
        tvAdminNote.setVisibility(View.GONE);
        btnPartnerAction.setVisibility(View.GONE);

        if ("partner".equalsIgnoreCase(role) || "APPROVED".equalsIgnoreCase(partnerStatus)) {
            // Trường hợp là cộng sự đã duyệt
            layoutPartnerStatus.setVisibility(View.VISIBLE);
            tvPartnerStatus.setText("Bạn đã trở thành cộng sự");
            tvPartnerStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnPartnerAction.setVisibility(View.VISIBLE);
            btnPartnerAction.setText("Vào Partner Dashboard");
            btnPartnerAction.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, PartnerDashboardActivity.class));
            });
        } else if ("PENDING".equalsIgnoreCase(partnerStatus)) {
            // Trường hợp đang chờ duyệt
            layoutPartnerStatus.setVisibility(View.VISIBLE);
            tvPartnerStatus.setText("Hồ sơ cộng sự đang chờ Admin phê duyệt");
            tvPartnerStatus.setTextColor(getResources().getColor(R.color.ocean_blue));
        } else if ("REJECTED".equalsIgnoreCase(partnerStatus)) {
            // Trường hợp bị từ chối
            layoutPartnerStatus.setVisibility(View.VISIBLE);
            tvPartnerStatus.setText("Hồ sơ cộng sự bị từ chối");
            tvPartnerStatus.setTextColor(getResources().getColor(R.color.error));

            if (adminNote != null && !adminNote.isEmpty()) {
                tvAdminNote.setVisibility(View.VISIBLE);
                tvAdminNote.setText("Lý do: " + adminNote);
            }

            btnPartnerAction.setVisibility(View.VISIBLE);
            btnPartnerAction.setText("Đăng ký lại");
            btnPartnerAction.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, PartnerRegisterActivity.class));
            });
        } else if ("BLOCKED".equalsIgnoreCase(partnerStatus)) {
            // Trường hợp tài khoản bị khóa
            layoutPartnerStatus.setVisibility(View.VISIBLE);
            tvPartnerStatus.setText("Tài khoản đã bị khóa");
            tvPartnerStatus.setTextColor(getResources().getColor(R.color.error));
        } else {
            // User thường
            btnRegisterPartner.setVisibility(View.VISIBLE);
        }
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
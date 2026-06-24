package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileNameDisplay;
    private TextView tvProfileEmailDisplay;
    private TextView tvProfilePhoneDisplay;
    private TextView tvProfileGenderDisplay;
    private TextView tvProfileDobDisplay;
    private TextView tvProfileCountryDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initViews();
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

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseClient.getAuth().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {

                            String fullName = documentSnapshot.getString("fullName");
                            String email = documentSnapshot.getString("email");
                            String phone = documentSnapshot.getString("phone");
                            String gender = documentSnapshot.getString("gender");
                            String dob = documentSnapshot.getString("date_of_birth");
                            String country = documentSnapshot.getString("country");


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

                        } else {
                            Log.d("ProfileActivity", "Document không tồn tại!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Không thể tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "Lỗi tải dữ liệu", e);
                    });
        }
    }
}
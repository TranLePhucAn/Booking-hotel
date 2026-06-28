package com.example.hotelbooking.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminUserDetailActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private String userId;
    private String currentAdminId;
    private String currentStatus;

    private TextView textStatus;
    private Button buttonToggleStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        firestore = FirebaseFirestore.getInstance();
        currentAdminId = FirebaseAuth.getInstance().getUid();

        bindViews();
        loadIntentData();
        renderUser();
    }

    private void bindViews() {
        TextView buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        buttonToggleStatus = findViewById(R.id.buttonToggleStatus);
        buttonToggleStatus.setOnClickListener(v -> confirmToggleStatus());
    }

    private void loadIntentData() {
        userId = getIntent().getStringExtra("uid");
        currentStatus = valueOrDefault(getIntent().getStringExtra("status"), "active");
    }

    private void renderUser() {
        TextView textName = findViewById(R.id.textName);
        TextView textUid = findViewById(R.id.textUid);
        TextView textEmail = findViewById(R.id.textEmail);
        TextView textPhone = findViewById(R.id.textPhone);
        TextView textRole = findViewById(R.id.textRole);
        TextView textPartnerStatus = findViewById(R.id.textPartnerStatus);
        textStatus = findViewById(R.id.textStatus);

        textName.setText(valueOrDefault(getIntent().getStringExtra("fullName"), "Chưa cập nhật tên"));
        textUid.setText("UID: " + valueOrDefault(userId, ""));
        textEmail.setText("Email: " + valueOrDefault(getIntent().getStringExtra("email"), "Chưa có email"));
        textPhone.setText("Số điện thoại: " + valueOrDefault(getIntent().getStringExtra("phone"), "Chưa cập nhật"));
        textRole.setText("Role: " + valueOrDefault(getIntent().getStringExtra("role"), AppConstants.ROLE_USER));

        String partnerStatus = valueOrDefault(getIntent().getStringExtra("partnerStatus"), "");
        textPartnerStatus.setText(partnerStatus.isEmpty()
                ? "Partner status: Không có"
                : "Partner status: " + partnerStatus);

        updateStatusViews();
    }

    private void confirmToggleStatus() {
        if (currentAdminId != null && currentAdminId.equals(userId)) {
            Toast.makeText(this, "Admin không thể tự khóa tài khoản của mình", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean blocked = AppConstants.STATUS_BLOCKED.equalsIgnoreCase(currentStatus);
        String nextStatus = blocked ? "active" : AppConstants.STATUS_BLOCKED;
        String message = blocked ? "Bạn muốn mở khóa tài khoản này?" : "Bạn muốn khóa tài khoản này?";

        new AlertDialog.Builder(this)
                .setTitle(blocked ? "Mở khóa tài khoản" : "Khóa tài khoản")
                .setMessage(message)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đồng ý", (dialog, which) -> updateStatus(nextStatus))
                .show();
    }

    private void updateStatus(String nextStatus) {
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy UID", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonToggleStatus.setEnabled(false);
        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userId)
                .update("status", nextStatus)
                .addOnSuccessListener(unused -> {
                    currentStatus = nextStatus;
                    updateStatusViews();
                    Toast.makeText(this, "Đã cập nhật trạng thái tài khoản", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Không cập nhật được: " + error.getMessage(), Toast.LENGTH_LONG).show())
                .addOnCompleteListener(task -> buttonToggleStatus.setEnabled(true));
    }

    private void updateStatusViews() {
        boolean blocked = AppConstants.STATUS_BLOCKED.equalsIgnoreCase(currentStatus);
        textStatus.setText("Trạng thái: " + (blocked ? "Đã khóa" : valueOrDefault(currentStatus, "active")));
        buttonToggleStatus.setText(blocked ? "Mở khóa tài khoản" : "Khóa tài khoản");
        buttonToggleStatus.setBackgroundColor(Color.parseColor(blocked ? "#2E7D32" : "#B00020"));
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}

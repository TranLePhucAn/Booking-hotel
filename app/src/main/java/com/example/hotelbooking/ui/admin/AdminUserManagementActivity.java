package com.example.hotelbooking.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.adapter.UserManagementAdapter;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdminUserManagementActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private UserManagementAdapter adapter;
    private final List<DocumentSnapshot> allUsers = new ArrayList<>();

    private TextView textSummary;
    private ProgressBar progressLoading;
    private String currentFilter = "all";
    private String currentAdminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        firestore = FirebaseFirestore.getInstance();
        currentAdminId = FirebaseAuth.getInstance().getUid();

        bindViews();
        setupRecyclerView();
        loadUsers();
    }

    private void bindViews() {
        TextView buttonBack = findViewById(R.id.buttonBack);
        Button buttonRefresh = findViewById(R.id.buttonRefresh);
        Button filterAll = findViewById(R.id.filterAll);
        Button filterUser = findViewById(R.id.filterUser);
        Button filterPartner = findViewById(R.id.filterPartner);
        Button filterAdmin = findViewById(R.id.filterAdmin);
        Button filterBlocked = findViewById(R.id.filterBlocked);

        textSummary = findViewById(R.id.textSummary);
        progressLoading = findViewById(R.id.progressLoading);

        buttonBack.setOnClickListener(v -> finish());
        buttonRefresh.setOnClickListener(v -> loadUsers());
        filterAll.setOnClickListener(v -> applyFilter("all"));
        filterUser.setOnClickListener(v -> applyFilter(AppConstants.ROLE_USER));
        filterPartner.setOnClickListener(v -> applyFilter(AppConstants.ROLE_PARTNER));
        filterAdmin.setOnClickListener(v -> applyFilter(AppConstants.ROLE_ADMIN));
        filterBlocked.setOnClickListener(v -> applyFilter(AppConstants.STATUS_BLOCKED));
    }

    private void setupRecyclerView() {
        RecyclerView recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserManagementAdapter(new UserManagementAdapter.OnUserActionListener() {
            @Override
            public void onViewDetail(DocumentSnapshot userDocument) {
                openUserDetail(userDocument);
            }

            @Override
            public void onToggleStatus(DocumentSnapshot userDocument) {
                confirmToggleUserStatus(userDocument);
            }
        });
        recyclerUsers.setAdapter(adapter);
    }

    private void loadUsers() {
        progressLoading.setVisibility(View.VISIBLE);
        textSummary.setText("Đang tải người dùng...");

        firestore.collection(AppConstants.COLLECTION_USERS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allUsers.clear();
                    allUsers.addAll(querySnapshot.getDocuments());
                    allUsers.sort(Comparator.comparing(this::displayNameForSort));
                    progressLoading.setVisibility(View.GONE);
                    applyFilter(currentFilter);
                })
                .addOnFailureListener(error -> {
                    progressLoading.setVisibility(View.GONE);
                    textSummary.setText("Không tải được người dùng");
                    Toast.makeText(this, "Không tải được người dùng: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        List<DocumentSnapshot> result = new ArrayList<>();

        for (DocumentSnapshot document : allUsers) {
            String role = stringValue(document, "role", AppConstants.ROLE_USER);
            String status = stringValue(document, "status", "active");

            boolean matches;
            if ("all".equals(filter)) {
                matches = true;
            } else if (AppConstants.STATUS_BLOCKED.equals(filter)) {
                matches = AppConstants.STATUS_BLOCKED.equalsIgnoreCase(status);
            } else {
                matches = filter.equalsIgnoreCase(role);
            }

            if (matches) {
                result.add(document);
            }
        }

        adapter.updateData(result);
        textSummary.setText("Đang hiển thị " + result.size() + "/" + allUsers.size() + " tài khoản");
    }

    private void openUserDetail(DocumentSnapshot document) {
        Intent intent = new Intent(this, AdminUserDetailActivity.class);
        intent.putExtra("uid", document.getId());
        intent.putExtra("fullName", firstString(document, "Chưa cập nhật tên", "fullName", "displayName", "name"));
        intent.putExtra("email", firstString(document, "Chưa có email", "email"));
        intent.putExtra("phone", firstString(document, "Chưa cập nhật", "phone", "phoneNumber"));
        intent.putExtra("role", stringValue(document, "role", AppConstants.ROLE_USER));
        intent.putExtra("partnerStatus", firstString(document, "", "partnerStatus", "partner_status"));
        intent.putExtra("status", stringValue(document, "status", "active"));
        startActivity(intent);
    }

    private void confirmToggleUserStatus(DocumentSnapshot document) {
        String userId = document.getId();
        if (currentAdminId != null && currentAdminId.equals(userId)) {
            Toast.makeText(this, "Admin không thể tự khóa tài khoản của mình", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = stringValue(document, "status", "active");
        boolean blocked = AppConstants.STATUS_BLOCKED.equalsIgnoreCase(status);
        String nextStatus = blocked ? "active" : AppConstants.STATUS_BLOCKED;
        String title = blocked ? "Mở khóa tài khoản" : "Khóa tài khoản";
        String message = blocked
                ? "Bạn muốn mở khóa tài khoản này?"
                : "Bạn muốn khóa tài khoản này?";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đồng ý", (dialog, which) -> updateUserStatus(userId, nextStatus))
                .show();
    }

    private void updateUserStatus(String userId, String status) {
        firestore.collection(AppConstants.COLLECTION_USERS)
                .document(userId)
                .update("status", status)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật trạng thái tài khoản", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Không cập nhật được: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String displayNameForSort(DocumentSnapshot document) {
        return firstString(document, "", "fullName", "displayName", "name", "email").toLowerCase(Locale.ROOT);
    }

    private String stringValue(DocumentSnapshot document, String field, String fallback) {
        String value = document.getString(field);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String firstString(DocumentSnapshot document, String fallback, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return fallback;
    }
}

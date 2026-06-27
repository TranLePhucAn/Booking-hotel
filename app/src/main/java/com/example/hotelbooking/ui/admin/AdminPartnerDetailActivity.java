package com.example.hotelbooking.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.PartnerApplication;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.viewmodels.PartnerViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminPartnerDetailActivity extends AppCompatActivity {
    private PartnerViewModel viewModel;
    private PartnerApplication application;
    private String applicationId;
    private FirebaseFirestore db;

    private TextView tvName, tvRep, tvPhone, tvEmail, tvAddress, tvTax, tvDesc, tvStatus;
    private EditText etNote;
    private ImageView ivVerificationFile;
    private Button btnViewFile, btnApprove, btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_partner_detail);

        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(PartnerViewModel.class);

        initViews();

        applicationId = getIntent().getStringExtra("applicationId");

        if (applicationId == null || applicationId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin hồ sơ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadApplicationDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (applicationId != null) {
            loadApplicationDetails();
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tvDetailBusinessName);
        tvRep = findViewById(R.id.tvDetailRepName);
        tvPhone = findViewById(R.id.tvDetailPhone);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvAddress = findViewById(R.id.tvDetailAddress);
        tvTax = findViewById(R.id.tvDetailTaxCode);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvStatus = findViewById(R.id.tvApplicationStatus);
        etNote = findViewById(R.id.etAdminNote);
        ivVerificationFile = findViewById(R.id.ivVerificationFile);
        btnViewFile = findViewById(R.id.btnViewVerificationFile);
        btnApprove = findViewById(R.id.btnApproveApp);
        btnReject = findViewById(R.id.btnRejectApp);
    }

    private void loadApplicationDetails() {
        db.collection(AppConstants.COLLECTION_PARTNER_APPLICATIONS).document(applicationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        application = documentSnapshot.toObject(PartnerApplication.class);
                        if (application != null) {
                            application.setId(documentSnapshot.getId());
                            application.setUserId(documentSnapshot.getString("user_id"));
                            displayDetails();
                        } else {
                            Toast.makeText(this, "Không đọc được dữ liệu hồ sơ", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Hồ sơ không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải hồ sơ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayDetails() {
        tvName.setText(application.getBusinessName());
        tvRep.setText(application.getRepresentativeName());
        tvPhone.setText(application.getPhone());
        tvEmail.setText(application.getEmail());
        tvAddress.setText(application.getAddress());
        tvTax.setText(application.getTaxCode());
        tvDesc.setText(application.getDescription());

        if (tvStatus != null) {
            String status = application.getStatus();
            if (status == null) status = AppConstants.STATUS_PENDING;

            switch (status) {
                case AppConstants.STATUS_APPROVED:
                    tvStatus.setText("Đã duyệt");
                    tvStatus.setBackgroundResource(R.drawable.status_approved);
                    break;
                case AppConstants.STATUS_REJECTED:
                    tvStatus.setText("Đã từ chối");
                    tvStatus.setBackgroundResource(R.drawable.status_rejected);
                    break;
                default:
                    tvStatus.setText("Đang chờ duyệt");
                    tvStatus.setBackgroundResource(R.drawable.status_pending);
                    break;
            }
        }

        etNote.setText(application.getAdminNote());

        if (AppConstants.STATUS_APPROVED.equals(application.getStatus())
                || AppConstants.STATUS_REJECTED.equals(application.getStatus())) {
            btnApprove.setEnabled(false);
            btnReject.setEnabled(false);
            etNote.setEnabled(false);
        }

        String url = application.getVerificationFileUrl();

        if (url != null && !url.isEmpty()) {
            if (url.endsWith(".jpg")
                    || url.endsWith(".jpeg")
                    || url.endsWith(".png")) {
                ivVerificationFile.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivVerificationFile);
            } else {
                ivVerificationFile.setVisibility(View.GONE);
            }
        } else {
            ivVerificationFile.setVisibility(View.GONE);
        }

        btnViewFile.setOnClickListener(v -> {
            if (url == null || url.isEmpty()) {
                Toast.makeText(this, "Không có file xác minh", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có ứng dụng để mở file", Toast.LENGTH_SHORT).show();
            }
        });

        // ĐÃ SỬA: Đưa Toast và finish() vào callback onSuccess
        btnApprove.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Duyệt hồ sơ")
                    .setMessage("Bạn có chắc muốn duyệt hồ sơ này?")
                    .setPositiveButton("Duyệt", (dialog, which) -> {
                        btnApprove.setEnabled(false);
                        btnReject.setEnabled(false);

                        // Callback () -> { ... } sẽ được gọi KHI ĐÃ LƯU XONG trên Firebase
                        viewModel.approveApplication(
                                application,
                                etNote.getText().toString(),
                                () -> {
                                    Toast.makeText(this, "Đã duyệt hồ sơ đối tác", Toast.LENGTH_SHORT).show();
                                    finish(); // Đợi Firebase lưu xong rồi mới finish
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnReject.setOnClickListener(v -> showRejectPartnerDialog());
    }

    private void showRejectPartnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Từ chối hồ sơ đối tác");
        builder.setMessage("Vui lòng nhập lý do từ chối:");

        final EditText input = new EditText(this);
        input.setHint("Lý do từ chối...");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        // ĐÃ SỬA: Đưa Toast và finish() vào callback onSuccess
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(this, "Lý do từ chối không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Callback () -> { ... }
            viewModel.rejectApplication(
                    application.getId(),
                    application.getUserId(),
                    reason,
                    () -> {
                        Toast.makeText(this, "Đã từ chối hồ sơ đối tác", Toast.LENGTH_SHORT).show();
                        finish(); // Đợi Firebase lưu xong rồi mới finish
                    });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
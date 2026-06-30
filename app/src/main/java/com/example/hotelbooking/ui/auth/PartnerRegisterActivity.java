package com.example.hotelbooking.ui.auth;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PartnerRegisterActivity extends AppCompatActivity {

    // Thông tin tài khoản
    private EditText edtOwnerName, edtPartnerEmail, edtPartnerPhone, edtPartnerPassword;

    // Thông tin khách sạn
    private EditText edtBusinessName, edtBusinessAddress, edtBusinessType;

    // Upload file
    private Button btnPickFile;
    private TextView tvSelectedFileName, tvPdfIndicator;
    private ImageView ivFilePreview;
    private Button btnPartnerRegister;

    private LoadingDialog loadingDialog;
    private FirebaseFirestore db;

    // Trạng thái: người dùng đã đăng nhập hay chưa
    private boolean isLoggedIn = false;
    private String uid;

    // File đã chọn
    private Uri selectedFileUri = null;
    private boolean isPdf = false;

    // Launcher chọn file (ảnh hoặc PDF)
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    String path = uri.getLastPathSegment();
                    String fileName = path != null ? path : "file_xac_minh";

                    String mimeType = getContentResolver().getType(uri);
                    isPdf = mimeType != null && mimeType.equals("application/pdf");

                    tvSelectedFileName.setText("✓ " + fileName);
                    tvSelectedFileName.setTextColor(getResources().getColor(R.color.ocean_blue));

                    if (isPdf) {
                        ivFilePreview.setVisibility(View.GONE);
                        tvPdfIndicator.setVisibility(View.VISIBLE);
                    } else {
                        tvPdfIndicator.setVisibility(View.GONE);
                        ivFilePreview.setVisibility(View.VISIBLE);
                        Glide.with(this).load(uri).into(ivFilePreview);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_register);

        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);

        bindViews();

        // Kiểm tra người dùng đã đăng nhập chưa
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            isLoggedIn = true;
            uid = currentUser.getUid();
            // Tự điền thông tin + ẩn ô mật khẩu
            loadCurrentUserInfo();
            edtPartnerPassword.setVisibility(View.GONE);
        }

        btnPickFile.setOnClickListener(v ->
                filePickerLauncher.launch(new String[]{"application/pdf", "image/jpeg", "image/png"})
        );

        btnPartnerRegister.setOnClickListener(v -> submitApplication());
    }

    private void bindViews() {
        edtOwnerName       = findViewById(R.id.edtOwnerName);
        edtPartnerEmail    = findViewById(R.id.edtPartnerEmail);
        edtPartnerPhone    = findViewById(R.id.edtPartnerPhone);
        edtPartnerPassword = findViewById(R.id.edtPartnerPassword);
        edtBusinessName    = findViewById(R.id.edtBusinessName);
        edtBusinessAddress = findViewById(R.id.edtBusinessAddress);
        edtBusinessType    = findViewById(R.id.edtBusinessType);
        btnPickFile        = findViewById(R.id.btnPickFile);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        tvPdfIndicator     = findViewById(R.id.tvPdfIndicator);
        ivFilePreview      = findViewById(R.id.ivFilePreview);
        btnPartnerRegister = findViewById(R.id.btnPartnerRegister);
    }

    /**
     * Tự động lấy thông tin từ tài khoản đã đăng nhập và điền vào form.
     */
    private void loadCurrentUserInfo() {
        db.collection(AppConstants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name  = document.getString("fullName");
                        String email = document.getString("email");
                        String phone = document.getString("phone");

                        edtOwnerName.setText(name != null ? name : "");
                        edtPartnerEmail.setText(email != null ? email : "");
                        edtPartnerPhone.setText(phone != null ? phone : "");
                    } else {
                        // Fallback từ FirebaseAuth
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            edtOwnerName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
                            edtPartnerEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        edtOwnerName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
                        edtPartnerEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                    }
                });
    }

    private void submitApplication() {
        String ownerName    = textOf(edtOwnerName);
        String email        = textOf(edtPartnerEmail);
        String phone        = textOf(edtPartnerPhone);
        String businessName = textOf(edtBusinessName);
        String address      = textOf(edtBusinessAddress);

        // Validate
        if (TextUtils.isEmpty(ownerName)) {
            edtOwnerName.setError("Vui lòng nhập tên người đại diện");
            edtOwnerName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtPartnerEmail.setError("Vui lòng nhập email");
            edtPartnerEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(businessName)) {
            edtBusinessName.setError("Vui lòng nhập tên khách sạn");
            edtBusinessName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            edtBusinessAddress.setError("Vui lòng nhập địa chỉ");
            edtBusinessAddress.requestFocus();
            return;
        }

        // Nếu chưa đăng nhập → cần mật khẩu để tạo tài khoản mới
        if (!isLoggedIn) {
            String password = textOf(edtPartnerPassword);
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                edtPartnerPassword.setError("Mật khẩu tối thiểu 6 ký tự");
                edtPartnerPassword.requestFocus();
                return;
            }
            // Tạo tài khoản mới rồi gửi hồ sơ
            loadingDialog.show();
            createAccountAndSubmit(ownerName, email, phone, password);
            return;
        }


        loadingDialog.show();
        if (selectedFileUri != null) {
            uploadFileAndSave();
        } else {
            saveApplicationToFirestore("");
        }
    }

  
    private void createAccountAndSubmit(String name, String email, String phone, String password) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    uid = authResult.getUser().getUid();
                    isLoggedIn = true;

                    // Lưu user vào Firestore
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", uid);
                    userData.put("fullName", name);
                    userData.put("email", email);
                    userData.put("phone", phone);
                    userData.put("role", AppConstants.ROLE_USER);
                    userData.put("partnerStatus", AppConstants.STATUS_PENDING);

                    db.collection(AppConstants.COLLECTION_USERS).document(uid).set(userData)
                            .addOnSuccessListener(v -> {
                                if (selectedFileUri != null) {
                                    uploadFileAndSave();
                                } else {
                                    saveApplicationToFirestore("");
                                }
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Lỗi lưu user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void uploadFileAndSave() {
        String ext = isPdf ? ".pdf" : ".jpg";
        String path = "partner_verification/" + uid + "/verification" + ext;
        StorageReference ref = FirebaseStorage.getInstance().getReference(path);

        ref.putFile(selectedFileUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(downloadUrl -> saveApplicationToFirestore(downloadUrl.toString()))
                        .addOnFailureListener(e -> {
                            loadingDialog.dismiss();
                            Toast.makeText(this, "Lỗi lấy URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        })
                )
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Lỗi upload file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Lưu hồ sơ đăng ký vào Firestore partner_applications.
     */
    private void saveApplicationToFirestore(String fileUrl) {
        Map<String, Object> data = new HashMap<>();

        data.put("user_id", uid);
        data.put("representative_name", textOf(edtOwnerName));
        data.put("email", textOf(edtPartnerEmail));
        data.put("phone", textOf(edtPartnerPhone));
        data.put("business_name", textOf(edtBusinessName));
        data.put("address", textOf(edtBusinessAddress));
        data.put("description", textOf(edtBusinessType));
        data.put("verification_file_url", fileUrl);
        data.put("status", AppConstants.STATUS_PENDING);
        data.put("admin_note", "");
        data.put("created_at", FieldValue.serverTimestamp());

        db.collection(AppConstants.COLLECTION_PARTNER_APPLICATIONS)
                .document(uid)
                .set(data)
                .addOnSuccessListener(v -> {
                    // Cập nhật partnerStatus = pending trên user
                    db.collection(AppConstants.COLLECTION_USERS)
                            .document(uid)
                            .update("partnerStatus", AppConstants.STATUS_PENDING)
                            .addOnCompleteListener(task -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this,
                                        "Đăng ký cộng sự thành công!\nVui lòng chờ quản trị viên xét duyệt.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Lỗi gửi hồ sơ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }
}
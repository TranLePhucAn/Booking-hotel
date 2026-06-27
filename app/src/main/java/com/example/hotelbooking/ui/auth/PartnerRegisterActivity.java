package com.example.hotelbooking.ui.auth;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.example.hotelbooking.data.model.PartnerApplication;
import com.example.hotelbooking.data.model.User;
import com.example.hotelbooking.data.repository.PartnerRepository;
import com.example.hotelbooking.data.repository.UserRepository;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.utils.LoadingDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PartnerRegisterActivity extends AppCompatActivity {

    private EditText edtOwnerName, edtPartnerEmail, edtPartnerPhone, edtPartnerPassword;
    private EditText edtBusinessName, edtBusinessAddress, edtBusinessType;

    private Button btnPickFile, btnPartnerRegister;
    private TextView tvSelectedFileName, tvPdfIndicator;
    private ImageView ivFilePreview;

    private LoadingDialog loadingDialog;
    private UserRepository userRepository;
    private PartnerRepository partnerRepository;

    // File đã chọn
    private Uri selectedFileUri = null;
    private String selectedFileName = "";
    private boolean isPdf = false;

    // Launcher để chọn file từ thiết bị (ảnh hoặc PDF)
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    // Lấy tên file từ URI
                    String path = uri.getLastPathSegment();
                    selectedFileName = path != null ? path : "file_xac_minh";

                    // Kiểm tra loại file
                    String mimeType = getContentResolver().getType(uri);
                    isPdf = mimeType != null && mimeType.equals("application/pdf");

                    // Cập nhật UI
                    tvSelectedFileName.setText("✓ " + selectedFileName);
                    tvSelectedFileName.setTextColor(getResources().getColor(R.color.ocean_blue));

                    if (isPdf) {
                        // Hiện chỉ báo PDF
                        ivFilePreview.setVisibility(View.GONE);
                        tvPdfIndicator.setVisibility(View.VISIBLE);
                    } else {
                        // Hiện preview ảnh bằng Glide
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

        userRepository = new UserRepository();
        partnerRepository = new PartnerRepository();
        loadingDialog = new LoadingDialog(this);

        bindViews();

        btnPickFile.setOnClickListener(v -> {
            // Mở file picker — chấp nhận PDF, JPG, PNG
            filePickerLauncher.launch(new String[]{"application/pdf", "image/jpeg", "image/png"});
        });

        btnPartnerRegister.setOnClickListener(v -> registerPartner());
    }

    private void bindViews() {
        edtOwnerName        = findViewById(R.id.edtOwnerName);
        edtPartnerEmail     = findViewById(R.id.edtPartnerEmail);
        edtPartnerPhone     = findViewById(R.id.edtPartnerPhone);
        edtPartnerPassword  = findViewById(R.id.edtPartnerPassword);
        edtBusinessName     = findViewById(R.id.edtBusinessName);
        edtBusinessAddress  = findViewById(R.id.edtBusinessAddress);
        edtBusinessType     = findViewById(R.id.edtBusinessType);

        btnPickFile         = findViewById(R.id.btnPickFile);
        btnPartnerRegister  = findViewById(R.id.btnPartnerRegister);
        tvSelectedFileName  = findViewById(R.id.tvSelectedFileName);
        tvPdfIndicator      = findViewById(R.id.tvPdfIndicator);
        ivFilePreview       = findViewById(R.id.ivFilePreview);
    }

    private void registerPartner() {
        String ownerName     = textOf(edtOwnerName);
        String email         = textOf(edtPartnerEmail);
        String phone         = textOf(edtPartnerPhone);
        String password      = textOf(edtPartnerPassword);
        String businessName  = textOf(edtBusinessName);
        String address       = textOf(edtBusinessAddress);

        // Validate các trường bắt buộc
        if (TextUtils.isEmpty(ownerName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(businessName) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không đúng định dạng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();

        // Bước 1: Tạo tài khoản Firebase Auth
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || FirebaseAuth.getInstance().getCurrentUser() == null) {
                        loadingDialog.dismiss();
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Không tạo được tài khoản";
                        Toast.makeText(this, "Lỗi Auth: " + msg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Bước 2: Cập nhật displayName
                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(
                            new UserProfileChangeRequest.Builder().setDisplayName(ownerName).build()
                    ).addOnCompleteListener(profileTask -> {
                        if (selectedFileUri != null) {
                            // Bước 3a: Upload file lên Firebase Storage → rồi lưu hồ sơ
                            uploadFileAndSave(uid, ownerName, email, phone, businessName, address);
                        } else {
                            // Bước 3b: Không có file → lưu hồ sơ với URL rỗng
                            savePartnerApplication(uid, ownerName, email, phone, businessName, address, "");
                        }
                    });
                });
    }

    /**
     * Upload file xác minh lên Firebase Storage, sau đó lưu hồ sơ.
     */
    private void uploadFileAndSave(String uid, String ownerName, String email, String phone,
                                   String businessName, String address) {
        // Đường dẫn Storage: partner_verification/{uid}/{fileName}
        String ext = isPdf ? ".pdf" : ".jpg";
        String storagePath = AppConstants.STORAGE_PARTNER_VERIFICATION + "/" + uid + "/verification" + ext;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference(storagePath);

        storageRef.putFile(selectedFileUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    loadingDialog.show(); // giữ dialog hiển thị
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy download URL
                    storageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String fileUrl = downloadUri.toString();
                                savePartnerApplication(uid, ownerName, email, phone, businessName, address, fileUrl);
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Lỗi lấy URL file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Lỗi upload file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Lưu User + PartnerApplication vào Firestore.
     */
    private void savePartnerApplication(String uid, String ownerName, String email, String phone,
                                        String businessName, String address, String fileUrl) {
        // Tạo user với role = user, partnerStatus = pending
        User user = new User(uid, ownerName, email, phone, AppConstants.ROLE_USER, AppConstants.STATUS_PENDING);
        user.setCreatedAt(Timestamp.now());

        // Tạo hồ sơ đăng ký cộng sự
        PartnerApplication app = new PartnerApplication();
        app.setId(uid);
        app.setUserId(uid);
        app.setBusinessName(businessName);
        app.setRepresentativeName(ownerName);
        app.setEmail(email);
        app.setPhone(phone);
        app.setAddress(address);
        app.setDescription(textOf(edtBusinessType));
        app.setTaxCode("");
        app.setVerificationFileUrl(fileUrl);
        app.setStatus(AppConstants.STATUS_PENDING);
        app.setAdminNote("");
        app.setCreatedAt(Timestamp.now());

        userRepository.saveUser(user)
                .addOnSuccessListener(v -> {
                    partnerRepository.submitApplication(app)
                            .addOnSuccessListener(v2 -> {
                                loadingDialog.dismiss();
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(this,
                                        "Đăng ký thành công! Vui lòng chờ Admin phê duyệt hồ sơ.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Lỗi lưu hồ sơ: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Lỗi lưu user: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }
}
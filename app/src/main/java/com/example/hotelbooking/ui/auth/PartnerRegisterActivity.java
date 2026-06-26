package com.example.hotelbooking.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class PartnerRegisterActivity extends AppCompatActivity {

    private EditText edtOwnerName;
    private EditText edtPartnerEmail;
    private EditText edtPartnerPhone;
    private EditText edtPartnerPassword;
    private EditText edtBusinessName;
    private EditText edtBusinessAddress;
    private EditText edtBusinessType;
    private EditText edtLicenseInfo;
    private LoadingDialog loadingDialog;

    private UserRepository userRepository;
    private PartnerRepository partnerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_register);

        userRepository = new UserRepository();
        partnerRepository = new PartnerRepository();

        bindViews();
        loadingDialog = new LoadingDialog(this);

        Button btnPartnerRegister = findViewById(R.id.btnPartnerRegister);
        btnPartnerRegister.setOnClickListener(v -> registerPartner());
    }

    private void bindViews() {
        edtOwnerName = findViewById(R.id.edtOwnerName);
        edtPartnerEmail = findViewById(R.id.edtPartnerEmail);
        edtPartnerPhone = findViewById(R.id.edtPartnerPhone);
        edtPartnerPassword = findViewById(R.id.edtPartnerPassword);
        edtBusinessName = findViewById(R.id.edtBusinessName);
        edtBusinessAddress = findViewById(R.id.edtBusinessAddress);
        edtBusinessType = findViewById(R.id.edtBusinessType);
        edtLicenseInfo = findViewById(R.id.edtLicenseInfo);
    }

    private void registerPartner() {
        String ownerName = textOf(edtOwnerName);
        String email = textOf(edtPartnerEmail);
        String phone = textOf(edtPartnerPhone);
        String password = textOf(edtPartnerPassword);
        String businessName = textOf(edtBusinessName);
        String address = textOf(edtBusinessAddress);

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

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || FirebaseAuth.getInstance().getCurrentUser() == null) {
                        loadingDialog.dismiss();
                        String message = task.getException() != null ? task.getException().getMessage() : "Không tạo được tài khoản";
                        Toast.makeText(this, "Lỗi Auth: " + message, Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(
                            new UserProfileChangeRequest.Builder().setDisplayName(ownerName).build()
                    ).addOnCompleteListener(profileTask -> {

                        savePartnerApplication(uid, ownerName, email, phone, businessName, address);
                    });
                });
    }

    private void savePartnerApplication(String uid, String ownerName, String email, String phone, String businessName, String address) {
        User user = new User(uid, ownerName, email, phone, AppConstants.ROLE_USER, AppConstants.STATUS_PENDING);
        user.setCreatedAt(Timestamp.now());

        PartnerApplication app = new PartnerApplication();
        app.setId(uid);
        app.setUserId(uid);
        app.setBusinessName(businessName);
        app.setRepresentativeName(ownerName);
        app.setEmail(email);
        app.setPhone(phone);
        app.setAddress(address);
        app.setDescription(textOf(edtBusinessType));
        app.setTaxCode(textOf(edtLicenseInfo));
        app.setVerificationFileUrl("");
        app.setStatus(AppConstants.STATUS_PENDING);
        app.setAdminNote("");
        app.setCreatedAt(Timestamp.now());

        userRepository.saveUser(user)
                .addOnSuccessListener(unusedUser -> {
                    partnerRepository.submitApplication(app)
                            .addOnSuccessListener(unusedApp -> {
                                loadingDialog.dismiss();
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(this, "Đăng ký thành công! Vui lòng chờ Admin phê duyệt hồ sơ.", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Lỗi lưu bảng hồ sơ đối tác: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Lỗi lưu thông tin user cơ bản: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }
}
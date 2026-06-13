package com.example.hotelbooking.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_register);

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
            Toast.makeText(this, "Vui long nhap day du thong tin bat buoc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email khong dung dinh dang", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mat khau phai co it nhat 6 ky tu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 9) {
            Toast.makeText(this, "So dien thoai khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || FirebaseAuth.getInstance().getCurrentUser() == null) {
                        loadingDialog.dismiss();
                        String message = task.getException() != null ? task.getException().getMessage() : "Khong tao duoc tai khoan";
                        Toast.makeText(this, "Dang ky doi tac that bai: " + message, Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(
                            new UserProfileChangeRequest.Builder().setDisplayName(ownerName).build());
                    savePartner(uid, ownerName, email, phone, businessName, address);
                });
    }

    private void savePartner(String uid, String ownerName, String email, String phone, String businessName, String address) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("fullName", ownerName);
        userData.put("role", "owner");
        userData.put("partner_status", "pending");

        Map<String, Object> partnerData = new HashMap<>();
        partnerData.put("uid", uid);
        partnerData.put("business_name", businessName);
        partnerData.put("owner_name", ownerName);
        partnerData.put("email", email);
        partnerData.put("phone", phone);
        partnerData.put("business_type", textOf(edtBusinessType));
        partnerData.put("address", address);
        partnerData.put("license_info", textOf(edtLicenseInfo));
        partnerData.put("status", "pending");
        partnerData.put("created_at", System.currentTimeMillis());

        db.collection("users").document(uid).set(userData)
                .continueWithTask(task -> db.collection("businesses").document(uid).set(partnerData))
                .addOnSuccessListener(unused -> {
                    loadingDialog.dismiss();
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(this, "Da gui dang ky doi tac. Vui long cho quan tri vien duyet.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Khong luu duoc ho so doi tac: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }
}

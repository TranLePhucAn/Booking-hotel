package com.example.hotelbooking.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnRegister;

    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        auth = FirebaseClient.getAuth();
        loadingDialog = new LoadingDialog(this);
        btnRegister.setOnClickListener(v -> register());
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void register() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui long nhap day du thong tin", Toast.LENGTH_SHORT).show();
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

        loadingDialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (!task.isSuccessful() || auth.getCurrentUser() == null) {
                        String message = task.getException() != null ? task.getException().getMessage() : "Khong tao duoc tai khoan";
                        Toast.makeText(this, "Loi tao tai khoan: " + message, Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build());
                    saveUserProfile(firebaseUser.getUid(), name, email);
                });
    }

    private void saveUserProfile(String uid, String name, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("fullName", name);
        userData.put("email", email);
        userData.put("role", "customer");
        userData.put("phone", "");
        userData.put("avatarUrl", "");
        userData.put("gender", "");
        userData.put("date_of_birth", "");
        userData.put("country", "");
        userData.put("created_at", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    auth.signOut();
                    Toast.makeText(this, "Dang ky thanh cong. Vui long dang nhap.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    auth.signOut();
                    Toast.makeText(this, "Tai khoan da tao, nhung chua luu duoc ho so: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}

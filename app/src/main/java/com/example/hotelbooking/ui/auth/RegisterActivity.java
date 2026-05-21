package com.example.hotelbooking.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.User;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword;
    private Button btnRegister;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();

        auth = FirebaseClient.getAuth();
        databaseReference = FirebaseClient.getDatabaseReference();
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
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();

        // 1. Tạo tài khoản trên Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // 2. Tạo đối tượng User để lưu lên Realtime Database
                            User user = new User(uid, name, email);

                            // Lưu vào nhánh "Users" -> "uid"
                            databaseReference.child("Users").child(uid).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        loadingDialog.dismiss();

                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                            finish(); // Đóng màn hình đăng ký, về lại đăng nhập
                                        } else {
                                            Toast.makeText(this, "Lỗi lưu dữ liệu: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Lỗi tạo tài khoản: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
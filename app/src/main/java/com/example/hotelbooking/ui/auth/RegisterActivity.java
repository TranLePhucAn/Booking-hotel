package com.example.hotelbooking.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hotelbooking.R;
import com.example.hotelbooking.utils.LoadingDialog;
import com.example.hotelbooking.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnRegister;

    private AuthViewModel authViewModel;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        loadingDialog = new LoadingDialog(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        btnRegister.setOnClickListener(v -> register());
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void observeViewModel() {
        authViewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) loadingDialog.show();
            else loadingDialog.dismiss();
        });

        authViewModel.userSession.observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                authViewModel.logout();
                Toast.makeText(this, "Đăng ký thành công. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        authViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void register() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập Email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không đúng định dạng");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        // Regex mạnh: ít nhất 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt, tối thiểu 8 ký tự
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!password.matches(passwordPattern)) {
            edtPassword.setError("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            edtPassword.requestFocus();
            return;
        }

        authViewModel.register(email, password, name, "");
    }
}

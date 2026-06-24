package com.example.hotelbooking.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.User;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.utils.LoadingDialog;
import com.example.hotelbooking.utils.RoleRouter;
import com.example.hotelbooking.viewmodels.AuthViewModel;
import com.example.hotelbooking.viewmodels.UserViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView txtRegister;
    private TextView txtPartnerRegister;

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        loadingDialog = new LoadingDialog(this);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        observeViewModels();

        btnLogin.setOnClickListener(v -> login());
        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        
        if (txtPartnerRegister != null) {
            txtPartnerRegister.setOnClickListener(v ->
                    startActivity(new Intent(this, PartnerRegisterActivity.class)));
        }
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);
        txtPartnerRegister = findViewById(R.id.txtPartnerRegister);
    }

    private void observeViewModels() {
        authViewModel.userSession.observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                userViewModel.fetchUserData(firebaseUser.getUid());
            }
        });

        authViewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) loadingDialog.show();
            else loadingDialog.dismiss();
        });

        authViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi đăng nhập: " + error, Toast.LENGTH_LONG).show();
            }
        });

        userViewModel.userData.observe(this, user -> {
            if (user != null) {
                String role = user.getRole();
                String partnerStatus = user.getPartnerStatus();
                if (AppConstants.ROLE_USER.equalsIgnoreCase(role)) {
                    finish();
                } else {
                    RoleRouter.navigateByRole(this, role, partnerStatus);
                    finish();
                }
            }
        });

        userViewModel.error.observe(this, error -> {
            if (error != null) {
                if (authViewModel.userSession.getValue() != null) {
                    createDefaultUser(authViewModel.userSession.getValue().getUid(),
                            authViewModel.userSession.getValue().getEmail());
                }
            }
        });
    }
    private void createDefaultUser(String uid, String email) {
        User newUser = new User(uid, "Người dùng", email, "", AppConstants.ROLE_USER, null);

        FirebaseFirestore.getInstance().collection(AppConstants.COLLECTION_USERS)
                .document(uid).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    finish();
                });
    }

    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không đúng định dạng", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.login(email, password);
    }
}

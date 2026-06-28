package com.example.hotelbooking.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.ui.home.HomeActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.utils.LoadingDialog;
import com.example.hotelbooking.utils.RoleRouter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_EMAIL = "saved_email";

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private CheckBox cbRememberMe;
    private TextView txtRegister;
    private TextView txtPartnerRegister;

    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        auth = FirebaseClient.getAuth();
        loadingDialog = new LoadingDialog(this);

        // Khôi phục email đã lưu nếu người dùng đã tích "Ghi nhớ đăng nhập"
        if (prefs.getBoolean(KEY_REMEMBER, false)) {
            String savedEmail = prefs.getString(KEY_EMAIL, "");
            edtEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }

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
        cbRememberMe = findViewById(R.id.cbRememberMe);
        txtRegister = findViewById(R.id.txtRegister);
        txtPartnerRegister = findViewById(R.id.txtPartnerRegister);
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

        // Lưu hoặc xoá email tuỳ trạng thái checkbox
        SharedPreferences.Editor editor = prefs.edit();
        if (cbRememberMe != null && cbRememberMe.isChecked()) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_EMAIL, email);
        } else {
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_EMAIL);
        }
        editor.apply();

        setLoginLoading(true);
        loadingDialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        openHomeForCustomerOrRouteRole();
                    } else {
                        loadingDialog.dismiss();
                        setLoginLoading(false);
                        String message = getLoginErrorMessage(task.getException());
                        Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openHomeForCustomerOrRouteRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            loadingDialog.dismiss();
            setLoginLoading(false);
            Toast.makeText(this, "Không lấy được thông tin tài khoản", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String partnerStatus = documentSnapshot.getString("partnerStatus");
                        String accountStatus = documentSnapshot.getString("status");

                        if (AppConstants.STATUS_BLOCKED.equalsIgnoreCase(accountStatus)) {
                            auth.signOut();
                            loadingDialog.dismiss();
                            setLoginLoading(false);
                            Toast.makeText(this, "TÃ i khoáº£n cá»§a báº¡n Ä‘Ã£ bá»‹ khÃ³a", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                            RoleRouter.routeCurrentUser(this, loadingDialog);
                            return;
                        }

                        if (AppConstants.ROLE_PARTNER.equalsIgnoreCase(role)) {
                            if (AppConstants.STATUS_APPROVED.equalsIgnoreCase(partnerStatus)) {
                                RoleRouter.routeCurrentUser(this, loadingDialog);
                                return;
                            }
                        }
                    } else {
                        createCustomerProfile(user);
                    }
                    loadingDialog.dismiss();
                    openHome();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    openHome();
                });
    }

    private void setLoginLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setAlpha(loading ? 0.65f : 1f);
        btnLogin.setText(loading ? "ĐANG ĐĂNG NHẬP..." : "ĐĂNG NHẬP");
    }

    private void createCustomerProfile(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getDisplayName());
        userData.put("role", AppConstants.ROLE_USER);

        FirebaseFirestore.getInstance()
                .collection(AppConstants.COLLECTION_USERS)
                .document(user.getUid())
                .set(userData);
    }

    private void openHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getLoginErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email hoặc mật khẩu không đúng.";
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "Tài khoản không tồn tại.";
        }

        if (exception != null && exception.getMessage() != null) {
            return exception.getMessage();
        }

        return "Không đăng nhập được. Hãy kiểm tra mạng và thử lại.";
    }
}

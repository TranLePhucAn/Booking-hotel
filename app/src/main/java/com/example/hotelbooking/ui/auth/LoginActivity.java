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

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.utils.LoadingDialog;
import com.example.hotelbooking.utils.RoleRouter;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView txtRegister;
    private TextView txtPartnerRegister;

    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        auth = FirebaseClient.getAuth();
        loadingDialog = new LoadingDialog(this);

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

    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui long nhap day du thong tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email khong dung dinh dang", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Dang nhap thanh cong", Toast.LENGTH_SHORT).show();
                        RoleRouter.routeCurrentUser(this, loadingDialog);
                    } else {
                        loadingDialog.dismiss();
                        String message = task.getException() != null ? task.getException().getMessage() : "Khong dang nhap duoc";
                        Toast.makeText(this, "Loi: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }
}

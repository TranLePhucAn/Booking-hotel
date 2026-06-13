package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hotelbooking.R;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText edtProfileName = findViewById(R.id.edtProfileName);
        EditText edtProfileEmail = findViewById(R.id.edtProfileEmail);
        EditText edtProfilePhone = findViewById(R.id.edtProfilePhone);
        Button btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        if (btnUpdateProfile != null) {
            btnUpdateProfile.setOnClickListener(v -> {
                // Xử lý cập nhật thông tin
                finish();
            });
        }
    }
}
package com.example.hotelbooking.ui.home;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.utils.LoadingDialog;
import com.example.hotelbooking.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.*;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;

    private ImageView imgAvatar, btnEditAvatar;
    private EditText edtName, edtPhone, edtDob;
    private TextView tvEmail;
    private Spinner spinnerGender;
    private AutoCompleteTextView autoCompleteCountry;

    private String uid;
    private Uri imageUri;
    private String avatarUrl = "";

    private UserViewModel userViewModel;
    private LoadingDialog loadingDialog;

    private final String[] genderList = {"Nam", "Nữ", "Khác"};
    private final String[] countryList = {"Việt Nam", "USA", "Japan", "Korea", "Thailand", "Singapore", "Malaysia"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadingDialog = new LoadingDialog(this);

        initViews();
        setupAdapters();

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        observeViewModel();

        userViewModel.fetchUser(uid);
        setupEvents();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);

        edtName = findViewById(R.id.edtName);
        tvEmail = findViewById(R.id.tvEmail);
        edtPhone = findViewById(R.id.edtPhone);
        spinnerGender = findViewById(R.id.spinnerGender);
        edtDob = findViewById(R.id.edtDob);
        autoCompleteCountry = findViewById(R.id.autoCompleteCountry);
    }

    private void setupAdapters() {
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genderList);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, countryList);
        autoCompleteCountry.setAdapter(countryAdapter);
    }

    private void observeViewModel() {
        userViewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) {
                if (isLoading) loadingDialog.show();
                else loadingDialog.dismiss();
            }
        });

        userViewModel.userData.observe(this, data -> {
            if (data != null) {
                edtName.setText((String) data.get("fullName"));
                tvEmail.setText((String) data.get("email"));
                edtPhone.setText((String) data.get("phone"));
                edtDob.setText((String) data.get("date_of_birth"));
                autoCompleteCountry.setText((String) data.get("country"), false);

                String gender = (String) data.get("gender");
                if (gender != null) {
                    int position = Arrays.asList(genderList).indexOf(gender);
                    if (position >= 0) spinnerGender.setSelection(position);
                }

                avatarUrl = (String) data.get("avatarUrl");
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(imgAvatar);
                }
            }
        });

        userViewModel.updateSuccess.observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                userViewModel.clearState();
                finish();
            }
        });

        userViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                userViewModel.clearState();
            }
        });
    }

    private void setupEvents() {
        btnEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        View.OnClickListener dateClick = v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        String date = y + "-" + String.format(Locale.getDefault(), "%02d", (m + 1)) + "-" + String.format(Locale.getDefault(), "%02d", d);
                        edtDob.setText(date);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        };

        edtDob.setOnClickListener(dateClick);
        if (findViewById(R.id.btnSelectDob) != null) {
            findViewById(R.id.btnSelectDob).setOnClickListener(dateClick);
        }

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            saveProfile();
        });
    }

    private void saveProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("fullName", edtName.getText().toString().trim());
        data.put("phone", edtPhone.getText().toString().trim());
        data.put("gender", spinnerGender.getSelectedItem().toString());
        data.put("date_of_birth", edtDob.getText().toString().trim());
        data.put("country", autoCompleteCountry.getText().toString().trim());

        if (imageUri != null) {
            userViewModel.uploadAvatar(uid, imageUri, data);
        } else {
            userViewModel.updateUserProfile(uid, data);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgAvatar.setImageURI(imageUri);
        }
    }
}
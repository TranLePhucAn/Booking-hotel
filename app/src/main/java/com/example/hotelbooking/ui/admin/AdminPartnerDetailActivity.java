package com.example.hotelbooking.ui.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.PartnerApplication;
import com.example.hotelbooking.viewmodels.PartnerViewModel;

public class AdminPartnerDetailActivity extends AppCompatActivity {
    private PartnerViewModel viewModel;
    private PartnerApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_partner_detail);

        application = (PartnerApplication) getIntent().getSerializableExtra("application");
        if (application == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(PartnerViewModel.class);

        TextView tvName = findViewById(R.id.tvDetailBusinessName);
        TextView tvRep = findViewById(R.id.tvDetailRepName);
        TextView tvPhone = findViewById(R.id.tvDetailPhone);
        TextView tvEmail = findViewById(R.id.tvDetailEmail);
        TextView tvAddress = findViewById(R.id.tvDetailAddress);
        TextView tvTax = findViewById(R.id.tvDetailTaxCode);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);
        EditText etNote = findViewById(R.id.etAdminNote);
        Button btnViewFile = findViewById(R.id.btnViewVerificationFile);
        Button btnApprove = findViewById(R.id.btnApproveApp);
        Button btnReject = findViewById(R.id.btnRejectApp);

        tvName.setText(application.getBusinessName());
        tvRep.setText(application.getRepresentativeName());
        tvPhone.setText(application.getPhone());
        tvEmail.setText(application.getEmail());
        tvAddress.setText(application.getAddress());
        tvTax.setText(application.getTaxCode());
        tvDesc.setText(application.getDescription());

        btnViewFile.setOnClickListener(v -> {
            if (application.getVerificationFileUrl() != null && !application.getVerificationFileUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(application.getVerificationFileUrl()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có file xác minh", Toast.LENGTH_SHORT).show();
            }
        });

        btnApprove.setOnClickListener(v -> {
            viewModel.approveApplication(application, etNote.getText().toString());
            Toast.makeText(this, "Đã duyệt hồ sơ", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnReject.setOnClickListener(v -> {
            String note = etNote.getText().toString();
            if (note.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập lý do từ chối", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.rejectApplication(application.getId(), application.getUserId(), note);
            Toast.makeText(this, "Đã từ chối hồ sơ", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

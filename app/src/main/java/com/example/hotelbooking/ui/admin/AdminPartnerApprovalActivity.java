package com.example.hotelbooking.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotelbooking.R;
import com.example.hotelbooking.viewmodels.PartnerViewModel;

public class AdminPartnerApprovalActivity extends AppCompatActivity {
    private PartnerViewModel viewModel;
    private PartnerApplicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_partner_approval);

        RecyclerView rv = findViewById(R.id.rvPartnerApplications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PartnerApplicationAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PartnerViewModel.class);
        
        viewModel.applications.observe(this, apps -> {
            adapter.setList(apps);
        });

        viewModel.error.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        adapter.setOnItemClickListener(app -> {
            Intent intent = new Intent(this, AdminPartnerDetailActivity.class);
            intent.putExtra("application", app);
            startActivity(intent);
        });

        viewModel.fetchPendingApplications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.fetchPendingApplications();
    }
}

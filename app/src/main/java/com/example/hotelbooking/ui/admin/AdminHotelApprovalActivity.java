package com.example.hotelbooking.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.adapter.PendingHotelAdapter;
import com.example.hotelbooking.viewmodels.HotelViewModel;

public class AdminHotelApprovalActivity extends AppCompatActivity {
    private HotelViewModel viewModel;
    private PendingHotelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_hotel_approval);

        RecyclerView rv = findViewById(R.id.rvPendingHotels);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingHotelAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HotelViewModel.class);

        viewModel.pendingHotels.observe(this, hotels -> {
            adapter.setList(hotels);
        });

        viewModel.error.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        adapter.setOnItemClickListener(hotel -> {
            Intent intent = new Intent(this, AdminHotelDetailActivity.class);
            intent.putExtra("hotel", hotel);
            startActivity(intent);
        });

        viewModel.fetchPendingHotels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.fetchPendingHotels();
    }
}

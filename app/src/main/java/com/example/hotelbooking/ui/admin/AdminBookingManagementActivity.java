package com.example.hotelbooking.ui.admin;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.adapter.BookingHistoryAdapter;
import com.example.hotelbooking.viewmodels.BookingViewModel;

public class AdminBookingManagementActivity extends AppCompatActivity {
    private BookingViewModel viewModel;
    private BookingHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_booking_management);

        RecyclerView rv = findViewById(R.id.rvAllReservations);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingHistoryAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        viewModel.reservations.observe(this, reservations -> {
            adapter.updateData(reservations);
        });

        viewModel.error.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        viewModel.fetchAllReservations();
    }
}

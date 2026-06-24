package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Reservation;
import com.example.hotelbooking.utils.AppConstants;
import com.example.hotelbooking.viewmodels.BookingViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private BookingViewModel viewModel;
    private BookingHistoryAdapter adapter;
    private List<Reservation> allReservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        Button btnFilterUpcoming = findViewById(R.id.btnFilterUpcoming);
        Button btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        RecyclerView rvBookingHistory = findViewById(R.id.rvBookingHistory);

        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingHistoryAdapter();
        rvBookingHistory.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        viewModel.reservations.observe(this, reservations -> {
            allReservations = reservations;
            showFilteredReservations(false);
        });

        viewModel.error.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        btnFilterUpcoming.setOnClickListener(v -> showFilteredReservations(false));
        btnFilterCompleted.setOnClickListener(v -> showFilteredReservations(true));

        loadData();
    }

    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.fetchUserBookings(user.getUid());
    }

    private void showFilteredReservations(boolean completedOnly) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation res : allReservations) {
            String status = res.getStatus();
            boolean isCompleted = AppConstants.BOOKING_COMPLETED.equalsIgnoreCase(status) 
                    || AppConstants.BOOKING_CANCELLED.equalsIgnoreCase(status);
            
            if (completedOnly == isCompleted) {
                result.add(res);
            }
        }
        adapter.updateData(result);
    }
}

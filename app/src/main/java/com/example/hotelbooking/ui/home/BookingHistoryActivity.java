package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Reservation;
import com.example.hotelbooking.ui.adapter.BookingHistoryAdapter;
import com.example.hotelbooking.viewmodels.BookingViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private final List<Reservation> allBookings = new ArrayList<>();
    private final BookingHistoryAdapter adapter = new BookingHistoryAdapter();
    private BookingViewModel bookingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        Button btnFilterUpcoming = findViewById(R.id.btnFilterUpcoming);
        Button btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        RecyclerView rvBookingHistory = findViewById(R.id.rvBookingHistory);

        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBookingHistory.setAdapter(adapter);

        btnFilterUpcoming.setOnClickListener(v -> {
            btnFilterUpcoming.setBackgroundTintList(getColorStateList(R.color.ocean_blue));
            btnFilterCompleted.setBackgroundTintList(getColorStateList(R.color.separate_color));
            showBookings(false);
        });

        btnFilterCompleted.setOnClickListener(v -> {
            btnFilterCompleted.setBackgroundTintList(getColorStateList(R.color.ocean_blue));
            btnFilterUpcoming.setBackgroundTintList(getColorStateList(R.color.separate_color));
            showBookings(true);
        });

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        observeViewModel();

        loadBookings();
    }

    private void observeViewModel() {
        bookingViewModel.reservations.observe(this, reservations -> {
            allBookings.clear();
            if (reservations != null) {
                allBookings.addAll(reservations);
            }
            if (allBookings.isEmpty()) {
                Toast.makeText(this, "Bạn chưa có đơn đặt phòng nào", Toast.LENGTH_SHORT).show();
            }
            showBookings(false);
        });

        bookingViewModel.error.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Không tải được lịch sử: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadBookings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            return;
        }

        bookingViewModel.fetchUserBookings(user.getUid());
    }

    private void showBookings(boolean completedOnly) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation res : allBookings) {
            String status = res.getStatus();
            if (status == null) status = "";

            boolean isCompletedStatus = "COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status);

            if (completedOnly == isCompletedStatus) {
                result.add(res);
            }
        }
        adapter.updateData(result);
    }
}
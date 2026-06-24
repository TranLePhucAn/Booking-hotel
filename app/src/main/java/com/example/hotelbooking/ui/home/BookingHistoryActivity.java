package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private final List<DocumentSnapshot> allBookings = new ArrayList<>();
    private final BookingHistoryAdapter adapter = new BookingHistoryAdapter();

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

        loadBookings();
    }

    private void loadBookings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentEmail = user.getEmail();

        FirebaseFirestore.getInstance()
                .collection("reservations")
                .whereEqualTo("guest_email", currentEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    allBookings.clear();
                    allBookings.addAll(querySnapshot.getDocuments());

                    if (allBookings.isEmpty()) {
                        Toast.makeText(this, "Bạn chưa có đơn đặt phòng nào", Toast.LENGTH_SHORT).show();
                    }

                    showBookings(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không tải được lịch sử: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showBookings(boolean completedOnly) {
        List<DocumentSnapshot> result = new ArrayList<>();
        for (DocumentSnapshot booking : allBookings) {
            String status = booking.getString("status");

            if (status == null) status = "";

            boolean isCompleted = "COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status);

            if (completedOnly == isCompleted) {
                result.add(booking);
            }
        }
        adapter.updateData(result);
    }
}
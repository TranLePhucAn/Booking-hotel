package com.example.hotelbooking.ui.home;

import android.os.Bundle;
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

        btnFilterUpcoming.setOnClickListener(v -> showBookings(false));
        btnFilterCompleted.setOnClickListener(v -> showBookings(true));

        loadBookings();
    }

    private void loadBookings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui long dang nhap de xem lich su", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("user_id", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allBookings.clear();
                    allBookings.addAll(querySnapshot.getDocuments());
                    showBookings(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong tai duoc lich su: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showBookings(boolean completedOnly) {
        List<DocumentSnapshot> result = new ArrayList<>();
        for (DocumentSnapshot booking : allBookings) {
            String status = booking.getString("status");
            boolean completed = "completed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status);
            if (completedOnly == completed) {
                result.add(booking);
            }
        }
        adapter.updateData(result);
    }
}

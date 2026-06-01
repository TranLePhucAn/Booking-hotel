package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotelbooking.R;

public class BookingHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        Button btnFilterUpcoming = findViewById(R.id.btnFilterUpcoming);
        Button btnFilterCompleted = findViewById(R.id.btnFilterCompleted);
        RecyclerView rvBookingHistory = findViewById(R.id.rvBookingHistory);

        if (rvBookingHistory != null) {
            rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        }
    }
}
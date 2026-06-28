package com.example.hotelbooking.ui.partner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.adapter.PartnerBookingAdapter;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.viewmodels.BookingViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class PartnerDashboardActivity extends AppCompatActivity {

    private BookingViewModel bookingViewModel;
    private PartnerBookingAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_dashboard);

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ScrollView dashboardScroll = findViewById(R.id.partnerDashboardScroll);
        TextView bookingSectionTitle = findViewById(R.id.textBookingSectionTitle);
        CardView cardMyHotels = findViewById(R.id.cardMyHotels);
        CardView cardManageRooms = findViewById(R.id.cardManageRooms);
        CardView cardBookings = findViewById(R.id.cardBookings);
        CardView cardAddHotel = findViewById(R.id.cardAddHotel);
        Button btnLogout = findViewById(R.id.btnLogoutPartner);
        RecyclerView rvReservations = findViewById(R.id.rvPartnerReservations);

        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PartnerBookingAdapter();
        rvReservations.setAdapter(adapter);

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        bookingViewModel.reservations.observe(this, reservations -> adapter.setList(reservations));

        bookingViewModel.error.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        cardAddHotel.setOnClickListener(v -> startActivity(new Intent(this, AddHotelActivity.class)));
        cardMyHotels.setOnClickListener(v -> startActivity(new Intent(this, PartnerHotelManagementActivity.class)));
        cardManageRooms.setOnClickListener(v -> {
            Toast.makeText(this, "Chọn khách sạn rồi bấm Thêm phòng hoặc quản lý phòng.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PartnerHotelManagementActivity.class));
        });
        cardBookings.setOnClickListener(v -> dashboardScroll.post(() ->
                dashboardScroll.smoothScrollTo(0, bookingSectionTitle.getTop())));

        btnLogout.setOnClickListener(v -> {
            btnLogout.setEnabled(false);
            btnLogout.setText("Đang đăng xuất...");
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, com.example.hotelbooking.ui.home.HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        bookingViewModel.fetchPartnerBookings(currentUserId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookingViewModel != null && currentUserId != null) {
            bookingViewModel.fetchPartnerBookings(currentUserId);
        }
    }
}

package com.example.hotelbooking.ui.payment;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfirmActivity extends AppCompatActivity {

    private String hotelId;
    private String roomId;
    private String hotelName;
    private String hotelImage;
    private String ownerId;
    private String roomName;
    private double roomPrice;
    private String checkInDate;
    private String checkOutDate;
    private int nights = 1;
    private double totalPrice;

    private EditText etGuestName;
    private EditText etGuestPhone;
    private EditText etGuestEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        readIntentData();
        bindViews();
        fillBookingSummary();
    }

    private void readIntentData() {
        hotelId = getIntent().getStringExtra("hotel_id");
        roomId = getIntent().getStringExtra("room_id");
        hotelName = valueOrDefault(getIntent().getStringExtra("hotel_name"), "Khach san");
        hotelImage = valueOrDefault(getIntent().getStringExtra("hotel_image"), "");
        ownerId = valueOrDefault(getIntent().getStringExtra("owner_id"), "");
        roomName = valueOrDefault(getIntent().getStringExtra("room_name"), "Phong tieu chuan");
        roomPrice = getIntent().getDoubleExtra("room_price", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        checkInDate = formatDate(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        checkOutDate = formatDate(calendar);
        totalPrice = roomPrice * nights;
    }

    private void bindViews() {
        etGuestName = findViewById(R.id.et_guest_name);
        etGuestPhone = findViewById(R.id.et_guest_phone);
        etGuestEmail = findViewById(R.id.et_guest_email);

        Button btnConfirmBooking = findViewById(R.id.btn_confirm_booking);
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null) {
                etGuestName.setText(user.getDisplayName());
            }
            if (user.getEmail() != null) {
                etGuestEmail.setText(user.getEmail());
            }
        }
    }

    private void fillBookingSummary() {
        TextView tvHotelName = findViewById(R.id.textView);
        TextView tvReviewScore = findViewById(R.id.textView2);
        TextView tvReviewCount = findViewById(R.id.textView3);
        TextView tvRoomName = findViewById(R.id.textView5);
        TextView tvAvailable = findViewById(R.id.textView6);
        TextView tvCheckIn = findViewById(R.id.textView8);
        TextView tvNightCount = findViewById(R.id.textView10);
        TextView tvCheckOut = findViewById(R.id.textView12);
        TextView tvBasePrice = findViewById(R.id.tv_base_price);
        TextView tvTaxPrice = findViewById(R.id.tv_tax_price);
        TextView tvTotalPrice = findViewById(R.id.tv_total_price);
        RatingBar ratingBar = findViewById(R.id.ratingBar);

        double reviewScore = getIntent().getDoubleExtra("review_score", 0);
        int reviewCount = getIntent().getIntExtra("review_count", 0);
        int ratingStar = getIntent().getIntExtra("rating_star", 0);
        double tax = roomPrice * 0.1;
        totalPrice = roomPrice + tax;

        tvHotelName.setText(hotelName);
        tvReviewScore.setText(String.format(Locale.getDefault(), "%.1f/10", reviewScore));
        tvReviewCount.setText("(" + reviewCount + " danh gia)");
        tvRoomName.setText(roomName);
        tvAvailable.setText("Trang thai: con phong");
        tvCheckIn.setText(checkInDate);
        tvNightCount.setText(nights + " dem");
        tvCheckOut.setText(checkOutDate);
        tvBasePrice.setText(formatMoney(roomPrice));
        tvTaxPrice.setText(formatMoney(tax));
        tvTotalPrice.setText(formatMoney(totalPrice));
        ratingBar.setRating(ratingStar);
    }

    private void confirmBooking() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui long dang nhap de dat phong", Toast.LENGTH_SHORT).show();
            return;
        }

        String guestName = etGuestName.getText().toString().trim();
        String guestPhone = etGuestPhone.getText().toString().trim();
        String guestEmail = etGuestEmail.getText().toString().trim();

        if (TextUtils.isEmpty(guestName) || TextUtils.isEmpty(guestPhone) || TextUtils.isEmpty(guestEmail)) {
            Toast.makeText(this, "Vui long nhap day du thong tin lien he", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("user_id", user.getUid());
        booking.put("owner_id", ownerId);
        booking.put("hotel_id", hotelId);
        booking.put("room_id", roomId);
        booking.put("hotel_name", hotelName);
        booking.put("hotel_image", hotelImage);
        booking.put("room_name", roomName);
        booking.put("check_in", checkInDate);
        booking.put("check_out", checkOutDate);
        booking.put("nights", nights);
        booking.put("guest_name", guestName);
        booking.put("guest_phone", guestPhone);
        booking.put("guest_email", guestEmail);
        booking.put("price_per_night", roomPrice);
        booking.put("total_price", totalPrice);
        booking.put("status", "pending_confirmation");
        booking.put("created_at", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Dat phong thanh cong", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong luu duoc dat phong: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String formatDate(Calendar calendar) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
    }

    private String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0f VND", value);
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}

package com.example.hotelbooking.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.viewmodels.HotelViewModel;

public class AdminHotelDetailActivity extends AppCompatActivity {
    private HotelViewModel viewModel;
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_hotel_detail);

        hotel = (Hotel) getIntent().getSerializableExtra("hotel");
        if (hotel == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(HotelViewModel.class);

        ImageView ivHotel = findViewById(R.id.ivHotelDetail);
        TextView tvName = findViewById(R.id.tvHotelDetailName);
        TextView tvAddress = findViewById(R.id.tvHotelDetailAddress);
        TextView tvPrice = findViewById(R.id.tvHotelDetailPrice);
        TextView tvDesc = findViewById(R.id.tvHotelDetailDesc);
        TextView tvAmenities = findViewById(R.id.tvHotelDetailAmenities);
        EditText etNote = findViewById(R.id.etHotelAdminNote);
        Button btnApprove = findViewById(R.id.btnApproveHotel);
        Button btnReject = findViewById(R.id.btnRejectHotel);

        tvName.setText(hotel.getName());
        tvAddress.setText(hotel.getAddress());
        tvPrice.setText(String.format("%,.0f VNĐ", hotel.getPrice()));
        tvDesc.setText(hotel.getDescription());
        
        if (hotel.getAmenities() != null) {
            tvAmenities.setText(String.join(", ", hotel.getAmenities()));
        }

        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            Glide.with(this).load(hotel.getImageUrl()).into(ivHotel);
        }

        btnApprove.setOnClickListener(v -> {
            viewModel.approveHotel(hotel.getId(), etNote.getText().toString());
            Toast.makeText(this, "Đã duyệt khách sạn", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnReject.setOnClickListener(v -> {
            String note = etNote.getText().toString();
            if (note.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập lý do từ chối", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.rejectHotel(hotel.getId(), note);
            Toast.makeText(this, "Đã từ chối khách sạn", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

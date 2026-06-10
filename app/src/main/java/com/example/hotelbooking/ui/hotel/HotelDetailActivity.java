package com.example.hotelbooking.ui.hotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.hotelbooking.databinding.ActivityHotelDetailBinding;
import com.example.hotelbooking.data.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class HotelDetailActivity extends AppCompatActivity {

    private ActivityHotelDetailBinding binding;
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHotelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        hotel = (Hotel) getIntent().getSerializableExtra("hotel");

        if (hotel != null) {
            displayHotelDetails();
        }

        binding.btnShowMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("hotel", hotel);
            startActivity(intent);
        });

        binding.btnBookNow.setOnClickListener(v -> {
            // Logic đặt phòng
        });
    }

    private void displayHotelDetails() {
        binding.tvHotelName.setText(hotel.getName());
        binding.tvHotelAddress.setText(hotel.getAddress());
        binding.tvDescription.setText(hotel.getDescription());
        binding.tvPrice.setText(String.format("$%.2f / đêm", hotel.getPrice()));
        binding.ratingBar.setRating(hotel.getRating());

        if (hotel.getAmenities() != null) {
            binding.tvAmenities.setText(String.join(", ", hotel.getAmenities()));
        }

        // Setup Image Slider
        List<SlideModel> imageList = new ArrayList<>();
        if (hotel.getMainImage() != null) {
            imageList.add(new SlideModel(hotel.getMainImage(), ScaleTypes.CENTER_CROP));
        }
        if (hotel.getSecondaryImages() != null) {
            for (String imgUrl : hotel.getSecondaryImages()) {
                imageList.add(new SlideModel(imgUrl, ScaleTypes.CENTER_CROP));
            }
        }
        binding.imageSlider.setImageList(imageList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotelbooking.data.model.DemoHotelData;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.databinding.ActivityHomeBinding;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private CategoryAdapter categoryAdapter;
    private HotelAdapter hotelAdapter;
    private final List<Hotel> featuredHotels = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        initViews();
        setupCategories();
        setupFeaturedHotels();
        loadActiveHotels();
    }

    private void initViews() {
        binding.searchBar.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        if (FirebaseClient.getAuth().getCurrentUser() != null) {
            String name = FirebaseClient.getAuth().getCurrentUser().getDisplayName();
            if (name != null && !name.isEmpty()) {
                binding.btnProfile.setText(name);
            }
        }

        binding.btnProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseClient.getAuth().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnFilter.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
    }

    private void setupCategories() {
        List<String> categories = Arrays.asList("Tat ca", "Resort", "Khach san", "Villa", "Homestay", "Can ho");
        categoryAdapter = new CategoryAdapter(categories, category ->
                Toast.makeText(this, "Danh muc: " + category, Toast.LENGTH_SHORT).show());

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupFeaturedHotels() {
        hotelAdapter = new HotelAdapter(featuredHotels, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                openHotelDetail(hotel);
            }

            @Override
            public void onBookClick(Hotel hotel) {
                openHotelDetail(hotel);
            }
        });

        binding.rvFeaturedHotels.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFeaturedHotels.setAdapter(hotelAdapter);
    }

    private void loadActiveHotels() {
        db.collection("hotels")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    featuredHotels.clear();
                    querySnapshot.getDocuments().forEach(document ->
                            featuredHotels.add(Hotel.fromDocument(document)));
                    addDemoHotelsIfNeeded();
                    hotelAdapter.updateData(featuredHotels);
                })
                .addOnFailureListener(e -> {
                    featuredHotels.clear();
                    featuredHotels.addAll(DemoHotelData.hotels());
                    hotelAdapter.updateData(featuredHotels);
                    Toast.makeText(this, "Dang hien thi du lieu mau", Toast.LENGTH_SHORT).show();
                });
    }

    private void addDemoHotelsIfNeeded() {
        for (Hotel demoHotel : DemoHotelData.hotels()) {
            if (!containsHotel(demoHotel.getId())) {
                featuredHotels.add(demoHotel);
            }
        }
    }

    private boolean containsHotel(String hotelId) {
        for (Hotel hotel : featuredHotels) {
            if (hotelId != null && hotelId.equals(hotel.getId())) {
                return true;
            }
        }
        return false;
    }

    private void openHotelDetail(Hotel hotel) {
        Intent intent = new Intent(HomeActivity.this, HotelDetailActivity.class);
        intent.putExtra("hotel_id", hotel.getId());
        intent.putExtra("hotel", hotel);
        startActivity(intent);
    }
}

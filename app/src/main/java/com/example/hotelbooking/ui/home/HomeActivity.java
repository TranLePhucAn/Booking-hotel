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
    private final List<Hotel> allHotels = new ArrayList<>(); // Master list containing all loaded hotels
    private final List<Hotel> displayedHotels = new ArrayList<>(); // List used by the adapter
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        initViews();
        setupCategories();
        setupHotelRecyclerView();
        loadHotels();
    }

    private void initViews() {
        // Search bar navigation
        binding.searchBar.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        // Profile display name
        if (FirebaseClient.getAuth().getCurrentUser() != null) {
            String name = FirebaseClient.getAuth().getCurrentUser().getDisplayName();
            if (name != null && !name.isEmpty()) {
                binding.btnProfile.setText(name);
            }
        }

        // Profile navigation
        binding.btnProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        // Logout
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseClient.getAuth().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Filter button navigation
        binding.btnFilter.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
    }

    private void setupCategories() {
        // Defined categories matching DemoHotelData
        List<String> categories = Arrays.asList("Tat ca", "Resort", "Khach san", "Villa", "Homestay", "Can ho");
        
        // Initialize adapter with actual filtering logic
        categoryAdapter = new CategoryAdapter(categories, this::filterByCategory);

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupHotelRecyclerView() {
        hotelAdapter = new HotelAdapter(displayedHotels, new HotelAdapter.OnHotelClickListener() {
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

    private void loadHotels() {
        // 1. Load Demo data immediately for "chạy thử"
        allHotels.clear();
        allHotels.addAll(DemoHotelData.hotels());
        
        // Initial display showing everything
        updateDisplayedHotels("Tat ca");

        // 2. Fetch from Firestore to sync real data if available
        db.collection("hotels")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        querySnapshot.getDocuments().forEach(document -> {
                            Hotel h = Hotel.fromDocument(document);
                            if (!containsHotel(h.getId())) {
                                allHotels.add(h);
                            }
                        });
                        // Refresh with merged data
                        updateDisplayedHotels("Tat ca");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hien thi du lieu mau tu DemoHotelData", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterByCategory(String category) {
        // Perform real filtering
        updateDisplayedHotels(category);
        
        if (displayedHotels.isEmpty() && !category.equalsIgnoreCase("Tat ca")) {
            Toast.makeText(this, "Khong tim thay ket qua cho: " + category, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDisplayedHotels(String category) {
        displayedHotels.clear();
        if (category.equalsIgnoreCase("Tat ca")) {
            displayedHotels.addAll(allHotels);
        } else {
            for (Hotel hotel : allHotels) {
                // Case-insensitive check to match DemoHotelData categories
                if (hotel.getCategory() != null && hotel.getCategory().equalsIgnoreCase(category)) {
                    displayedHotels.add(hotel);
                }
            }
        }
        
        // Update adapter data
        if (hotelAdapter != null) {
            hotelAdapter.updateData(displayedHotels);
        }
    }

    private boolean containsHotel(String hotelId) {
        for (Hotel hotel : allHotels) {
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

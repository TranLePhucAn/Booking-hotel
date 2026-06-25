package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private String currentCategory = "Tat ca";

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
        
        binding.btnRetry.setOnClickListener(v -> loadHotels());
    }

    private void setupCategories() {
        List<String> categories = Arrays.asList("Tat ca", "Resort", "Khach san", "Villa", "Homestay", "Can ho");
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
        showLoading();
        
        // Nạp dữ liệu mẫu trước
        allHotels.clear();
        allHotels.addAll(DemoHotelData.hotels());

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
                    }
                    updateDisplay(currentCategory);
                })
                .addOnFailureListener(e -> {
                    if (allHotels.isEmpty()) {
                        showError();
                    } else {
                        // Vẫn còn dữ liệu mẫu thì hiển thị content kèm thông báo
                        updateDisplay(currentCategory);
                        Toast.makeText(this, "Loi ket noi, dang dung du lieu tam thoi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterByCategory(String category) {
        this.currentCategory = category;
        updateDisplay(category);
    }

    private void updateDisplay(String category) {
        displayedHotels.clear();
        if (category.equalsIgnoreCase("Tat ca")) {
            displayedHotels.addAll(allHotels);
        } else {
            for (Hotel hotel : allHotels) {
                if (hotel.getCategory() != null && hotel.getCategory().equalsIgnoreCase(category)) {
                    displayedHotels.add(hotel);
                }
            }
        }
        
        if (displayedHotels.isEmpty()) {
            showEmpty();
        } else {
            showContent();
            hotelAdapter.updateData(displayedHotels);
        }
    }

    private void showLoading() {
        binding.stateLayout.setVisibility(View.VISIBLE);
        binding.loadingView.setVisibility(View.VISIBLE);
        binding.emptyView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.GONE);
        binding.mainContent.setVisibility(View.GONE);
    }

    private void showEmpty() {
        binding.stateLayout.setVisibility(View.VISIBLE);
        binding.loadingView.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.VISIBLE);
        binding.errorView.setVisibility(View.GONE);
        binding.mainContent.setVisibility(View.GONE);
    }

    private void showError() {
        binding.stateLayout.setVisibility(View.VISIBLE);
        binding.loadingView.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.VISIBLE);
        binding.mainContent.setVisibility(View.GONE);
    }

    private void showContent() {
        binding.stateLayout.setVisibility(View.GONE);
        binding.mainContent.setVisibility(View.VISIBLE);
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

package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
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
    private HotelAdapter featuredAdapter;
    private HotelAdapter suggestionsAdapter;
    
    private final List<Hotel> allHotels = new ArrayList<>();
    private final List<Hotel> featuredHotels = new ArrayList<>();
    private final List<Hotel> suggestionHotels = new ArrayList<>();
    
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        initViews();
        setupBanner();
        setupCategories();
        setupHotelRecyclerViews();
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
        
        binding.tvSeeAllFeatured.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private void setupBanner() {
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=1200", "Khám phá StayHub", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200", "Ưu đãi hè rực rỡ", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel("https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=1200", "Đặt phòng ngay hôm nay", ScaleTypes.CENTER_CROP));
        
        binding.bannerSlider.setImageList(slideModels);
    }

    private void setupCategories() {
        List<String> categories = Arrays.asList("Tat ca", "Resort", "Khach san", "Villa", "Homestay", "Can ho");
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, this::filterByCategory);

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupHotelRecyclerViews() {
        // Featured Hotels - Horizontal
        featuredAdapter = new HotelAdapter(featuredHotels, true, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) { openHotelDetail(hotel); }
            @Override
            public void onBookClick(Hotel hotel) { openHotelDetail(hotel); }
        });
        binding.rvFeaturedHotels.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedHotels.setAdapter(featuredAdapter);

        // Suggestions - Vertical
        suggestionsAdapter = new HotelAdapter(suggestionHotels, false, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) { openHotelDetail(hotel); }
            @Override
            public void onBookClick(Hotel hotel) { openHotelDetail(hotel); }
        });
        binding.rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSuggestions.setAdapter(suggestionsAdapter);
    }

    private void loadHotels() {
        showLoading();
        
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
                    processAndDisplayData();
                })
                .addOnFailureListener(e -> {
                    if (allHotels.isEmpty()) {
                        showError();
                    } else {
                        processAndDisplayData();
                        Toast.makeText(this, "Lỗi kết nối, đang dùng dữ liệu tạm thời", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processAndDisplayData() {
        showContent();
        
        featuredHotels.clear();
        suggestionHotels.clear();
        
        for (Hotel hotel : allHotels) {
            if (hotel.isFeatured()) {
                featuredHotels.add(hotel);
            } else {
                suggestionHotels.add(hotel);
            }
        }
        
        // If not enough featured, take some from suggestions
        if (featuredHotels.size() < 3 && !suggestionHotels.isEmpty()) {
            for (int i = 0; i < Math.min(3, suggestionHotels.size()); i++) {
                featuredHotels.add(suggestionHotels.get(i));
            }
        }

        featuredAdapter.updateData(featuredHotels);
        suggestionsAdapter.updateData(suggestionHotels);
    }

    private void filterByCategory(String category) {
        suggestionHotels.clear();
        if (category.equalsIgnoreCase("Tat ca")) {
            for (Hotel h : allHotels) {
                if (!h.isFeatured()) suggestionHotels.add(h);
            }
        } else {
            for (Hotel hotel : allHotels) {
                if (hotel.getCategory() != null && hotel.getCategory().equalsIgnoreCase(category)) {
                    suggestionHotels.add(hotel);
                }
            }
        }
        
        suggestionsAdapter.updateData(suggestionHotels);
        
        if (suggestionHotels.isEmpty() && !category.equalsIgnoreCase("Tat ca")) {
            Toast.makeText(this, "Không có kết quả cho: " + category, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading() {
        binding.stateLayout.setVisibility(View.VISIBLE);
        binding.loadingView.setVisibility(View.VISIBLE);
        binding.emptyView.setVisibility(View.GONE);
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

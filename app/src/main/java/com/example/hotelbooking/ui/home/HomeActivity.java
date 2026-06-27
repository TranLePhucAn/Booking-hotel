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
import com.example.hotelbooking.data.repository.WishlistRepository;
import com.example.hotelbooking.databinding.ActivityHomeBinding;
import com.example.hotelbooking.ui.adapter.CategoryAdapter;
import com.example.hotelbooking.ui.adapter.HotelAdapter;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private CategoryAdapter categoryAdapter;
    private HotelAdapter featuredAdapter;
    private HotelAdapter suggestionsAdapter;
    
    private final List<Hotel> allHotels = new ArrayList<>();
    private final List<Hotel> featuredHotels = new ArrayList<>();
    private final List<Hotel> suggestionHotels = new ArrayList<>();
    
    private FirebaseFirestore db;
    private WishlistRepository wishlistRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        wishlistRepository = new WishlistRepository();

        initViews();
        setupBanner();
        setupCategories();
        setupHotelRecyclerViews();
        loadHotels();
    }

    private void updateUserUI() {
        if (FirebaseClient.getAuth().getCurrentUser() != null) {
            String name = FirebaseClient.getAuth().getCurrentUser().getDisplayName();
            binding.btnProfile.setText(name != null && !name.isEmpty() ? name : "Tài khoản");
            binding.btnLogout.setVisibility(View.VISIBLE);
            binding.btnProfile.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        } else {
            binding.btnProfile.setText("Đăng nhập");
            binding.btnLogout.setVisibility(View.GONE);
            binding.btnProfile.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserUI();
        if (featuredAdapter != null) featuredAdapter.updateData(featuredHotels);
        if (suggestionsAdapter != null) suggestionsAdapter.updateData(suggestionHotels);
    }

    private void initViews() {
        binding.searchBar.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)));

        updateUserUI();

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseClient.getAuth().signOut();
            updateUserUI();
            Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
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
        List<String> categories = Arrays.asList("Tất cả", "Resort", "Khách sạn", "Villa", "Homestay", "Căn hộ");
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, this::filterByCategory);

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupHotelRecyclerViews() {
        HotelAdapter.OnHotelClickListener hotelClickListener = new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                openHotelDetail(hotel);
            }

            @Override
            public void onBookClick(Hotel hotel) {
                openHotelDetail(hotel);
            }

            @Override
            public void onFavoriteClick(Hotel hotel) {
                handleFavoriteToggle(hotel);
            }
        };

        // Featured Hotels - Horizontal
        featuredAdapter = new HotelAdapter(featuredHotels, true, hotelClickListener);
        binding.rvFeaturedHotels.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedHotels.setAdapter(featuredAdapter);

        // Suggestions - Vertical
        suggestionsAdapter = new HotelAdapter(suggestionHotels, false, hotelClickListener);
        binding.rvSuggestions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSuggestions.setAdapter(suggestionsAdapter);
    }
    private void handleFavoriteToggle(Hotel hotel) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thực hiện", Toast.LENGTH_SHORT).show();
            return;
        }

        wishlistRepository.isFavorite(userId, hotel.getId()).addOnSuccessListener(isFav -> {
            if (isFav) {
                // Đang thích -> Bỏ thích
                wishlistRepository.removeFromWishlist(userId, hotel.getId()).addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeActivity.this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                    refreshBothAdapters();
                });
            } else {
                // Chưa thích -> Thêm vào yêu thích
                Map<String, Object> hotelInfo = new HashMap<>();
                hotelInfo.put("hotelName", hotel.getHotelName());
                hotelInfo.put("address", hotel.getAddress());
                hotelInfo.put("basePrice", hotel.getPrice());
                hotelInfo.put("hotelImage", hotel.getImageUrl());
                hotelInfo.put("rating", hotel.getRatingStar());

                wishlistRepository.addToWishlist(userId, hotel.getId(), hotelInfo).addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    refreshBothAdapters();
                });
            }
        });
    }
    private void refreshBothAdapters() {
        if (featuredAdapter != null) {
            featuredAdapter.updateData(featuredHotels);
        }
        if (suggestionsAdapter != null) {
            suggestionsAdapter.updateData(suggestionHotels);
        }
    }
    private void loadHotels() {
        showLoading();
        
        allHotels.clear();
        allHotels.addAll(DemoHotelData.hotels());

        db.collection(AppConstants.COLLECTION_HOTELS)
                .whereEqualTo("approval_status", AppConstants.STATUS_APPROVED)
                .whereEqualTo("is_active", true)
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
        if (category.equalsIgnoreCase("Tất cả")) {
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
        
        if (suggestionHotels.isEmpty() && !category.equalsIgnoreCase("Tất cả")) {
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

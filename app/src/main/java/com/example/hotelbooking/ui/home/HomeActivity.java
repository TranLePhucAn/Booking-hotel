package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.hotelbooking.R;
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

    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private int totalPages = 0;
    // Danh sách hiển thị của từng trang
    private final List<Hotel> pageHotels = new ArrayList<>();

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

//    private void updateUserUI() {
//        binding.btnProfile.setEnabled(true);
//        binding.btnProfile.setAlpha(1f);
////        binding.btnLogout.setEnabled(true);
////        binding.btnLogout.setAlpha(1f);
////        binding.btnLogout.setText("Đăng xuất");
//
//        if (FirebaseClient.getAuth().getCurrentUser() != null) {
//            String name = FirebaseClient.getAuth().getCurrentUser().getDisplayName();
//            binding.btnProfile.setText(name != null && !name.isEmpty() ? name : "Tài khoản");
////            binding.btnLogout.setVisibility(View.VISIBLE);
//            binding.btnProfile.setOnClickListener(v ->
//                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
//        } else {
//            binding.btnProfile.setText("Đăng nhập");
////            binding.btnLogout.setVisibility(View.GONE);
//            binding.btnProfile.setOnClickListener(v ->
//                    startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
//        }
//    }

    private void updateUserUI() {

        binding.btnProfile.setEnabled(true);
        binding.btnProfile.setAlpha(1f);

        if (FirebaseClient.getAuth().getCurrentUser() != null) {

            String name = FirebaseClient.getAuth().getCurrentUser().getDisplayName();

            binding.tvProfileName.setText(
                    name != null && !name.isEmpty()
                            ? name
                            : "Tài khoản"
            );

            binding.btnProfile.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        } else {

            binding.tvProfileName.setText("Đăng nhập");

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

//        binding.btnLogout.setOnClickListener(v -> {
//            binding.btnLogout.setEnabled(false);
//            binding.btnLogout.setAlpha(0.65f);
//            binding.btnLogout.setText("Đang đăng xuất...");
//            FirebaseClient.getAuth().signOut();
//            updateUserUI();
//            Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
//        });

        binding.btnFilter.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        
        binding.btnRetry.setOnClickListener(v -> loadHotels());
        
        binding.tvSeeAllFeatured.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        binding.btnPrev.setOnClickListener(v->{
            if(currentPage>0){
                showSuggestionPage(currentPage-1);
            }
        });

        binding.btnNext.setOnClickListener(v->{
            if(currentPage<totalPages-1){
                showSuggestionPage(currentPage+1);
            }
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
        suggestionsAdapter = new HotelAdapter(pageHotels, false, hotelClickListener);
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
                    showError();
                    Toast.makeText(this, "Không tải được khách sạn từ Firebase", Toast.LENGTH_SHORT).show();
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
        currentPage = 0;
        totalPages = (int) Math.ceil((double) suggestionHotels.size() / PAGE_SIZE);
        showSuggestionPage(currentPage);
    }

    private void showSuggestionPage(int page) {

        this.currentPage = page;

        pageHotels.clear();

        int start = page * PAGE_SIZE;

        int end = Math.min(start + PAGE_SIZE, suggestionHotels.size());

        pageHotels.addAll(suggestionHotels.subList(start, end));

        suggestionsAdapter.updateData(pageHotels);

        updatePagination();
    }
    private void updatePagination(){

        binding.layoutPageNumbers.removeAllViews();

        int startPage;
        int endPage;

        if(totalPages<=3){

            startPage=0;

            endPage=totalPages-1;

        }else{

            if(currentPage==0){
                startPage=0;
                endPage=2;

            }else if(currentPage==totalPages-1){
                startPage=totalPages-3;
                endPage=totalPages-1;

            }else{
                startPage=currentPage-1;
                endPage=currentPage+1;
            }
        }

        for(int i=startPage;i<=endPage;i++){

            TextView tv=new TextView(this);

            tv.setText(String.valueOf(i+1));

            tv.setPadding(30,15,30,15);

            tv.setTextSize(16);

            if(i==currentPage){

                tv.setBackgroundResource(R.drawable.bg_page_selected);

                tv.setTextColor(getColor(R.color.white));

            }else{

                tv.setTextColor(getColor(R.color.primary));

            }

            int page=i;

            tv.setOnClickListener(v->showSuggestionPage(page));

            binding.layoutPageNumbers.addView(tv);

        }

        binding.btnPrev.setEnabled(currentPage>0);

        binding.btnNext.setEnabled(currentPage<totalPages-1);

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

        currentPage = 0;
        totalPages = (int) Math.ceil((double) suggestionHotels.size() / PAGE_SIZE);
        showSuggestionPage(currentPage);
        
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

package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.databinding.ActivityHomeBinding;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
// Nếu màn hình chi tiết của em ở package khác, hãy import nó vào đây nhé
// Ví dụ: import com.example.hotelbooking.ui.detail.ProductDetailActivity; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private RecyclerView rvCategories, rvFeaturedHotels;
    private CategoryAdapter categoryAdapter;
    private HotelAdapter hotelAdapter;
    private List<Hotel> featuredHotels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupCategories();
        setupFeaturedHotels();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        rvFeaturedHotels = findViewById(R.id.rvFeaturedHotels);
        LinearLayout searchBar = findViewById(R.id.searchBar);
        TextView btnProfile = findViewById(R.id.btnProfile);
        ImageView btnFilter = findViewById(R.id.btnFilter);

        // Chuyển sang màn hình tìm kiếm khi nhấn vào thanh search
        if (searchBar != null) {
            searchBar.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            });
        }

        // Hiển thị thông tin user từ Firebase
        if (FirebaseClient.getAuth().getCurrentUser() != null) {
            String name = FirebaseClient.getAuth().getCurrentUser().getDisplayName();
            String email = FirebaseClient.getAuth().getCurrentUser().getEmail();

            if (name != null && !name.isEmpty()) btnProfile.setText(name);
        }

        // Xử lý chuyển sang Profile
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        // Nút đăng xuất
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseClient.getAuth().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            });
        }

        // Nút lọc
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            });
        }
    }

    private void setupCategories() {
        List<String> categories = Arrays.asList("Tất cả", "Resort", "Khách sạn", "Villa", "Homestay", "Căn hộ");
        categoryAdapter = new CategoryAdapter(categories, category -> {
            Toast.makeText(this, "Danh mục: " + category, Toast.LENGTH_SHORT).show();
        });
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvCategories.setAdapter(categoryAdapter);
        }
    }

    private void setupFeaturedHotels() {
        featuredHotels = new ArrayList<>();
        
        // Cập nhật dữ liệu mẫu chạy theo Constructor 11 tham số mới của file Hotel.java
        featuredHotels.add(new Hotel(
                "1", 
                "Vinpearl Resort & Spa", 
                "Đảo Hòn Tre, Nha Trang", 
                250.0, 
                4.8f, 
                "https://vcdn1-dulich.vnecdn.net/2022/04/15/v-1650013394.jpg", 
                "Resort", 
                "Khu nghỉ dưỡng sang trọng với bãi biển riêng và công viên giải trí.",
                Arrays.asList("https://vcdn1-dulich.vnecdn.net/2022/04/15/v-1650013394.jpg", "https://vcdn1-dulich.vnecdn.net/2022/04/15/v-1650013394.jpg"),
                Arrays.asList("Wifi", "Hồ bơi", "Spa", "Nhà hàng"),
                12.2197, 
                109.2435
        ));
        
        featuredHotels.add(new Hotel(
                "2", 
                "InterContinental Danang", 
                "Bán đảo Sơn Trà, Đà Nẵng", 
                450.0, 
                5.0f, 
                "https://pix10.agoda.net/hotelImages/301136/-1/6c30e20f2637956e5454b52b3112a688.jpg", 
                "Khách sạn", 
                "Nằm trên sườn đồi với tầm nhìn tuyệt đẹp ra biển Đông.",
                Arrays.asList("https://pix10.agoda.net/hotelImages/301136/-1/6c30e20f2637956e5454b52b3112a688.jpg"),
                Arrays.asList("Wifi", "Bãi biển riêng", "Bar", "Gym"),
                16.1219, 
                108.2782
        ));

        hotelAdapter = new HotelAdapter(featuredHotels, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                // KHI BẤM VÀO ITEM: Mở màn hình Chi tiết khách sạn của An và truyền Object sang
              
                Intent intent = new Intent(HomeActivity.this, HotelDetailActivity.class);
                intent.putExtra("hotel", hotel);
                startActivity(intent);
            }

            @Override
            public void onBookClick(Hotel hotel) {
                Toast.makeText(HomeActivity.this, "Đặt phòng: " + hotel.getHotelName(), Toast.LENGTH_SHORT).show();
            }
        });

        if (rvFeaturedHotels != null) {
            rvFeaturedHotels.setLayoutManager(new LinearLayoutManager(this));
            rvFeaturedHotels.setAdapter(hotelAdapter);
        }
    }
}

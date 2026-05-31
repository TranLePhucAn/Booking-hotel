package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories, rvFeaturedHotels;
    private CategoryAdapter categoryAdapter;
    private HotelAdapter hotelAdapter;
    private List<Hotel> featuredHotels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        Button btnLogout = findViewById(R.id.btnLogout);
//
//        btnLogout.setOnClickListener(v -> {
//            // Đăng xuất khỏi Firebase
//            FirebaseClient.getAuth().signOut();
//
//            // Chuyển về màn hình Login
//            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
//            finish(); // Đóng HomeActivity
//        });

        initViews();
        setupCategories();
        setupFeaturedHotels();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        rvFeaturedHotels = findViewById(R.id.rvFeaturedHotels);
        LinearLayout searchBar = findViewById(R.id.searchBar);
        ImageView btnLogout = findViewById(R.id.btnLogout);
        ImageView btnFilter = findViewById(R.id.btnFilter);

        // Chuyển sang màn hình tìm kiếm khi nhấn vào thanh search
        if (searchBar != null) {
            searchBar.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            });
        }

        // Nút đăng xuất (Sử dụng ImageView để tránh ClassCastException)
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
        featuredHotels.add(new Hotel("1", "Vinpearl Resort & Spa", "Nha Trang", 200, 4.8f, "", "Resort", "Nghỉ dưỡng đẳng cấp"));
        featuredHotels.add(new Hotel("2", "InterContinental", "Đà Nẵng", 350, 4.9f, "", "Khách sạn", "View biển cực đẹp"));
        featuredHotels.add(new Hotel("3", "JW Marriott", "Phú Quốc", 400, 5.0f, "", "Resort", "Không gian sang trọng"));

        hotelAdapter = new HotelAdapter(featuredHotels, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                Toast.makeText(HomeActivity.this, "Chi tiết: " + hotel.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBookClick(Hotel hotel) {
                Toast.makeText(HomeActivity.this, "Đặt phòng: " + hotel.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        if (rvFeaturedHotels != null) {
            rvFeaturedHotels.setLayoutManager(new LinearLayoutManager(this));
            rvFeaturedHotels.setAdapter(hotelAdapter);
        }
    }
}

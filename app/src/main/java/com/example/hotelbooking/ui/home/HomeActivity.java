package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.databinding.ActivityHomeBinding;
import com.example.hotelbooking.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private HotelAdapter adapter;
    private List<Hotel> hotelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        loadSampleData();

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseClient.getAuth().signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupRecyclerView() {
        hotelList = new ArrayList<>();
        adapter = new HotelAdapter(hotelList, this);
        binding.rvHotels.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHotels.setAdapter(adapter);
    }

    private void loadSampleData() {
        // Dữ liệu mẫu để kiểm tra giao diện
        Hotel hotel1 = new Hotel(
                "1",
                "Vinpearl Resort & Spa",
                "Đảo Hòn Tre, Nha Trang",
                "Khu nghỉ dưỡng sang trọng với bãi biển riêng và công viên giải trí.",
                250.0,
                4.8f,
                "https://vcdn1-dulich.vnecdn.net/2022/04/15/v-1650013394.jpg",
                Arrays.asList("https://vcdn1-dulich.vnecdn.net/2022/04/15/v-1650013394.jpg", "https://vcdn1-dulich.vnecdn.net/2022/04/15/v-1650013394.jpg"),
                Arrays.asList("Wifi", "Hồ bơi", "Spa", "Nhà hàng"),
                12.2197,
                109.2435
        );

        Hotel hotel2 = new Hotel(
                "2",
                "InterContinental Danang",
                "Bán đảo Sơn Trà, Đà Nẵng",
                "Nằm trên sườn đồi với tầm nhìn tuyệt đẹp ra biển Đông.",
                450.0,
                5.0f,
                "https://pix10.agoda.net/hotelImages/301136/-1/6c30e20f2637956e5454b52b3112a688.jpg",
                Arrays.asList("https://pix10.agoda.net/hotelImages/301136/-1/6c30e20f2637956e5454b52b3112a688.jpg"),
                Arrays.asList("Wifi", "Bãi biển riêng", "Bar", "Gym"),
                16.1219,
                108.2782
        );

        hotelList.add(hotel1);
        hotelList.add(hotel2);
        adapter.notifyDataSetChanged();
    }
}

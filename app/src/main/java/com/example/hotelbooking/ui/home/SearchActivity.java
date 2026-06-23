package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.DemoHotelData;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnBack;
    private ImageView btnFilter;
    private RecyclerView rvSearchResults;
    private HotelAdapter hotelAdapter;
    private final List<Hotel> allHotels = new ArrayList<>();
    private final List<Hotel> filteredHotels = new ArrayList<>();

    private double maxPriceFilter = 10000000;
    private float minRatingFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadHotels();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilterSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
    }

    private void setupRecyclerView() {
        hotelAdapter = new HotelAdapter(filteredHotels, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                openHotelDetail(hotel);
            }

            @Override
            public void onBookClick(Hotel hotel) {
                openHotelDetail(hotel);
            }
        });
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(hotelAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFilter.setOnClickListener(v -> showFilterDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadHotels() {
        FirebaseFirestore.getInstance()
                .collection("hotels")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allHotels.clear();
                    querySnapshot.getDocuments().forEach(document ->
                            allHotels.add(Hotel.fromDocument(document)));
                    if (allHotels.isEmpty()) {
                        allHotels.addAll(DemoHotelData.hotels());
                    }
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    allHotels.clear();
                    allHotels.addAll(DemoHotelData.hotels());
                    applyFilters();
                    Toast.makeText(this, "Dang tim kiem tren du lieu mau", Toast.LENGTH_SHORT).show();
                });
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        dialog.setContentView(view);

        SeekBar sbPrice = view.findViewById(R.id.sbPrice);
        TextView tvPriceValue = view.findViewById(R.id.tvPriceValue);
        android.widget.RatingBar rbStars = view.findViewById(R.id.rbStars);
        Button btnApply = view.findViewById(R.id.btnApplyFilter);

        int progress = (int) Math.min(maxPriceFilter, sbPrice.getMax());
        sbPrice.setProgress(progress);
        tvPriceValue.setText(formatMoney(progress));
        rbStars.setRating(minRatingFilter);

        sbPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPriceValue.setText(formatMoney(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnApply.setOnClickListener(v -> {
            maxPriceFilter = sbPrice.getProgress();
            minRatingFilter = rbStars.getRating();
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        filteredHotels.clear();

        for (Hotel hotel : allHotels) {
            String name = safeLower(hotel.getHotelName());
            String location = safeLower(hotel.getAddress());
            boolean matchQuery = query.isEmpty() || name.contains(query) || location.contains(query);
            boolean matchPrice = hotel.getPrice() <= maxPriceFilter;
            boolean matchRating = hotel.getRating() >= minRatingFilter;

            if (matchQuery && matchPrice && matchRating) {
                filteredHotels.add(hotel);
            }
        }

        hotelAdapter.updateData(filteredHotels);
    }

    private void openHotelDetail(Hotel hotel) {
        Intent intent = new Intent(SearchActivity.this, HotelDetailActivity.class);
        intent.putExtra("hotel_id", hotel.getId());
        intent.putExtra("hotel", hotel);
        startActivity(intent);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0f VND", value);
    }
}

package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnBack, btnFilter;
    private RecyclerView rvSearchResults;
    private HotelAdapter hotelAdapter;
    private List<Hotel> allHotels;
    private List<Hotel> filteredHotels;
    
    private double maxPriceFilter = 1000;
    private float minRatingFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        loadData();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilterSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
    }

    private void loadData() {
        allHotels = new ArrayList<>();
        allHotels.add(new Hotel("1", "Vinpearl Resort & Spa", "Nha Trang", 200, 4.8f, "", "Resort", ""));
        allHotels.add(new Hotel("2", "InterContinental", "Đà Nẵng", 350, 4.9f, "", "Khách sạn", ""));
        allHotels.add(new Hotel("3", "JW Marriott", "Phú Quốc", 400, 5.0f, "", "Resort", ""));
        allHotels.add(new Hotel("4", "Pullman Hotel", "Vũng Tàu", 150, 4.5f, "", "Khách sạn", ""));
        allHotels.add(new Hotel("5", "Muong Thanh", "Hà Nội", 100, 4.2f, "", "Khách sạn", ""));
        allHotels.add(new Hotel("6", "Hanoi Hotel", "Hà Nội", 80, 4.0f, "", "Khách sạn", ""));
        allHotels.add(new Hotel("7", "Da Lat Palace", "Đà Lạt", 180, 4.7f, "", "Resort", ""));

        filteredHotels = new ArrayList<>(allHotels);
    }

    private void setupRecyclerView() {
        hotelAdapter = new HotelAdapter(filteredHotels, new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel) {
                // Navigate to Detail
            }

            @Override
            public void onBookClick(Hotel hotel) {
                // Process booking
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        dialog.setContentView(view);

        SeekBar sbPrice = view.findViewById(R.id.sbPrice);
        TextView tvPriceValue = view.findViewById(R.id.tvPriceValue);
        // RatingBar is used for display in my dialog_filter.xml, but I should probably use a simpler selection for filtering
        // Let's assume the user can pick stars. In the layout I used a RatingBar.
        android.widget.RatingBar rbStars = view.findViewById(R.id.rbStars);
        Button btnApply = view.findViewById(R.id.btnApplyFilter);

        sbPrice.setProgress((int) maxPriceFilter);
        tvPriceValue.setText(String.format("%.0f USD", maxPriceFilter));
        rbStars.setRating(minRatingFilter);

        sbPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPriceValue.setText(progress + " USD");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
        String query = etSearch.getText().toString().toLowerCase();
        
        filteredHotels = allHotels.stream()
                .filter(hotel -> (hotel.getName().toLowerCase().contains(query) || 
                                 hotel.getLocation().toLowerCase().contains(query)))
                .filter(hotel -> hotel.getPrice() <= maxPriceFilter)
                .filter(hotel -> hotel.getRating() >= minRatingFilter)
                .collect(Collectors.toList());

        hotelAdapter.updateData(filteredHotels);
    }
}

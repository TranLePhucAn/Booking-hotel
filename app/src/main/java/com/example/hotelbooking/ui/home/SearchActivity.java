package com.example.hotelbooking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {

    private AutoCompleteTextView etSearch;
    private ImageView btnBack, btnFilter;
    private RecyclerView rvSearchResults;
    private ProgressBar loadingView;
    private LinearLayout emptyView, errorView;
    private Button btnRetry;
    
    private HotelAdapter hotelAdapter;
    private final List<Hotel> allHotels = new ArrayList<>();
    private final List<Hotel> filteredHotels = new ArrayList<>();

    private double maxPriceFilter = 10000000;
    private float minRatingFilter = 0;
    private int selectedSortIndex = 0;

    private final String[] sortOptions = {
            "Mặc định",
            "Giá tăng dần",
            "Giá giảm dần",
            "Rating cao nhất",
            "Mới nhất",
            "Nổi bật"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupAutoComplete();
        setupRecyclerView();
        setupListeners();
        loadHotels();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilterSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        loadingView = findViewById(R.id.loadingView);
        emptyView = findViewById(R.id.emptyView);
        errorView = findViewById(R.id.errorView);
        btnRetry = findViewById(R.id.btnRetrySearch);
    }

    private void setupAutoComplete() {
        if (etSearch == null) return;
        List<String> locations = DemoHotelData.getLocations();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, locations);
        etSearch.setAdapter(adapter);
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
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnFilter != null) btnFilter.setOnClickListener(v -> showFilterDialog());
        if (btnRetry != null) btnRetry.setOnClickListener(v -> loadHotels());

        if (etSearch != null) {
            etSearch.setOnItemClickListener((parent, view, position, id) -> applyFilters());
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
    }

    private void loadHotels() {
        showLoading();
        
        allHotels.clear();
        allHotels.addAll(DemoHotelData.hotels());

        FirebaseFirestore.getInstance()
                .collection("hotels")
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
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    if (allHotels.isEmpty()) {
                        showError();
                    } else {
                        applyFilters();
                        Toast.makeText(this, "Lỗi kết nối Firestore, đang dùng dữ liệu mẫu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyFilters() {
        if (etSearch == null) return;
        String rawQuery = etSearch.getText().toString().trim();
        String normalizedQuery = removeAccents(rawQuery);
        
        filteredHotels.clear();

        for (Hotel hotel : allHotels) {
            String hName = hotel.getName() != null ? hotel.getName() : (hotel.getHotelName() != null ? hotel.getHotelName() : "");
            String hAddress = hotel.getAddress() != null ? hotel.getAddress() : "";
            
            String normalizedName = removeAccents(hName);
            String normalizedLocation = removeAccents(hAddress);
            
            boolean matchQuery = normalizedQuery.isEmpty() || 
                                normalizedName.contains(normalizedQuery) || 
                                normalizedLocation.contains(normalizedQuery);
                                
            boolean matchPrice = hotel.getPrice() <= maxPriceFilter;
            
            double score = hotel.getRating() > 0 ? hotel.getRating() : hotel.getRatingStar();
            boolean matchRating = score >= minRatingFilter;

            if (matchQuery && matchPrice && matchRating) {
                filteredHotels.add(hotel);
            }
        }

        applySorting();
        
        if (filteredHotels.isEmpty()) {
            showEmpty();
        } else {
            showContent();
            if (hotelAdapter != null) {
                hotelAdapter.updateData(filteredHotels);
            }
        }
    }

    private void showLoading() {
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (errorView != null) errorView.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
    }

    private void showEmpty() {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
        if (errorView != null) errorView.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
    }

    private void showError() {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (errorView != null) errorView.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
    }

    private void showContent() {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        if (errorView != null) errorView.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);
    }

    private void applySorting() {
        switch (selectedSortIndex) {
            case 1: // Giá tăng dần
                Collections.sort(filteredHotels, (h1, h2) -> Double.compare(h1.getPrice(), h2.getPrice()));
                break;
            case 2: // Giá giảm dần
                Collections.sort(filteredHotels, (h1, h2) -> Double.compare(h2.getPrice(), h1.getPrice()));
                break;
            case 3: // Rating cao nhất
                Collections.sort(filteredHotels, (h1, h2) -> {
                    double s1 = h1.getRating() > 0 ? h1.getRating() : h1.getRatingStar();
                    double s2 = h2.getRating() > 0 ? h2.getRating() : h2.getRatingStar();
                    return Double.compare(s2, s1);
                });
                break;
            case 4: // Mới nhất
                Collections.sort(filteredHotels, (h1, h2) -> {
                    Timestamp t1 = h1.getCreatedAt();
                    Timestamp t2 = h2.getCreatedAt();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t2.compareTo(t1);
                });
                break;
            case 5: // Nổi bật
                Collections.sort(filteredHotels, (h1, h2) -> Boolean.compare(h2.isFeatured(), h1.isFeatured()));
                break;
        }
    }

    private String removeAccents(String str) {
        if (str == null) return "";
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(nfdNormalizedString).replaceAll("");
        return result.toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replace("Đ", "d");
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        dialog.setContentView(view);

        Spinner spSort = view.findViewById(R.id.spSort);
        SeekBar sbPrice = view.findViewById(R.id.sbPrice);
        TextView tvPriceValue = view.findViewById(R.id.tvPriceValue);
        android.widget.RatingBar rbStars = view.findViewById(R.id.rbStars);
        Button btnApply = view.findViewById(R.id.btnApplyFilter);

        if (spSort != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSort.setAdapter(adapter);
            spSort.setSelection(selectedSortIndex);
        }

        if (sbPrice != null) {
            int progress = (int) Math.min(maxPriceFilter, sbPrice.getMax());
            sbPrice.setProgress(progress);
            if (tvPriceValue != null) tvPriceValue.setText(formatMoney(progress));
            sbPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (tvPriceValue != null) tvPriceValue.setText(formatMoney(progress));
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (rbStars != null) rbStars.setRating(minRatingFilter);

        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                if (spSort != null) selectedSortIndex = spSort.getSelectedItemPosition();
                if (sbPrice != null) maxPriceFilter = sbPrice.getProgress();
                if (rbStars != null) minRatingFilter = rbStars.getRating();
                applyFilters();
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private boolean containsHotel(String hotelId) {
        for (Hotel hotel : allHotels) {
            if (hotelId != null && hotelId.equals(hotel.getId())) return true;
        }
        return false;
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

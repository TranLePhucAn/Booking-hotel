package com.example.hotelbooking.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddHotelActivity extends AppCompatActivity {

    private EditText edtBusinessName;
    private EditText edtHotelName;
    private EditText edtDescription;
    private EditText edtAddress;
    private EditText edtCity;
    private EditText edtDistrict;
    private EditText edtRatingStar;
    private EditText edtPriceFrom;
    private EditText edtAmenities;
    private EditText edtMainImageUrl;
    private EditText edtImageUrls;
    private EditText edtLatitude;
    private EditText edtLongitude;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hotel);

        db = FirebaseFirestore.getInstance();
        bindViews();

        Button btnSubmitHotel = findViewById(R.id.btnSubmitHotel);
        btnSubmitHotel.setOnClickListener(v -> submitHotel());
    }

    private void bindViews() {
        edtBusinessName = findViewById(R.id.edtBusinessName);
        edtHotelName = findViewById(R.id.edtHotelName);
        edtDescription = findViewById(R.id.edtDescription);
        edtAddress = findViewById(R.id.edtAddress);
        edtCity = findViewById(R.id.edtCity);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtRatingStar = findViewById(R.id.edtRatingStar);
        edtPriceFrom = findViewById(R.id.edtPriceFrom);
        edtAmenities = findViewById(R.id.edtAmenities);
        edtMainImageUrl = findViewById(R.id.edtMainImageUrl);
        edtImageUrls = findViewById(R.id.edtImageUrls);
        edtLatitude = findViewById(R.id.edtLatitude);
        edtLongitude = findViewById(R.id.edtLongitude);
    }

    private void submitHotel() {
        String hotelName = textOf(edtHotelName);
        String address = textOf(edtAddress);
        String description = textOf(edtDescription);
        double priceFrom = doubleOf(edtPriceFrom, 0);
        String mainImage = textOf(edtMainImageUrl);

        if (TextUtils.isEmpty(hotelName) || TextUtils.isEmpty(address) || TextUtils.isEmpty(description)
                || priceFrom <= 0 || TextUtils.isEmpty(mainImage)) {
            Toast.makeText(this, "Vui long nhap day du thong tin bat buoc.", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        Map<String, Object> location = new HashMap<>();
        location.put("address", address);
        location.put("city", textOf(edtCity));
        location.put("district", textOf(edtDistrict));
        location.put("latitude", doubleOf(edtLatitude, 0));
        location.put("longitude", doubleOf(edtLongitude, 0));
        location.put("owner_id", ownerId);
        location.put("created_at", System.currentTimeMillis());

        db.collection("locations")
                .add(location)
                .addOnSuccessListener(locationRef -> createHotel(ownerId, locationRef.getId()))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong luu duoc vi tri: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void createHotel(String ownerId, String locationId) {
        List<String> secondaryImages = listOf(textOf(edtImageUrls));
        List<String> allImages = new ArrayList<>();
        String mainImage = textOf(edtMainImageUrl);
        if (!mainImage.isEmpty()) {
            allImages.add(mainImage);
        }
        allImages.addAll(secondaryImages);

        Map<String, Object> hotel = new HashMap<>();
        hotel.put("owner_id", ownerId);
        hotel.put("business_id", ownerId);
        hotel.put("business_name", textOf(edtBusinessName));
        hotel.put("hotel_name", textOf(edtHotelName));
        hotel.put("description", textOf(edtDescription));
        hotel.put("address_text", textOf(edtAddress));
        hotel.put("city", textOf(edtCity));
        hotel.put("district", textOf(edtDistrict));
        hotel.put("rating_star", (float) doubleOf(edtRatingStar, 0));
        hotel.put("price_from", doubleOf(edtPriceFrom, 0));
        hotel.put("amenities", listOf(textOf(edtAmenities)));
        hotel.put("image_url", mainImage);
        hotel.put("image_urls", allImages);
        hotel.put("review_score", 0);
        hotel.put("review_count", 0);
        hotel.put("location_id", locationId);
        hotel.put("status", "pending");
        hotel.put("created_at", System.currentTimeMillis());
        hotel.put("updated_at", System.currentTimeMillis());

        db.collection("hotels")
                .add(hotel)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Da tao khach san. Hay them danh sach phong.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, AddRoomActivity.class);
                    intent.putExtra("hotel_id", documentReference.getId());
                    intent.putExtra("hotel_name", textOf(edtHotelName));
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong luu duoc khach san: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }

    private double doubleOf(EditText editText, double fallback) {
        try {
            String value = textOf(editText);
            return value.isEmpty() ? fallback : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private List<String> listOf(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String[] items = value.split(",");
        for (String item : items) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}

package com.example.hotelbooking.ui.partner;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartnerEditHotelActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private String hotelId;
    private String currentUserId;

    private EditText editHotelName;
    private EditText editDescription;
    private EditText editAddress;
    private EditText editRatingStar;
    private EditText editPrice;
    private EditText editAmenities;
    private EditText editMainImageUrl;
    private EditText editImageUrls;
    private EditText editLatitude;
    private EditText editLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_edit_hotel);

        firestore = FirebaseFirestore.getInstance();
        hotelId = getIntent().getStringExtra("hotel_id");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (TextUtils.isEmpty(hotelId) || TextUtils.isEmpty(currentUserId)) {
            Toast.makeText(this, "Không tìm thấy thông tin khách sạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        loadHotel();
    }

    private void bindViews() {
        editHotelName = findViewById(R.id.editHotelName);
        editDescription = findViewById(R.id.editDescription);
        editAddress = findViewById(R.id.editAddress);
        editRatingStar = findViewById(R.id.editRatingStar);
        editPrice = findViewById(R.id.editPrice);
        editAmenities = findViewById(R.id.editAmenities);
        editMainImageUrl = findViewById(R.id.editMainImageUrl);
        editImageUrls = findViewById(R.id.editImageUrls);
        editLatitude = findViewById(R.id.editLatitude);
        editLongitude = findViewById(R.id.editLongitude);

        Button saveButton = findViewById(R.id.buttonSaveHotel);
        saveButton.setOnClickListener(v -> saveHotel());
    }

    private void loadHotel() {
        firestore.collection(AppConstants.COLLECTION_HOTELS)
                .document(hotelId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(this, "Khách sạn không tồn tại", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Hotel hotel = Hotel.fromDocument(document);
                    if (!currentUserId.equals(hotel.getOwnerId())) {
                        Toast.makeText(this, "Bạn không có quyền sửa khách sạn này", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    fillHotelForm(hotel);
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Không tải được khách sạn: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void fillHotelForm(Hotel hotel) {
        editHotelName.setText(hotel.getHotelName());
        editDescription.setText(hotel.getDescription());
        editAddress.setText(hotel.getAddress());
        editRatingStar.setText(String.valueOf(hotel.getRatingStar()));
        editPrice.setText(String.valueOf(hotel.getPrice()));
        editAmenities.setText(String.join(", ", hotel.getAmenities()));
        editMainImageUrl.setText(hotel.getImageUrl());
        editImageUrls.setText(String.join(", ", hotel.getSecondaryImages()));
        editLatitude.setText(String.valueOf(hotel.getLatitude()));
        editLongitude.setText(String.valueOf(hotel.getLongitude()));
    }

    private void saveHotel() {
        String hotelName = textOf(editHotelName);
        String description = textOf(editDescription);
        String address = textOf(editAddress);
        double price = doubleOf(editPrice);
        String mainImageUrl = textOf(editMainImageUrl);

        if (TextUtils.isEmpty(hotelName) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(address) || price <= 0 || TextUtils.isEmpty(mainImageUrl)) {
            Toast.makeText(this, "Vui lòng nhập đủ tên, mô tả, địa chỉ, giá và ảnh chính", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("hotel_name", hotelName);
        updates.put("name", hotelName);
        updates.put("description", description);
        updates.put("address", address);
        updates.put("address_text", address);
        updates.put("price", price);
        updates.put("price_from", price);
        updates.put("rating_star", doubleOf(editRatingStar));
        updates.put("rating", doubleOf(editRatingStar));
        updates.put("amenities", listOf(textOf(editAmenities)));
        updates.put("image_url", mainImageUrl);
        updates.put("imageUrl", mainImageUrl);
        updates.put("image_urls", listOf(textOf(editImageUrls)));
        updates.put("latitude", doubleOf(editLatitude));
        updates.put("longitude", doubleOf(editLongitude));
        updates.put("approval_status", AppConstants.STATUS_PENDING);
        updates.put("is_active", false);
        updates.put("updated_at", System.currentTimeMillis());

        firestore.collection(AppConstants.COLLECTION_HOTELS)
                .document(hotelId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã lưu và gửi duyệt lại", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Không lưu được khách sạn: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }

    private double doubleOf(EditText editText) {
        try {
            return Double.parseDouble(textOf(editText));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private List<String> listOf(String rawText) {
        List<String> result = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return result;
        }
        for (String item : rawText.split(",")) {
            String value = item.trim();
            if (!value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }
}

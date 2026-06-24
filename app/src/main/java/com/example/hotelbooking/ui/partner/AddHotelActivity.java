package com.example.hotelbooking.ui.partner;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddHotelActivity extends AppCompatActivity {

    private EditText edtHotelName;
    private EditText edtDescription;
    private EditText edtAddress;
    private EditText edtCity;
    private EditText edtRatingStar;
    private EditText edtPrice;
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
        edtHotelName = findViewById(R.id.edtHotelName);
        edtDescription = findViewById(R.id.edtDescription);
        edtAddress = findViewById(R.id.edtAddress);
        edtCity = findViewById(R.id.edtCity);
        edtRatingStar = findViewById(R.id.edtRatingStar);
        edtPrice = findViewById(R.id.edtPriceFrom); // Giữ ID cũ nhưng dùng field mới
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
        double price = doubleOf(edtPrice, 0);
        String mainImage = textOf(edtMainImageUrl);

        if (TextUtils.isEmpty(hotelName) || TextUtils.isEmpty(address) || TextUtils.isEmpty(description)
                || price <= 0 || TextUtils.isEmpty(mainImage)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        Hotel hotel = new Hotel();
        hotel.setName(hotelName);
        hotel.setAddress(address);
        hotel.setLocation(textOf(edtCity));
        hotel.setDescription(description);
        hotel.setPrice(price);
        hotel.setRatingStar(doubleOf(edtRatingStar, 0));
        hotel.setRating(0);
        hotel.setImageUrl(mainImage);
        
        List<String> secondaryImages = listOf(textOf(edtImageUrls));
        List<String> allImages = new ArrayList<>();
        allImages.add(mainImage);
        allImages.addAll(secondaryImages);
        hotel.setImageUrls(allImages);
        
        hotel.setAmenities(listOf(textOf(edtAmenities)));
        hotel.setLatitude(doubleOf(edtLatitude, 0));
        hotel.setLongitude(doubleOf(edtLongitude, 0));
        hotel.setOwnerId(ownerId);
        hotel.setApprovalStatus(AppConstants.STATUS_PENDING);
        hotel.setIsActive(false);
        hotel.setCreatedAt(Timestamp.now());
        hotel.setUpdatedAt(Timestamp.now());

        db.collection(AppConstants.COLLECTION_HOTELS)
                .add(hotel)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã gửi yêu cầu tạo khách sạn. Vui lòng chờ duyệt.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, AddRoomActivity.class);
                    intent.putExtra("hotel_id", documentReference.getId());
                    intent.putExtra("hotel_name", hotelName);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không lưu được khách sạn: " + e.getMessage(), Toast.LENGTH_LONG).show());
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

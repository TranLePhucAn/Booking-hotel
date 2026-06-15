package com.example.hotelbooking.ui.hotel;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.DemoHotelData;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.databinding.ActivityHotelDetailBinding;
import com.example.hotelbooking.ui.map.HotelMapActivity;
import com.example.hotelbooking.ui.payment.ConfirmActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelDetailActivity extends AppCompatActivity {

    private ActivityHotelDetailBinding binding;
    private FirebaseFirestore db;
    private Hotel hotel;
    private String hotelId;
    private double latitude;
    private double longitude;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHotelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        hotelId = getIntent().getStringExtra("hotel_id");
        hotel = (Hotel) getIntent().getSerializableExtra("hotel");
        if ((hotelId == null || hotelId.isEmpty()) && hotel != null) {
            hotelId = hotel.getId();
        }

        if (hotelId == null || hotelId.isEmpty()) {
            if (hotel != null) {
                displayHotelDetails();
            } else {
                Toast.makeText(this, "Khong co ma khach san", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else if (isDemoHotel(hotelId)) {
            Hotel demoHotel = DemoHotelData.findHotel(hotelId);
            if (demoHotel != null) {
                hotel = demoHotel;
            }
            displayHotelDetails();
            latitude = hotel.getLatitude();
            longitude = hotel.getLongitude();
            address = hotel.getAddress();
            loadDemoRooms();
            loadDemoReviews();
        } else {
            loadHotel();
        }

        binding.btnShowMap.setOnClickListener(v -> openMap());
        binding.btnBookNow.setOnClickListener(v -> {
            if (isDemoHotel(hotelId)) {
                openFirstDemoRoom();
            } else {
                openBooking(null);
            }
        });
    }

    private void loadHotel() {
        db.collection("hotels")
                .document(hotelId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(this, "Khong tim thay khach san", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    hotel = Hotel.fromDocument(document);
                    displayHotelDetails();
                    loadLocation();
                    loadRooms();
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong doc duoc khach san: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void displayHotelDetails() {
        address = hotel.getAddress();
        binding.tvHotelName.setText(valueOrDefault(hotel.getHotelName(), "Khach san"));
        binding.tvHotelAddress.setText(valueOrDefault(hotel.getAddress(), "Dang cap nhat dia chi"));
        binding.tvDescription.setText(valueOrDefault(hotel.getDescription(), "Dang cap nhat mo ta"));
        binding.tvPrice.setText(formatMoney(hotel.getPrice()) + " / dem");
        binding.ratingBar.setRating((float) hotel.getRatingStar());
        binding.tvHotelRatingInfo.setText(formatHotelRatingInfo());

        if (hotel.getAmenities() != null && !hotel.getAmenities().isEmpty()) {
            binding.tvAmenities.setText(String.join(", ", hotel.getAmenities()));
        } else {
            binding.tvAmenities.setText("Dang cap nhat tien ich");
        }

        List<SlideModel> imageList = new ArrayList<>();
        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            imageList.add(new SlideModel(hotel.getImageUrl(), ScaleTypes.CENTER_CROP));
        }
        if (hotel.getSecondaryImages() != null) {
            for (String imageUrl : hotel.getSecondaryImages()) {
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals(hotel.getImageUrl())) {
                    imageList.add(new SlideModel(imageUrl, ScaleTypes.CENTER_CROP));
                }
            }
        }
        if (!imageList.isEmpty()) {
            binding.imageSlider.setImageList(imageList);
        }
    }

    private void loadLocation() {
        if (hotel.getLocationId() == null || hotel.getLocationId().isEmpty()) {
            useHotelLocationFallback();
            return;
        }

        db.collection("locations")
                .document(hotel.getLocationId())
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        useHotelLocationFallback();
                        return;
                    }

                    latitude = firstDoubleValue(document, hotel.getLatitude(), "latitude", "lat");
                    longitude = firstDoubleValue(document, hotel.getLongitude(), "longitude", "lng", "lon");
                    address = firstStringValue(document, hotel.getAddress(), "address", "address_text");
                    binding.tvHotelAddress.setText(valueOrDefault(address, hotel.getAddress()));
                })
                .addOnFailureListener(e -> useHotelLocationFallback());
    }

    private void loadRooms() {
        binding.layoutRooms.removeAllViews();
        db.collection("rooms")
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        binding.layoutRooms.addView(createText("Chua co phong dang hien thi", false));
                        return;
                    }

                    for (DocumentSnapshot room : querySnapshot.getDocuments()) {
                        addRoomView(room);
                    }
                })
                .addOnFailureListener(e ->
                        binding.layoutRooms.addView(createText("Khong tai duoc phong: " + e.getMessage(), false)));
    }

    private void addRoomView(DocumentSnapshot room) {
        LinearLayout box = createBox();
        String roomName = stringValue(room, "room_name", "Phong");
        String roomType = stringValue(room, "room_type", "");
        double price = doubleValue(room, "price_per_night", hotel.getPrice());
        String bedType = stringValue(room, "bed_type", "");
        String capacity = stringValue(room, "capacity", "");
        String status = stringValue(room, "status", "AVAILABLE");
        int availableRooms = intValue(room, "available_rooms", 0);
        double roomSize = doubleValue(room, "room_size", 0);

        box.addView(createText(roomName, true));
        box.addView(createText(roomType + (roomSize > 0 ? " - " + roomSize + " m2" : ""), false));
        box.addView(createText(bedType, false));
        box.addView(createText(capacity, false));
        box.addView(createText(formatMoney(price) + " / dem", true));
        boolean canBook = "AVAILABLE".equalsIgnoreCase(status) && availableRooms > 0;
        box.addView(createText("Trang thai: " + (canBook ? "Con phong" : "Het phong / Tam ngung phuc vu"), false));

        Button bookButton = new Button(this);
        bookButton.setText(canBook ? "Xem chi tiet / Dat phong" : "Het phong");
        bookButton.setEnabled(canBook);
        bookButton.setOnClickListener(v -> openBooking(room));
        box.addView(bookButton);

        binding.layoutRooms.addView(box);
    }

    private void loadDemoRooms() {
        binding.layoutRooms.removeAllViews();
        for (DemoHotelData.DemoRoom room : DemoHotelData.rooms(hotelId)) {
            addDemoRoomView(room);
        }
    }

    private void addDemoRoomView(DemoHotelData.DemoRoom room) {
        LinearLayout box = createBox();
        box.addView(createText(room.name, true));
        box.addView(createText(room.type + " - " + room.size + " m2", false));
        box.addView(createText(room.bedType, false));
        box.addView(createText(room.capacity, false));
        box.addView(createText(formatMoney(room.price) + " / dem", true));

        boolean canBook = "AVAILABLE".equalsIgnoreCase(room.status) && room.availableRooms > 0;
        box.addView(createText("Trang thai: " + (canBook ? "Con " + room.availableRooms + " phong" : "Het phong / Tam ngung phuc vu"), false));

        Button bookButton = new Button(this);
        bookButton.setText(canBook ? "Xem chi tiet / Dat phong" : "Het phong");
        bookButton.setEnabled(canBook);
        bookButton.setOnClickListener(v -> openDemoBooking(room));
        box.addView(bookButton);
        binding.layoutRooms.addView(box);
    }

    private void loadReviews() {
        binding.layoutReviews.removeAllViews();
        db.collection("reviews")
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        binding.layoutReviews.addView(createText("Chua co danh gia", false));
                        return;
                    }

                    for (DocumentSnapshot review : querySnapshot.getDocuments()) {
                        addReviewView(review);
                    }
                })
                .addOnFailureListener(e ->
                        binding.layoutReviews.addView(createText("Khong tai duoc danh gia: " + e.getMessage(), false)));
    }

    private void addReviewView(DocumentSnapshot review) {
        LinearLayout box = createBox();
        String userName = stringValue(review, "user_name", stringValue(review, "reviewer_name", "Khach hang"));
        double rating = doubleValue(review, "rating", doubleValue(review, "score", 0));
        String comment = stringValue(review, "comment", stringValue(review, "content", ""));

        box.addView(createText(userName + ": " + formatNumber(rating) + " sao", true));
        box.addView(createText(comment, false));
        binding.layoutReviews.addView(box);
    }

    private void loadDemoReviews() {
        binding.layoutReviews.removeAllViews();
        for (DemoHotelData.DemoReview review : DemoHotelData.reviews(hotelId)) {
            LinearLayout box = createBox();
            box.addView(createText(review.userName + ": " + formatNumber(review.rating) + " sao", true));
            box.addView(createText(review.comment, false));
            binding.layoutReviews.addView(box);
        }
    }

    private void openMap() {
        ensureMapLocationReady();
        Intent intent = new Intent(this, HotelMapActivity.class);
        intent.putExtra("hotel_name", hotel != null ? hotel.getHotelName() : "");
        intent.putExtra("address", address);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    private void openBooking(DocumentSnapshot room) {
        if (hotel == null) {
            return;
        }

        Intent intent = new Intent(this, ConfirmActivity.class);
        intent.putExtra("hotel_id", hotelId);
        intent.putExtra("hotel_name", hotel.getHotelName());
        intent.putExtra("hotel_address", address);
        intent.putExtra("hotel_image", hotel.getImageUrl());
        intent.putExtra("owner_id", hotel.getOwnerId());
        intent.putExtra("review_score", hotel.getRatingStar());
        intent.putExtra("rating_star", hotel.getRatingStar());
        intent.putExtra("review_count", hotel.getReviewCount());

        if (room != null) {
            intent.putExtra("room_id", room.getId());
            intent.putExtra("room_name", stringValue(room, "room_name", "Phong"));
            intent.putExtra("room_price", doubleValue(room, "price_per_night", hotel.getPrice()));
        } else {
            intent.putExtra("room_name", "Phong tieu chuan");
            intent.putExtra("room_price", hotel.getPrice());
        }
        startActivity(intent);
    }

    private void openDemoBooking(DemoHotelData.DemoRoom room) {
        if (hotel == null) {
            return;
        }

        Intent intent = new Intent(this, ConfirmActivity.class);
        intent.putExtra("hotel_id", hotelId);
        intent.putExtra("hotel_name", hotel.getHotelName());
        intent.putExtra("hotel_address", address);
        intent.putExtra("hotel_image", hotel.getImageUrl());
        intent.putExtra("owner_id", hotel.getOwnerId());
        intent.putExtra("review_score", hotel.getRatingStar());
        intent.putExtra("rating_star", hotel.getRatingStar());
        intent.putExtra("review_count", hotel.getReviewCount());
        intent.putExtra("room_id", room.id);
        intent.putExtra("room_name", room.name);
        intent.putExtra("room_price", room.price);
        startActivity(intent);
    }

    private void openFirstDemoRoom() {
        for (DemoHotelData.DemoRoom room : DemoHotelData.rooms(hotelId)) {
            if ("AVAILABLE".equalsIgnoreCase(room.status) && room.availableRooms > 0) {
                openDemoBooking(room);
                return;
            }
        }
        Toast.makeText(this, "Khach san hien chua co phong trong", Toast.LENGTH_SHORT).show();
    }

    private boolean isDemoHotel(String id) {
        return id != null && id.startsWith("demo_");
    }

    private LinearLayout createBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(14, 12, 14, 12);
        box.setBackgroundResource(R.drawable.bg_input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 10);
        box.setLayoutParams(params);
        return box;
    }

    private TextView createText(String text, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(valueOrDefault(text, ""));
        textView.setTextColor(getColor(R.color.text_primary));
        textView.setTextSize(14);
        textView.setPadding(0, 3, 0, 3);
        if (bold) {
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        }
        return textView;
    }

    private String stringValue(DocumentSnapshot document, String field, String fallback) {
        String value = document.getString(field);
        return value == null ? fallback : value;
    }

    private String firstStringValue(DocumentSnapshot document, String fallback, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return fallback;
    }

    private double doubleValue(DocumentSnapshot document, String field, double fallback) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return fallback;
    }

    private double firstDoubleValue(DocumentSnapshot document, double fallback, String... fields) {
        for (String field : fields) {
            Object value = document.get(field);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return fallback;
    }

    private int intValue(DocumentSnapshot document, String field, int fallback) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return fallback;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private void useHotelLocationFallback() {
        if (hotel == null) {
            return;
        }
        if (latitude == 0 && longitude == 0) {
            latitude = hotel.getLatitude();
            longitude = hotel.getLongitude();
        }
        address = valueOrDefault(address, hotel.getAddress());
    }

    private void ensureMapLocationReady() {
        useHotelLocationFallback();
        if (latitude == 0 && longitude == 0) {
            latitude = 10.8131;
            longitude = 106.6658;
        }
        address = valueOrDefault(address, hotel != null ? hotel.getAddress() : "Dang cap nhat");
    }

    private String formatHotelRatingInfo() {
        double reviewScore = hotel.getReviewScore();
        int reviewCount = hotel.getReviewCount();
        double ratingStar = hotel.getRatingStar();

        String scoreText = reviewScore > 0 ? formatNumber(reviewScore) + "/10" : "Chua co diem";
        String reviewText = reviewCount > 0 ? reviewCount + " danh gia" : "Chua co danh gia";
        return formatNumber(ratingStar) + " sao  -  " + scoreText + "  -  " + reviewText;
    }

    private String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0f VND", value);
    }

    private String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

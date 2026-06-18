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
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.model.Room;
import com.example.hotelbooking.data.model.Section;
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

        // Nhận thông tin từ dữ liệu thật truyền qua Intent
        hotelId = getIntent().getStringExtra("hotel_id");
        hotel = (Hotel) getIntent().getSerializableExtra("hotel");

        // lấy id hotel hiện tại
        if ((hotelId == null || hotelId.isEmpty()) && hotel != null) {
            hotelId = hotel.getId();
        }

        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã khách sạn hợp lệ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (hotel != null) {
            initHotelFlow();
        } else {
            loadHotelFromFirestore();
        }

        // hiển thị bảng đồ
        binding.btnShowMap.setOnClickListener(v -> openMap());

        // xử lý nút đặt ngay bằng cách tự động chọn phòng có trạng thái available đầu tiên
        binding.btnBookNow.setOnClickListener(v -> {
            // truy vấn bảng rooms
            db.collection("rooms")
                    .whereEqualTo("hotel_id", hotelId)
                    .whereEqualTo("status", "AVAILABLE")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot firstRoomSnapshot = querySnapshot.getDocuments().get(0);
                            Room targetRoom = firstRoomSnapshot.toObject(Room.class);
                            if (targetRoom != null) {
                                targetRoom.setId(firstRoomSnapshot.getId());
                                // gửi dl phòng đầu tiên qua trang confirm
                                openBookingWithRoom(targetRoom);
                            }
                        } else {
                            Toast.makeText(this, "Khách sạn hiện tại không còn phòng trống", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi kiểm tra phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loadHotelFromFirestore() {
        db.collection("hotels")
                .document(hotelId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(this, "Không tìm thấy khách sạn trên Firebase", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    hotel = Hotel.fromDocument(document);
                    initHotelFlow();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không đọc được dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void initHotelFlow() {
        displayHotelDetails();
        loadLocation();
        loadRooms();
        loadReviews();
    }

    @SuppressWarnings("unchecked")
    private void displayHotelDetails() {
        address = hotel.getAddress();
        binding.tvHotelName.setText(valueOrDefault(hotel.getHotelName(), "Khách sạn"));
        binding.tvHotelAddress.setText(valueOrDefault(hotel.getAddress(), "Đang cập nhật địa chỉ"));
        binding.tvDescription.setText(valueOrDefault(hotel.getDescription(), "Đang cập nhật mô tả"));
        binding.tvPrice.setText(formatMoney(hotel.getPrice()) + " / đêm");
        binding.ratingBar.setRating((float) hotel.getRatingStar());
        binding.tvHotelRatingInfo.setText(formatHotelRatingInfo());

        // Hiển thị Amenities
        if (hotel.getAmenities() != null && !hotel.getAmenities().isEmpty()) {
            binding.tvAmenities.setText(String.join(", ", hotel.getAmenities()));
        } else {
            binding.tvAmenities.setText("Đang cập nhật tiện ích");
        }

        // Xử lý Image Slider lấy từ `image_url` và mảng ảnh phụ `image_urls`
        List<SlideModel> imageList = new ArrayList<>();
        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            imageList.add(new SlideModel(hotel.getImageUrl(), ScaleTypes.CENTER_CROP));
        }

        db.collection("hotels").document(hotelId).get().addOnSuccessListener(document -> {
            if (document.exists() && document.get("image_urls") != null) {
                List<String> secondaryImages = (List<String>) document.get("image_urls");
                if (secondaryImages != null) {
                    for (String imgUrl : secondaryImages) {
                        if (imgUrl != null && !imgUrl.isEmpty() && !imgUrl.equals(hotel.getImageUrl())) {
                            imageList.add(new SlideModel(imgUrl, ScaleTypes.CENTER_CROP));
                        }
                    }
                    if (!imageList.isEmpty()) {
                        binding.imageSlider.setImageList(imageList);
                    }
                }
            }
        });
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
                    latitude = firstDoubleValue(document, hotel.getLatitude(), "latitude");
                    longitude = firstDoubleValue(document, hotel.getLongitude(), "longitude");
                    address = firstStringValue(document, hotel.getAddress(), "address");
                    binding.tvHotelAddress.setText(valueOrDefault(address, hotel.getAddress()));
                })
                .addOnFailureListener(e -> useHotelLocationFallback());
    }

    private void loadRooms() {
        binding.layoutRooms.removeAllViews();

        // Tìm các phòng có trường hotel_id trùng khớp với ID khách sạn hiện tại
        db.collection("rooms")
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Nếu trống, hiển thị thông báo kèm mã ID khách sạn
                        binding.layoutRooms.addView(createText("Chưa có phòng nào liên kết với hotel_id: " + hotelId, false));
                        return;
                    }
                    for (DocumentSnapshot room : querySnapshot.getDocuments()) {
                        addRoomView(room);
                    }
                })
                .addOnFailureListener(e ->
                        binding.layoutRooms.addView(createText("Không tải được phòng: " + e.getMessage(), false)));
    }

    private void addRoomView(DocumentSnapshot roomSnapshot) {
        LinearLayout roomContainer = binding.layoutRooms;

        Room room = roomSnapshot.toObject(Room.class);
        if (room == null) return;
        room.setId(roomSnapshot.getId());

        LinearLayout box = createBox();

        String name = valueOrDefault(room.getRoomName(), "Phòng tiêu chuẩn");
        String type = valueOrDefault(room.getRoomType(), "");
        double price = room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice();
        String bed = valueOrDefault(room.getBedType(), "Giường tiêu chuẩn");
        double size = room.getRoomSize();
        int adults = room.getCapacityAdults();
        int children = room.getCapacityChildren();

        boolean canBook = "AVAILABLE".equalsIgnoreCase(room.getStatus()) && room.getAvailableRooms() > 0;

        box.addView(createText(name, true));
        box.addView(createText(type + (size > 0 ? " - " + size + " m2" : ""), false));
        box.addView(createText("Giường: " + bed, false));
        box.addView(createText("Sức chứa: " + adults + " Người lớn, " + children + " Trẻ em", false));
        box.addView(createText(formatMoney(price) + " / đêm", true));
        box.addView(createText("Trạng thái: " + (canBook ? "Còn phòng (" + room.getAvailableRooms() + ")" : "Hết phòng"), false));

        Button bookButton = new Button(this);
        bookButton.setText(canBook ? "Xem chi tiết / Đặt phòng" : "Hết phòng");
        bookButton.setEnabled(canBook);

        // gửi dl phòng được chọn qua trang confirm
        bookButton.setOnClickListener(v -> openBookingWithRoom(room));

        box.addView(bookButton);
        roomContainer.addView(box);
    }

    private void openBookingWithRoom(Room room) {
        if (hotel == null || room == null) {
            Toast.makeText(this, "Dữ liệu đang tải, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ConfirmActivity.class);

        String sectionId = (room.getSectionId() != null && !room.getSectionId().isEmpty()) ? room.getSectionId() : room.getId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sections").document(sectionId).get()
                .addOnSuccessListener(documentSnapshot -> {

                    double realPrice = 0;
                    boolean isRefundable = false;
                    boolean isReschedulable = false;
                    String roomStyleText = room.getRoomName();

                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("base_price") && documentSnapshot.get("base_price") != null) {
                            realPrice = ((Number) documentSnapshot.get("base_price")).doubleValue();
                        }

                        if (documentSnapshot.contains("is_refundable")) {
                            isRefundable = documentSnapshot.getBoolean("is_refundable");
                        }

                        if (documentSnapshot.contains("is_reschedulable")) {
                            isReschedulable = documentSnapshot.getBoolean("is_reschedulable");
                        }

                        if (documentSnapshot.contains("room_style") && documentSnapshot.getString("room_style") != null) {
                            roomStyleText = documentSnapshot.getString("room_style");
                        }
                    } else {
                        // Không tìm thấy Section: Dùng cơ chế bọc lót (Fallback) lấy giá từ bảng Room
                        realPrice = room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice();
                    }

                    intent.putExtra("EXTRA_HOTEL", hotel);

                    Section targetSection = new Section(
                            sectionId,
                            realPrice,
                            hotel.getId(),
                            isRefundable,
                            isReschedulable,
                            room.getCapacityAdults(),
                            roomStyleText
                    );

                    intent.putExtra("EXTRA_SECTION", targetSection);

                    // lấy số lượng phòng trống
                    intent.putExtra("EXTRA_AVAILABLE_ROOMS", room.getAvailableRooms());
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thông tin gói phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviews() {
        binding.layoutReviews.removeAllViews();
        db.collection("reviews")
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        binding.layoutReviews.addView(createText("Chưa có đánh giá nào cho khách sạn này", false));
                        return;
                    }
                    for (DocumentSnapshot review : querySnapshot.getDocuments()) {
                        addReviewView(review);
                    }
                })
                .addOnFailureListener(e ->
                        binding.layoutReviews.addView(createText("Không tải được đánh giá", false)));
    }

    private void addReviewView(DocumentSnapshot review) {
        LinearLayout box = createBox();
        String userName = stringValue(review, "user_name", stringValue(review, "reviewer_name", "Khách hàng"));
        double rating = doubleValue(review, "rating", doubleValue(review, "score", 0));
        String comment = stringValue(review, "comment", stringValue(review, "content", ""));

        box.addView(createText(userName + ": " + formatNumber(rating) + " sao", true));
        box.addView(createText(comment, false));
        binding.layoutReviews.addView(box);
    }

    private void openMap() {
        useHotelLocationFallback();
        Intent intent = new Intent(this, HotelMapActivity.class);
        intent.putExtra("hotel_name", hotel != null ? hotel.getHotelName() : "");
        intent.putExtra("address", address);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
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

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private void useHotelLocationFallback() {
        if (hotel == null) return;
        if (latitude == 0 && longitude == 0) {
            latitude = hotel.getLatitude();
            longitude = hotel.getLongitude();
        }
        address = valueOrDefault(address, hotel.getAddress());
    }

    private String formatHotelRatingInfo() {
        double reviewScore = hotel.getReviewScore();
        int reviewCount = hotel.getReviewCount();
        double ratingStar = hotel.getRatingStar();

        String scoreText = reviewScore > 0 ? formatNumber(reviewScore) + "/10" : "Chưa có điểm";
        String reviewText = reviewCount > 0 ? reviewCount + " đánh giá" : "Chưa có đánh giá";
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
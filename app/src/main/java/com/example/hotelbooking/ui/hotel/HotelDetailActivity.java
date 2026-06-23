package com.example.hotelbooking.ui.hotel;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.hotelbooking.data.model.DemoHotelData;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.model.Room;
import com.example.hotelbooking.data.model.Section;
import com.example.hotelbooking.databinding.ActivityHotelDetailBinding;
import com.example.hotelbooking.ui.map.HotelMapActivity;
import com.example.hotelbooking.ui.payment.ConfirmActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HotelDetailActivity extends AppCompatActivity {

    private ActivityHotelDetailBinding binding;
    private FirebaseFirestore db;
    private Hotel hotel;
    private String hotelId;
    private double latitude;
    private double longitude;
    private String address;
    private Room selectedRoom;
    private String selectedRoomId;
    private String selectedRoomName;
    private double selectedRoomPrice;
    private String selectedRoomImage;
    private int selectedAvailableRooms;
    private long checkInDateMillis;
    private long checkOutDateMillis;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
        binding.btnWriteReview.setOnClickListener(v -> checkReviewEligibility());
        setupBookingControls();

        // xử lý nút đặt ngay bằng cách tự động chọn phòng có trạng thái available đầu tiên
        binding.btnBookNow.setOnClickListener(v -> {
            if (proceedBooking()) {
                return;
            }
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
        addDemoRoomsIfAvailable();
        loadReviews();
    }

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
        loadImageSlider();
    }

    private void addImageUrl(List<String> imageUrls, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrls.contains(imageUrl)) {
            return;
        }
        imageUrls.add(imageUrl);
    }

    private void loadImageSlider() {
        binding.imageSlider.setVisibility(View.GONE);

        db.collection("hotels")
                .document(hotelId)
                .get()
                .addOnSuccessListener(document -> {
                    List<String> imageList = new ArrayList<>();

                    if (document.exists()) {
                        addImageUrlsFromDocument(imageList, document);
                    }

                    if (imageList.isEmpty() && hotel != null) {
                        addImageUrl(imageList, hotel.getImageUrl());
                    }

                    showImageSlider(imageList);
                })
                .addOnFailureListener(e -> {
                    List<String> imageList = new ArrayList<>();
                    if (hotel != null) {
                        addImageUrl(imageList, hotel.getImageUrl());
                    }
                    showImageSlider(imageList);
                });
    }

    private void addImageUrlsFromDocument(List<String> imageList, DocumentSnapshot document) {
        Object imageUrlsValue = document.get("image_urls");
        if (imageUrlsValue instanceof List<?>) {
            for (Object item : (List<?>) imageUrlsValue) {
                if (item != null) {
                    addImageUrl(imageList, String.valueOf(item));
                }
            }
        }

        if (imageList.isEmpty()) {
            addImageUrl(imageList, document.getString("imageUrl"));
            addImageUrl(imageList, document.getString("image_url"));
        }
    }

    private void showImageSlider(List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            binding.imageSlider.setVisibility(View.GONE);
            return;
        }

        List<SlideModel> slideModels = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            slideModels.add(new SlideModel(imageUrl, ScaleTypes.CENTER_CROP));
        }

        binding.imageSlider.setImageList(slideModels);
        binding.imageSlider.setVisibility(View.VISIBLE);
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
                    address = firstStringValue(document, hotel.getAddress(), "address", "address_text");
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
        String roomImage = firstStringValue(roomSnapshot, "", "image_url", "imageUrl");

        if (!roomImage.isEmpty()) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(140)
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(roomImage).into(imageView);
            box.addView(imageView);
        }

        box.addView(createText(name, true));
        box.addView(createText(type + (size > 0 ? " - " + size + " m2" : ""), false));
        box.addView(createText("Giường: " + bed, false));
        box.addView(createText("Sức chứa: " + adults + " Người lớn, " + children + " Trẻ em", false));
        box.addView(createText(formatMoney(price) + " / đêm", true));
        box.addView(createText("Trạng thái: " + (canBook ? "Còn phòng (" + room.getAvailableRooms() + ")" : "Hết phòng"), false));

        Button bookButton = new Button(this);
        bookButton.setText(canBook ? "Chọn" : "Hết phòng");
        bookButton.setEnabled(canBook);

        // gửi dl phòng được chọn qua trang confirm
        bookButton.setOnClickListener(v -> selectRoom(room, roomImage));
        if (canBook) {
            box.setOnClickListener(v -> selectRoom(room, roomImage));
        }

        box.addView(bookButton);
        roomContainer.addView(box);
    }

    private boolean addDemoRoomsIfAvailable() {
        if (DemoHotelData.findHotel(hotelId) == null) {
            return false;
        }

        for (DemoHotelData.DemoRoom demoRoom : DemoHotelData.rooms(hotelId)) {
            Room room = new Room();
            room.setId(demoRoom.id);
            room.setRoomName(demoRoom.name);
            room.setRoomType(demoRoom.type);
            room.setBedType(demoRoom.bedType);
            room.setPricePerNight(demoRoom.price);
            room.setAvailableRooms(demoRoom.availableRooms);
            room.setStatus(demoRoom.status);
            room.setRoomSize(demoRoom.size);
            room.setCapacityAdults(adultsFromCapacity(demoRoom.capacity));
            room.setCapacityChildren(childrenFromCapacity(demoRoom.capacity));
            addDemoRoomView(room, demoRoom.capacity, demoRoom.imageUrl);
        }
        return true;
    }

    private void addDemoRoomView(Room room, String capacity, String roomImage) {
        LinearLayout box = createBox();
        boolean canBook = "AVAILABLE".equalsIgnoreCase(room.getStatus()) && room.getAvailableRooms() > 0;

        if (roomImage != null && !roomImage.isEmpty()) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(140)
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(roomImage).into(imageView);
            box.addView(imageView);
        }

        box.addView(createText(valueOrDefault(room.getRoomName(), "Phong"), true));
        box.addView(createText(valueOrDefault(room.getRoomType(), ""), false));
        box.addView(createText("Giuong: " + valueOrDefault(room.getBedType(), "Tieu chuan"), false));
        box.addView(createText("Suc chua: " + valueOrDefault(capacity, "Dang cap nhat"), false));
        box.addView(createText(formatMoney(room.getPricePerNight()) + " / dem", true));
        box.addView(createText("Trang thai: " + (canBook ? "Con phong (" + room.getAvailableRooms() + ")" : "Het phong"), false));

        Button selectButton = new Button(this);
        selectButton.setText(canBook ? "Chọn" : "Hết phòng");
        selectButton.setEnabled(canBook);
        selectButton.setOnClickListener(v -> selectRoom(room, roomImage));
        if (canBook) {
            box.setOnClickListener(v -> selectRoom(room, roomImage));
        }

        box.addView(selectButton);
        binding.layoutRooms.addView(box);
    }

    private int adultsFromCapacity(String capacity) {
        if (capacity == null || capacity.isEmpty()) {
            return 2;
        }
        try {
            return Integer.parseInt(capacity.substring(0, 1));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    private int childrenFromCapacity(String capacity) {
        if (capacity == null || !capacity.contains("tre em")) {
            return 0;
        }
        String[] parts = capacity.split(",");
        if (parts.length < 2) {
            return 0;
        }
        try {
            return Integer.parseInt(parts[1].trim().substring(0, 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setupBookingControls() {
        binding.btnCheckInDate.setOnClickListener(v -> showDatePicker(true));
        binding.btnCheckOutDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar calendar = Calendar.getInstance();
        long currentValue = isCheckIn ? checkInDateMillis : checkOutDateMillis;
        if (currentValue > 0) {
            calendar.setTimeInMillis(currentValue);
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);
                    long selectedMillis = selectedDate.getTimeInMillis();

                    if (isCheckIn) {
                        if (selectedMillis < todayStartMillis()) {
                            Toast.makeText(this, "Ngay den khong duoc nho hon hom nay", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkInDateMillis = selectedMillis;
                        binding.btnCheckInDate.setText(dateFormat.format(new Date(checkInDateMillis)));

                        if (checkOutDateMillis > 0 && checkOutDateMillis <= checkInDateMillis) {
                            checkOutDateMillis = 0;
                            binding.btnCheckOutDate.setText("Bam chon ngay di");
                        }
                        showDatePicker(false);
                    } else {
                        if (checkInDateMillis <= 0) {
                            Toast.makeText(this, "Hay chon ngay den truoc", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (selectedMillis <= checkInDateMillis) {
                            Toast.makeText(this, "Ngay di phai sau ngay den", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkOutDateMillis = selectedMillis;
                        binding.btnCheckOutDate.setText(dateFormat.format(new Date(checkOutDateMillis)));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(todayStartMillis());
        dialog.show();
    }

    private long todayStartMillis() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTimeInMillis();
    }

    private void selectRoom(Room room, String roomImage) {
        if (room == null || room.getAvailableRooms() <= 0 || !"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            Toast.makeText(this, "Phong nay hien khong con trong", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedRoom = room;
        selectedRoomId = room.getId();
        selectedRoomName = valueOrDefault(room.getRoomName(), "Phong");
        selectedRoomPrice = room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice();
        selectedRoomImage = roomImage;
        selectedAvailableRooms = room.getAvailableRooms();

        binding.tvSelectedRoom.setText("Da chon: " + selectedRoomName + " - " + formatMoney(selectedRoomPrice));
        binding.tvPrice.setText(formatMoney(selectedRoomPrice) + " / dem");
        Toast.makeText(this, "Da chon phong", Toast.LENGTH_SHORT).show();
    }

    private boolean proceedBooking() {
        if (selectedRoom == null || selectedRoomId == null || selectedRoomId.isEmpty()) {
            Toast.makeText(this, "Vui long chon hang phong", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (checkInDateMillis <= 0) {
            Toast.makeText(this, "Vui long chon ngay den", Toast.LENGTH_SHORT).show();
            showDatePicker(true);
            return true;
        }
        if (checkOutDateMillis <= 0) {
            Toast.makeText(this, "Vui long chon ngay di", Toast.LENGTH_SHORT).show();
            showDatePicker(false);
            return true;
        }
        if (checkInDateMillis < todayStartMillis()) {
            Toast.makeText(this, "Ngay den khong duoc nho hon hom nay", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (checkOutDateMillis <= checkInDateMillis) {
            Toast.makeText(this, "Ngay di phai sau ngay den", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (selectedAvailableRooms <= 0) {
            Toast.makeText(this, "Phong da chon khong con trong", Toast.LENGTH_SHORT).show();
            return true;
        }
        int capacity = selectedRoom.getCapacityAdults() + selectedRoom.getCapacityChildren();
        if (capacity > 0 && getGuestCount() > capacity) {
            Toast.makeText(this, "So khach vuot qua suc chua phong", Toast.LENGTH_SHORT).show();
            return true;
        }

        openBookingWithRoom(selectedRoom);
        return true;
    }

    private int getGuestCount() {
        EditText guestCountInput = binding.edtGuestCount;
        try {
            int guestCount = Integer.parseInt(guestCountInput.getText().toString().trim());
            return Math.max(guestCount, 1);
        } catch (NumberFormatException e) {
            return 1;
        }
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
                    if (realPrice <= 0) {
                        realPrice = room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice();
                    }

                    intent.putExtra("hotel_id", hotelId);
                    intent.putExtra("room_id", selectedRoomId);
                    intent.putExtra("owner_id", hotel.getOwnerId());
                    intent.putExtra("hotel_name", hotel.getHotelName());
                    intent.putExtra("room_name", selectedRoomName);
                    intent.putExtra("hotel_image", hotel.getImageUrl());
                    intent.putExtra("room_image", selectedRoomImage);
                    intent.putExtra("address", address);
                    intent.putExtra("price", selectedRoomPrice > 0 ? selectedRoomPrice : realPrice);
                    intent.putExtra("check_in", checkInDateMillis);
                    intent.putExtra("check_out", checkOutDateMillis);
                    intent.putExtra("guest_count", getGuestCount());

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
                    intent.putExtra("EXTRA_CHECK_IN", checkInDateMillis);
                    intent.putExtra("EXTRA_CHECK_OUT", checkOutDateMillis);
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
        String reviewDate = reviewDateText(review);

        box.addView(createText(userName + ": " + formatNumber(rating) + " sao", true));
        box.addView(createText(comment, false));
        if (!reviewDate.isEmpty()) {
            box.addView(createText("Ngay danh gia: " + reviewDate, false));
        }
        binding.layoutReviews.addView(box);
    }

    private String reviewDateText(DocumentSnapshot review) {
        Object createdAt = review.get("created_at");
        if (createdAt instanceof Timestamp) {
            return dateFormat.format(((Timestamp) createdAt).toDate());
        }
        if (createdAt instanceof Number) {
            return dateFormat.format(new Date(((Number) createdAt).longValue()));
        }
        Object createdAtMillis = review.get("created_at_millis");
        if (createdAtMillis instanceof Number) {
            return dateFormat.format(new Date(((Number) createdAtMillis).longValue()));
        }
        return "";
    }

    private void checkReviewEligibility() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui long dang nhap de danh gia", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("reservations")
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    DocumentSnapshot completedReservation = null;
                    for (DocumentSnapshot reservation : querySnapshot.getDocuments()) {
                        if (isCompletedReservation(reservation)
                                && isReservationOfCurrentUser(reservation, currentUser.getUid())) {
                            completedReservation = reservation;
                            break;
                        }
                    }

                    if (completedReservation == null) {
                        Toast.makeText(this, "Ban chi co the danh gia sau khi don da hoan thanh", Toast.LENGTH_LONG).show();
                        return;
                    }

                    checkDuplicateReviewAndOpenDialog(completedReservation, currentUser);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong kiem tra duoc don dat phong: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean isCompletedReservation(DocumentSnapshot reservation) {
        String status = reservation.getString("status");
        return "completed".equalsIgnoreCase(status);
    }

    private boolean isReservationOfCurrentUser(DocumentSnapshot reservation, String uid) {
        String userId = reservation.getString("user_id");
        String customerId = reservation.getString("customer_id");
        return uid.equals(userId) || uid.equals(customerId);
    }

    private void checkDuplicateReviewAndOpenDialog(DocumentSnapshot reservation, FirebaseUser currentUser) {
        String reservationId = reservation.getId();
        db.collection("reviews")
                .whereEqualTo("reservation_id", reservationId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Don nay da duoc danh gia", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showReviewDialog(reservationId, currentUser);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong kiem tra duoc danh gia cu: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showReviewDialog(String reservationId, FirebaseUser currentUser) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setRating(5);

        EditText commentInput = new EditText(this);
        commentInput.setHint("Nhap noi dung danh gia");
        commentInput.setMinLines(3);
        commentInput.setBackgroundResource(R.drawable.bg_input);
        commentInput.setPadding(dp(12), dp(8), dp(12), dp(8));

        form.addView(ratingBar);
        form.addView(commentInput);

        new AlertDialog.Builder(this)
                .setTitle("Gui danh gia")
                .setView(form)
                .setNegativeButton("Huy", null)
                .setPositiveButton("Gui", (dialog, which) ->
                        submitReview(reservationId, currentUser, ratingBar.getRating(), commentInput.getText().toString().trim()))
                .show();
    }

    private void submitReview(String reservationId, FirebaseUser currentUser, float rating, String comment) {
        if (rating <= 0) {
            Toast.makeText(this, "Vui long chon so sao", Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui long nhap noi dung danh gia", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("hotel_id", hotelId);
        reviewData.put("reservation_id", reservationId);
        reviewData.put("user_id", currentUser.getUid());
        reviewData.put("user_name", valueOrDefault(currentUser.getDisplayName(), "Khach hang"));
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);
        reviewData.put("created_at", FieldValue.serverTimestamp());
        reviewData.put("created_at_millis", System.currentTimeMillis());

        db.collection("reviews")
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Da gui danh gia", Toast.LENGTH_SHORT).show();
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong gui duoc danh gia: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void openMap() {
        useHotelLocationFallback();
        if ((latitude == 0 && longitude == 0)
                && hotel != null
                && hotel.getLocationId() != null
                && !hotel.getLocationId().isEmpty()) {
            db.collection("locations")
                    .document(hotel.getLocationId())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            latitude = firstDoubleValue(document, latitude, "latitude");
                            longitude = firstDoubleValue(document, longitude, "longitude");
                            address = firstStringValue(document, address, "address", "address_text");
                        }
                        openMapWithCurrentLocation();
                    })
                    .addOnFailureListener(e -> openMapWithCurrentLocation());
            return;
        }
        openMapWithCurrentLocation();
    }

    private void openMapWithCurrentLocation() {
        useAddressCoordinateFallback();
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "Khach san chua co toa do ban do", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, HotelMapActivity.class);
        intent.putExtra("hotel_name", hotel != null ? hotel.getHotelName() : "");
        intent.putExtra("address", address);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    private void useAddressCoordinateFallback() {
        if (latitude != 0 || longitude != 0) {
            return;
        }

        String locationText = valueOrDefault(address, "");
        if (locationText.isEmpty() && hotel != null) {
            locationText = valueOrDefault(hotel.getAddress(), "");
        }
        String normalized = locationText.toLowerCase(Locale.ROOT);

        if (normalized.contains("da lat") || normalized.contains("dalat")) {
            latitude = 11.9404;
            longitude = 108.4583;
        } else if (normalized.contains("vung tau")) {
            latitude = 10.4114;
            longitude = 107.1362;
        } else if (normalized.contains("da nang")) {
            latitude = 16.0678;
            longitude = 108.2453;
        } else if (normalized.contains("hoi an")) {
            latitude = 15.8801;
            longitude = 108.3380;
        } else if (normalized.contains("ha noi") || normalized.contains("hanoi")) {
            latitude = 21.0285;
            longitude = 105.8542;
        } else if (normalized.contains("nha trang")) {
            latitude = 12.2388;
            longitude = 109.1967;
        } else if (normalized.contains("tp.hcm")
                || normalized.contains("ho chi minh")
                || normalized.contains("hồ chí minh")
                || normalized.contains("sai gon")
                || normalized.contains("saigon")) {
            latitude = 10.762622;
            longitude = 106.660172;
        }
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
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

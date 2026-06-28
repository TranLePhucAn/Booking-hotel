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
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.model.Room;
import com.example.hotelbooking.data.model.Section;
import com.example.hotelbooking.databinding.ActivityHotelDetailBinding;
import com.example.hotelbooking.ui.map.HotelMapActivity;
import com.example.hotelbooking.ui.payment.ConfirmActivity;
import com.example.hotelbooking.utils.AppConstants;
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
    private Room lowestAvailableRoom;
    private String lowestAvailableRoomImage;
    private double lowestAvailableRoomPrice = Double.MAX_VALUE;
    private long checkInDateMillis;
    private long checkOutDateMillis;
    private boolean isOpeningConfirm;
    private boolean isAdminPreviewMode;
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
        isAdminPreviewMode = "admin_preview".equalsIgnoreCase(getIntent().getStringExtra("mode"));

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

        if (isAdminPreviewMode) {
            applyAdminPreviewMode();
        } else {
            binding.btnBookNow.setOnClickListener(v -> autoBookLowestAvailableRoom());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOpeningConfirm = false;
    }

    private void loadHotelFromFirestore() {
        db.collection(AppConstants.COLLECTION_HOTELS)
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

    private void applyAdminPreviewMode() {
        binding.bottomBookingBar.setVisibility(View.GONE);
        binding.btnWriteReview.setVisibility(View.GONE);
        binding.btnCheckInDate.setEnabled(false);
        binding.btnCheckOutDate.setEnabled(false);
        binding.tvSelectedRoom.setVisibility(View.GONE);
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

        db.collection(AppConstants.COLLECTION_HOTELS)
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
        useHotelLocationFallback();
        binding.tvHotelAddress.setText(valueOrDefault(address, hotel.getAddress()));
    }

    private void loadRooms() {
        binding.layoutRooms.removeAllViews();
        lowestAvailableRoom = null;
        lowestAvailableRoomImage = "";
        lowestAvailableRoomPrice = Double.MAX_VALUE;

        // Tìm các phòng có trường hotel_id trùng khớp với ID khách sạn hiện tại
        db.collection(AppConstants.COLLECTION_ROOMS)
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Chỉ cho đặt phòng bằng dữ liệu phòng thật trên Firebase.
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
        rememberLowestAvailableRoom(room, roomImage, price, canBook);

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
        bookButton.setText(isAdminPreviewMode ? "Xem trước" : (canBook ? "Xem chi tiết" : "Hết phòng"));
        bookButton.setEnabled(canBook && !isAdminPreviewMode);

        // gửi dl phòng được chọn qua trang confirm
        bookButton.setOnClickListener(v -> openRoomDetail(room, roomImage));
        if (canBook && !isAdminPreviewMode) {
            box.setOnClickListener(v -> openRoomDetail(room, roomImage));
        }

        box.addView(bookButton);
        roomContainer.addView(box);
    }

    private void setupBookingControls() {
        binding.btnCheckInDate.setOnClickListener(v -> showDatePicker(true));
        binding.btnCheckOutDate.setOnClickListener(v -> {
            if (checkInDateMillis <= 0) {
                Toast.makeText(this, "Vui lòng chọn ngày đến trước", Toast.LENGTH_SHORT).show();
                showDatePicker(true);
                return;
            }
            showDatePicker(false);
        });
    }

    private void showDatePicker(boolean isCheckIn) {
        if (!isCheckIn && checkInDateMillis <= 0) {
            Toast.makeText(this, "Vui lòng chọn ngày đến trước", Toast.LENGTH_SHORT).show();
            showDatePicker(true);
            return;
        }

        Calendar calendar = Calendar.getInstance();
        long currentValue = isCheckIn ? checkInDateMillis : checkOutDateMillis;
        if (currentValue > 0) {
            calendar.setTimeInMillis(currentValue);
        } else if (!isCheckIn) {
            calendar.setTimeInMillis(checkInDateMillis);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth, isCheckIn ? 13 : 12, 0, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);
                    long selectedMillis = selectedDate.getTimeInMillis();

                    if (isCheckIn) {
                        if (selectedMillis < todayStartMillis()) {
                            Toast.makeText(this, "Ngày đến không được nhỏ hơn hôm nay", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkInDateMillis = selectedMillis;
                        binding.btnCheckInDate.setText(dateFormat.format(new Date(checkInDateMillis)));

                        if (checkOutDateMillis > 0 && checkOutDateMillis <= checkInDateMillis) {
                            checkOutDateMillis = 0;
                            binding.btnCheckOutDate.setText("Bấm chọn ngày đi");
                        }
                        Toast.makeText(this, "Vui lòng chọn ngày bạn muốn trả phòng", Toast.LENGTH_SHORT).show();
                        binding.getRoot().postDelayed(() -> showDatePicker(false), 250);
                    } else {
                        if (checkInDateMillis <= 0) {
                            Toast.makeText(this, "Hãy chọn ngày đến trước", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (selectedMillis <= checkInDateMillis) {
                            Toast.makeText(this, "Ngày đi phải sau ngày đến", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkOutDateMillis = selectedMillis;
                        binding.btnCheckOutDate.setText(dateFormat.format(new Date(checkOutDateMillis)));
                        Toast.makeText(this, "Đang chuyển sang trang xác nhận đặt phòng", Toast.LENGTH_SHORT).show();
                        binding.getRoot().postDelayed(() -> continueToConfirmAfterDateSelection(), 250);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.setTitle(isCheckIn ? "Chọn ngày đến" : "Chọn ngày đi");
        dialog.setMessage(isCheckIn
                ? "Vui lòng chọn ngày bạn muốn đến nhận phòng."
                : "Vui lòng chọn ngày bạn muốn trả phòng.");
        dialog.getDatePicker().setMinDate(isCheckIn ? todayStartMillis() : checkoutMinDateMillis());
        dialog.show();
    }

    private long checkoutMinDateMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(checkInDateMillis);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
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
            Toast.makeText(this, "Phòng nay hien khong con trong", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedRoom = room;
        selectedRoomId = room.getId();
        selectedRoomName = valueOrDefault(room.getRoomName(), "Phòng");
        selectedRoomPrice = room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice();
        selectedRoomImage = roomImage;
        selectedAvailableRooms = room.getAvailableRooms();

        binding.tvSelectedRoom.setText("Đã chọn: " + selectedRoomName + " - " + formatMoney(selectedRoomPrice));
        binding.tvPrice.setText(formatMoney(selectedRoomPrice) + " / đêm");
        binding.btnBookNow.setText("Chọn ngày");
        Toast.makeText(this, "Đã chọn phòng", Toast.LENGTH_SHORT).show();
    }

    private void openRoomDetail(Room room, String roomImage) {
        if (isAdminPreviewMode) {
            Toast.makeText(this, "Chế độ preview không cho đặt phòng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (room != null && room.getId() != null && room.getId().startsWith("demo_")) {
            Toast.makeText(this, "Phòng mẫu không thể đặt. Vui lòng chọn phòng từ Firebase.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (room == null || room.getAvailableRooms() <= 0 || !"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            Toast.makeText(this, "Phòng này hiện không còn trống", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, RoomDetailActivity.class);
        intent.putExtra("EXTRA_HOTEL", hotel);
        intent.putExtra("EXTRA_ROOM", room);
        intent.putExtra("hotel_id", hotelId);
        intent.putExtra("room_id", room.getId());
        intent.putExtra("room_image", valueOrDefault(roomImage, room.getImageUrl()));
        intent.putExtra("address", address);
        intent.putExtra("EXTRA_CHECK_IN", checkInDateMillis);
        intent.putExtra("EXTRA_CHECK_OUT", checkOutDateMillis);
        startActivity(intent);
    }

    private void rememberLowestAvailableRoom(Room room, String roomImage, double price, boolean canBook) {
        if (!canBook || room == null) {
            return;
        }
        double roomPrice = price > 0 ? price : hotel.getPrice();
        if (lowestAvailableRoom == null || roomPrice < lowestAvailableRoomPrice) {
            lowestAvailableRoom = room;
            lowestAvailableRoomImage = roomImage;
            lowestAvailableRoomPrice = roomPrice;
            binding.tvPrice.setText(formatMoney(roomPrice) + " / đêm");
        }
    }

    private boolean proceedBooking() {
        if (selectedRoom == null || selectedRoomId == null || selectedRoomId.isEmpty()) {
            autoBookLowestAvailableRoom();
            return true;
        }
        ensureDefaultBookingDates();
        if (checkInDateMillis <= 0) {
            Toast.makeText(this, "Vui lòng chọn ngày đến", Toast.LENGTH_SHORT).show();
            showDatePicker(true);
            return true;
        }
        if (checkOutDateMillis <= 0) {
            Toast.makeText(this, "Vui lòng chọn ngày đi", Toast.LENGTH_SHORT).show();
            showDatePicker(false);
            return true;
        }
        if (checkInDateMillis < todayStartMillis()) {
            Toast.makeText(this, "Ngày đến không được nhỏ hơn hôm nay", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (checkOutDateMillis <= checkInDateMillis) {
            Toast.makeText(this, "Ngày đi phải sau ngày đến", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (selectedAvailableRooms <= 0) {
            Toast.makeText(this, "Phòng đã chọn không còn trống", Toast.LENGTH_SHORT).show();
            return true;
        }
        int capacity = selectedRoom.getCapacityAdults() + selectedRoom.getCapacityChildren();
        if (capacity > 0 && getGuestCount() > capacity) {
            Toast.makeText(this, "Số khách vượt quá sức chứa phòng", Toast.LENGTH_SHORT).show();
            return true;
        }

        openRoomDetail(selectedRoom, selectedRoomImage);
        return true;
    }

    private void autoBookLowestAvailableRoom() {
        if (lowestAvailableRoom != null) {
            openRoomDetail(lowestAvailableRoom, lowestAvailableRoomImage);
            return;
        }

        db.collection(AppConstants.COLLECTION_ROOMS)
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Room cheapestRoom = null;
                    String cheapestRoomImage = "";
                    double cheapestPrice = Double.MAX_VALUE;

                    for (DocumentSnapshot roomSnapshot : querySnapshot.getDocuments()) {
                        Room room = roomSnapshot.toObject(Room.class);
                        if (!isRoomAvailable(room)) {
                            continue;
                        }
                        room.setId(roomSnapshot.getId());
                        double price = room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice();
                        if (price < cheapestPrice) {
                            cheapestPrice = price;
                            cheapestRoom = room;
                            cheapestRoomImage = firstStringValue(roomSnapshot, "", "image_url", "imageUrl");
                        }
                    }

                    if (cheapestRoom == null) {
                        Toast.makeText(this, "Khách sạn hien tai khong con phòng trong", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    openRoomDetail(cheapestRoom, cheapestRoomImage);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tìm được phòng giá thấp nhất: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean isRoomAvailable(Room room) {
        return room != null
                && room.getAvailableRooms() > 0
                && "AVAILABLE".equalsIgnoreCase(valueOrDefault(room.getStatus(), ""));
    }

    private void ensureDefaultBookingDates() {
        if (checkInDateMillis <= 0) {
            Calendar checkIn = Calendar.getInstance();
            checkIn.set(Calendar.HOUR_OF_DAY, 13);
            checkIn.set(Calendar.MINUTE, 0);
            checkIn.set(Calendar.SECOND, 0);
            checkIn.set(Calendar.MILLISECOND, 0);
            checkInDateMillis = checkIn.getTimeInMillis();
            binding.btnCheckInDate.setText(dateFormat.format(new Date(checkInDateMillis)));
        }

        if (checkOutDateMillis <= checkInDateMillis) {
            Calendar checkOut = Calendar.getInstance();
            checkOut.setTimeInMillis(checkInDateMillis);
            checkOut.add(Calendar.DAY_OF_MONTH, 1);
            checkOut.set(Calendar.HOUR_OF_DAY, 12);
            checkOut.set(Calendar.MINUTE, 0);
            checkOut.set(Calendar.SECOND, 0);
            checkOut.set(Calendar.MILLISECOND, 0);
            checkOutDateMillis = checkOut.getTimeInMillis();
            binding.btnCheckOutDate.setText(dateFormat.format(new Date(checkOutDateMillis)));
        }
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

    private void continueToConfirmAfterDateSelection() {
        if (isOpeningConfirm) {
            return;
        }
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để thực hiện đặt phòng.")
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    Intent intent = new Intent(this, com.example.hotelbooking.ui.auth.LoginActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
            return;
        }
        if (selectedRoom == null) {
            Toast.makeText(this, "Vui lòng chọn hạng phòng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkInDateMillis <= 0 || checkOutDateMillis <= 0) {
            Toast.makeText(this, "Vui lòng chọn đủ ngày đến và ngày đi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkOutDateMillis <= checkInDateMillis) {
            Toast.makeText(this, "Ngày đi phải sau ngày đến", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAvailableRooms <= 0) {
            Toast.makeText(this, "Phòng đã chọn không còn trống", Toast.LENGTH_SHORT).show();
            return;
        }
        openRoomDetail(selectedRoom, selectedRoomImage);
    }

    private void openBookingWithRoom(Room room) {
        if (isOpeningConfirm) {
            return;
        }
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để thực hiện đặt phòng.")
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    Intent intent = new Intent(this, com.example.hotelbooking.ui.auth.LoginActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
            return;
        }
        if (hotel == null || room == null) {
            Toast.makeText(this, "Dữ liệu đang tải, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        isOpeningConfirm = true;
        Intent intent = new Intent(this, ConfirmActivity.class);

        String sectionId = room.getId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.COLLECTION_ROOMS).document(room.getId()).get()
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
                    intent.putExtra("room_quantity", 1);

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
                    intent.putExtra("EXTRA_ROOM_QUANTITY", 1);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thông tin gói phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviews() {
        binding.layoutReviews.removeAllViews();
        db.collection(AppConstants.COLLECTION_REVIEWS)
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
            box.addView(createText("Ngày đánh giá: " + reviewDate, false));
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
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(AppConstants.COLLECTION_RESERVATIONS)
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
                        Toast.makeText(this, "Bạn chỉ có thể đánh giá sau khi đơn đã hoàn thành", Toast.LENGTH_LONG).show();
                        return;
                    }

                    checkDuplicateReviewAndOpenDialog(completedReservation, currentUser);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không kiểm tra được đơn đặt phòng: " + e.getMessage(), Toast.LENGTH_LONG).show());
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
        db.collection(AppConstants.COLLECTION_REVIEWS)
                .whereEqualTo("reservation_id", reservationId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Đơn này đã được đánh giá", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showReviewDialog(reservationId, currentUser);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không kiểm tra được đánh giá cũ: " + e.getMessage(), Toast.LENGTH_LONG).show());
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
        commentInput.setHint("Nhập nội dung đánh giá");
        commentInput.setMinLines(3);
        commentInput.setBackgroundResource(R.drawable.bg_input);
        commentInput.setPadding(dp(12), dp(8), dp(12), dp(8));

        form.addView(ratingBar);
        form.addView(commentInput);

        new AlertDialog.Builder(this)
                .setTitle("Gửi đánh giá")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi", (dialog, which) ->
                        submitReview(reservationId, currentUser, ratingBar.getRating(), commentInput.getText().toString().trim()))
                .show();
    }

    private void submitReview(String reservationId, FirebaseUser currentUser, float rating, String comment) {
        if (rating <= 0) {
            Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("hotel_id", hotelId);
        reviewData.put("reservation_id", reservationId);
        reviewData.put("user_id", currentUser.getUid());
        reviewData.put("user_name", valueOrDefault(currentUser.getDisplayName(), "Khách hàng"));
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);
        reviewData.put("created_at", FieldValue.serverTimestamp());
        reviewData.put("created_at_millis", System.currentTimeMillis());

        db.collection(AppConstants.COLLECTION_REVIEWS)
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không gửi được đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void openMap() {
        useHotelLocationFallback();
        openMapWithCurrentLocation();
    }

    private void openMapWithCurrentLocation() {
        useAddressCoordinateFallback();
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "Khách sạn chưa có tọa độ bản đồ", Toast.LENGTH_SHORT).show();
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

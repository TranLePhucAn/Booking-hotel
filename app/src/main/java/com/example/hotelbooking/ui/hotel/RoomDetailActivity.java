package com.example.hotelbooking.ui.hotel;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.model.Room;
import com.example.hotelbooking.data.model.Section;
import com.example.hotelbooking.ui.payment.ConfirmActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RoomDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Hotel hotel;
    private Room room;
    private String hotelId;
    private String roomId;
    private String roomImage;
    private String address;
    private long checkInDateMillis;
    private long checkOutDateMillis;
    private Section selectedSection;
    private int selectedAvailableRooms;
    private boolean adjustingRoomQuantity;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private TextView tvRoomName;
    private TextView tvRoomType;
    private TextView tvRoomDescription;
    private TextView tvRoomInfo;
    private TextView tvSelectedSection;
    private TextView btnCheckInDate;
    private TextView btnCheckOutDate;
    private EditText edtGuestCount;
    private EditText edtRoomQuantity;
    private LinearLayout layoutSections;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        db = FirebaseFirestore.getInstance();
        receiveIntentData();
        if (isDemoRoom()) {
            Toast.makeText(this, "Phòng mẫu không thể đặt. Vui lòng chọn phòng từ Firebase.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        initViews();
        bindRoomData();
        setupDatePickers();
        loadSections();
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        hotel = (Hotel) intent.getSerializableExtra("EXTRA_HOTEL");
        room = (Room) intent.getSerializableExtra("EXTRA_ROOM");
        hotelId = intent.getStringExtra("hotel_id");
        roomId = intent.getStringExtra("room_id");
        roomImage = intent.getStringExtra("room_image");
        address = intent.getStringExtra("address");
        checkInDateMillis = intent.getLongExtra("EXTRA_CHECK_IN", 0);
        checkOutDateMillis = intent.getLongExtra("EXTRA_CHECK_OUT", 0);

        if (room != null && (roomId == null || roomId.isEmpty())) {
            roomId = room.getId();
        }
        if (hotel != null && (hotelId == null || hotelId.isEmpty())) {
            hotelId = hotel.getId();
        }
    }

    private boolean isDemoRoom() {
        return roomId != null && roomId.startsWith("demo_");
    }

    private void initViews() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarRoomDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        tvRoomName = findViewById(R.id.tvRoomDetailName);
        tvRoomType = findViewById(R.id.tvRoomDetailType);
        tvRoomDescription = findViewById(R.id.tvRoomDetailDescription);
        tvRoomInfo = findViewById(R.id.tvRoomDetailInfo);
        tvSelectedSection = findViewById(R.id.tvSelectedSection);
        btnCheckInDate = findViewById(R.id.btnRoomCheckInDate);
        btnCheckOutDate = findViewById(R.id.btnRoomCheckOutDate);
        edtGuestCount = findViewById(R.id.edtRoomGuestCount);
        edtRoomQuantity = findViewById(R.id.edtRoomQuantity);
        layoutSections = findViewById(R.id.layoutSections);
        btnContinue = findViewById(R.id.btnContinueConfirm);

        int initialRoomQuantity = getIntent().getIntExtra("EXTRA_ROOM_QUANTITY", 1);
        if (edtRoomQuantity != null) {
            edtRoomQuantity.setText(String.valueOf(Math.max(1, initialRoomQuantity)));
        }
        if (checkInDateMillis > 0) {
            btnCheckInDate.setText(dateFormat.format(new Date(checkInDateMillis)));
        }
        if (checkOutDateMillis > 0) {
            btnCheckOutDate.setText(dateFormat.format(new Date(checkOutDateMillis)));
        }

        setupRoomQuantityValidation();
        btnContinue.setOnClickListener(v -> continueToConfirm());
    }

    private void setupRoomQuantityValidation() {
        if (edtRoomQuantity == null) {
            return;
        }
        edtRoomQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (adjustingRoomQuantity || selectedAvailableRooms <= 0 || s == null || s.toString().trim().isEmpty()) {
                    return;
                }
                int quantity = getRoomQuantity();
                if (quantity > selectedAvailableRooms) {
                    adjustingRoomQuantity = true;
                    edtRoomQuantity.setText(String.valueOf(selectedAvailableRooms));
                    edtRoomQuantity.setSelection(edtRoomQuantity.getText().length());
                    adjustingRoomQuantity = false;
                    Toast.makeText(RoomDetailActivity.this, "Số phòng tối đa là " + selectedAvailableRooms, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bindRoomData() {
        if (hotel == null || room == null || roomId == null || roomId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin phòng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ImageView imageView = findViewById(R.id.imgRoomDetail);
        String image = valueOrDefault(roomImage, room.getImageUrl());
        if (!image.isEmpty()) {
            Glide.with(this).load(image).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }

        tvRoomName.setText(valueOrDefault(room.getRoomName(), "Phòng"));
        tvRoomType.setText(valueOrDefault(room.getRoomType(), "Hạng phòng tiêu chuẩn"));
        tvRoomDescription.setText(valueOrDefault(room.getDescription(), "Phòng đang cập nhật mô tả chi tiết."));

        String bed = valueOrDefault(room.getBedType(), "Tieu chuan");
        int adults = room.getCapacityAdults();
        int children = room.getCapacityChildren();
        String sizeText = room.getRoomSize() > 0 ? formatNumber(room.getRoomSize()) + " m2 - " : "";
        tvRoomInfo.setText(sizeText + "Giường: " + bed + " - Sức chứa: " + adults + " người lớn, " + children + " trẻ em");
    }

    private void setupDatePickers() {
        btnCheckInDate.setOnClickListener(v -> showDatePicker(true));
        btnCheckOutDate.setOnClickListener(v -> {
            if (checkInDateMillis <= 0) {
                Toast.makeText(this, "Vui lòng chọn ngày đến trước", Toast.LENGTH_SHORT).show();
                showDatePicker(true);
                return;
            }
            showDatePicker(false);
        });
    }

    private void showDatePicker(boolean isCheckIn) {
        Calendar calendar = Calendar.getInstance();
        long currentValue = isCheckIn ? checkInDateMillis : checkOutDateMillis;
        if (currentValue > 0) {
            calendar.setTimeInMillis(currentValue);
        } else if (!isCheckIn && checkInDateMillis > 0) {
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
                        btnCheckInDate.setText(dateFormat.format(new Date(checkInDateMillis)));
                        if (checkOutDateMillis > 0 && checkOutDateMillis <= checkInDateMillis) {
                            checkOutDateMillis = 0;
                            btnCheckOutDate.setText("Bấm chọn ngày đi");
                        }
                        return;
                    }

                    if (checkInDateMillis <= 0) {
                        Toast.makeText(this, "Hãy chọn ngày đến trước", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedMillis <= checkInDateMillis) {
                        Toast.makeText(this, "Ngày đi phải sau ngày đến", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    checkOutDateMillis = selectedMillis;
                    btnCheckOutDate.setText(dateFormat.format(new Date(checkOutDateMillis)));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.setTitle(isCheckIn ? "Chọn ngày đến" : "Chọn ngày đi");
        dialog.getDatePicker().setMinDate(isCheckIn ? todayStartMillis() : checkoutMinDateMillis());
        dialog.show();
    }

    private void loadSections() {
        layoutSections.removeAllViews();
        selectedSection = null;
        selectedAvailableRooms = 0;

        String sectionId = room.getSectionId();
        if (sectionId != null && !sectionId.trim().isEmpty()) {
            db.collection(AppConstants.COLLECTION_SECTIONS)
                    .document(sectionId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            addSectionFromSnapshot(document);
                        } else {
                            loadSectionsByRoomId();
                        }
                    })
                    .addOnFailureListener(e -> loadSectionsByRoomId());
            return;
        }

        loadSectionsByRoomId();
    }

    private void loadSectionsByRoomId() {
        db.collection(AppConstants.COLLECTION_SECTIONS)
                .whereEqualTo("room_id", roomId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        loadSectionsByCamelRoomId();
                        return;
                    }
                    showSections(querySnapshot);
                })
                .addOnFailureListener(e -> showRoomAsDefaultSection());
    }

    private void loadSectionsByCamelRoomId() {
        db.collection(AppConstants.COLLECTION_SECTIONS)
                .whereEqualTo("roomId", roomId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showRoomAsDefaultSection();
                        return;
                    }
                    showSections(querySnapshot);
                })
                .addOnFailureListener(e -> showRoomAsDefaultSection());
    }

    private void showSections(QuerySnapshot querySnapshot) {
        layoutSections.removeAllViews();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            addSectionFromSnapshot(document);
        }
    }

    private void addSectionFromSnapshot(DocumentSnapshot document) {
        Section section = document.toObject(Section.class);
        if (section == null) {
            return;
        }
        section.setId(document.getId());
        if (section.getHotelId() == null || section.getHotelId().isEmpty()) {
            section.setHotelId(hotelId);
        }
        if (section.getBasePrice() <= 0) {
            section.setBasePrice(room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice());
        }
        if (section.getMaxGuests() <= 0) {
            section.setMaxGuests(room.getCapacityAdults() + room.getCapacityChildren());
        }
        if (section.getRoomStyle() == null || section.getRoomStyle().trim().isEmpty()) {
            section.setRoomStyle(valueOrDefault(room.getRoomName(), "Phòng"));
        }
        addSectionView(section, availableRoomsForSection(document));
    }

    private void showRoomAsDefaultSection() {
        layoutSections.removeAllViews();
        Section fallback = new Section(
                roomId,
                room.getPricePerNight() > 0 ? room.getPricePerNight() : hotel.getPrice(),
                hotelId,
                false,
                false,
                room.getCapacityAdults() + room.getCapacityChildren(),
                valueOrDefault(room.getRoomName(), "Phòng")
        );
        addSectionView(fallback, room.getAvailableRooms());
    }

    private void addSectionView(Section section, int availableRooms) {
        LinearLayout box = createBox();
        String style = valueOrDefault(section.getRoomStyle(), valueOrDefault(room.getRoomName(), "Hạng phòng"));
        double price = section.getBasePrice() > 0 ? section.getBasePrice() : room.getPricePerNight();
        int maxGuests = section.getMaxGuests() > 0 ? section.getMaxGuests() : room.getCapacityAdults() + room.getCapacityChildren();

        box.addView(createText(style, true));
        box.addView(createText(formatMoney(price) + " / đêm", true));
        if (maxGuests > 0) {
            box.addView(createText("Tối đa " + maxGuests + " khách", false));
        }
        box.addView(createText(section.isRefundable() ? "Có hoàn tiền" : "Không hoàn tiền", false));
        box.addView(createText(section.isReschedulable() ? "Có đổi lịch" : "Không đổi lịch", false));
        box.addView(createText("Còn " + availableRooms + " phòng", false));

        Button chooseButton = new Button(this);
        chooseButton.setText("Chọn hạng này");
        chooseButton.setEnabled(availableRooms > 0);
        chooseButton.setOnClickListener(v -> selectSection(section, availableRooms));
        if (availableRooms > 0) {
            box.setOnClickListener(v -> selectSection(section, availableRooms));
        }

        box.addView(chooseButton);
        layoutSections.addView(box);
    }

    private void selectSection(Section section, int availableRooms) {
        selectedSection = section;
        selectedAvailableRooms = availableRooms;
        int roomQuantity = getRoomQuantity();
        if (roomQuantity > selectedAvailableRooms) {
            edtRoomQuantity.setText(String.valueOf(selectedAvailableRooms));
            Toast.makeText(this, "Số phòng tối đa là " + selectedAvailableRooms, Toast.LENGTH_SHORT).show();
        }
        tvSelectedSection.setText("Đã chọn: " + valueOrDefault(section.getRoomStyle(), room.getRoomName()));
    }

    private void continueToConfirm() {
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
        if (selectedSection == null) {
            Toast.makeText(this, "Vui lòng chọn hạng phòng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkInDateMillis <= 0 || checkOutDateMillis <= 0) {
            Toast.makeText(this, "Vui lòng chọn ngày đến và ngày đi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkOutDateMillis <= checkInDateMillis) {
            Toast.makeText(this, "Ngày đi phải sau ngày đến", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAvailableRooms <= 0) {
            Toast.makeText(this, "Hạng phòng đã chọn không còn trống", Toast.LENGTH_SHORT).show();
            return;
        }
        int roomQuantity = getRoomQuantity();
        if (roomQuantity > selectedAvailableRooms) {
            Toast.makeText(this, "Số phòng chọn vượt quá số phòng còn trống", Toast.LENGTH_SHORT).show();
            return;
        }
        int maxGuests = selectedSection.getMaxGuests() > 0
                ? selectedSection.getMaxGuests()
                : room.getCapacityAdults() + room.getCapacityChildren();
        if (maxGuests > 0 && getGuestCount() > maxGuests * roomQuantity) {
            Toast.makeText(this, "Số khách vượt quá sức chứa", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ConfirmActivity.class);
        intent.putExtra("hotel_id", hotelId);
        intent.putExtra("room_id", roomId);
        intent.putExtra("owner_id", hotel.getOwnerId());
        intent.putExtra("hotel_name", hotel.getHotelName());
        intent.putExtra("room_name", room.getRoomName());
        intent.putExtra("hotel_image", hotel.getImageUrl());
        intent.putExtra("room_image", valueOrDefault(roomImage, room.getImageUrl()));
        intent.putExtra("address", address);
        intent.putExtra("price", selectedSection.getBasePrice());
        intent.putExtra("check_in", checkInDateMillis);
        intent.putExtra("check_out", checkOutDateMillis);
        intent.putExtra("guest_count", getGuestCount());
        intent.putExtra("room_quantity", roomQuantity);
        intent.putExtra("EXTRA_HOTEL", hotel);
        intent.putExtra("EXTRA_SECTION", selectedSection);
        intent.putExtra("EXTRA_AVAILABLE_ROOMS", selectedAvailableRooms);
        intent.putExtra("EXTRA_CHECK_IN", checkInDateMillis);
        intent.putExtra("EXTRA_CHECK_OUT", checkOutDateMillis);
        intent.putExtra("EXTRA_ROOM_QUANTITY", roomQuantity);
        startActivity(intent);
    }

    private int getGuestCount() {
        try {
            return Math.max(1, Integer.parseInt(edtGuestCount.getText().toString().trim()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int getRoomQuantity() {
        try {
            return Math.max(1, Integer.parseInt(edtRoomQuantity.getText().toString().trim()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int availableRoomsForSection(DocumentSnapshot document) {
        Object value = document.get("available_rooms");
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return room.getAvailableRooms();
    }

    private long checkoutMinDateMillis() {
        Calendar calendar = Calendar.getInstance();
        if (checkInDateMillis > 0) {
            calendar.setTimeInMillis(checkInDateMillis);
        }
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

    private LinearLayout createBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackgroundResource(R.drawable.bg_input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        box.setLayoutParams(params);
        return box;
    }

    private TextView createText(String text, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(valueOrDefault(text, ""));
        textView.setTextColor(getColor(R.color.text_primary));
        textView.setTextSize(14);
        textView.setPadding(0, dp(3), 0, dp(3));
        if (bold) {
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        }
        return textView;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0f VND", value);
    }

    private String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%.0f", value);
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

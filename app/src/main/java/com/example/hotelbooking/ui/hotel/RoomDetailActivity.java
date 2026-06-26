package com.example.hotelbooking.ui.hotel;

import android.app.DatePickerDialog;
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
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private TextView tvRoomName;
    private TextView tvRoomType;
    private TextView tvRoomDescription;
    private TextView tvRoomInfo;
    private TextView tvSelectedSection;
    private TextView btnCheckInDate;
    private TextView btnCheckOutDate;
    private EditText edtGuestCount;
    private LinearLayout layoutSections;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        db = FirebaseFirestore.getInstance();
        receiveIntentData();
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

        if (room != null && (roomId == null || roomId.isEmpty())) {
            roomId = room.getId();
        }
        if (hotel != null && (hotelId == null || hotelId.isEmpty())) {
            hotelId = hotel.getId();
        }
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
        layoutSections = findViewById(R.id.layoutSections);
        btnContinue = findViewById(R.id.btnContinueConfirm);

        btnContinue.setOnClickListener(v -> continueToConfirm());
    }

    private void bindRoomData() {
        if (hotel == null || room == null || roomId == null || roomId.isEmpty()) {
            Toast.makeText(this, "Khong tim thay thong tin phong", Toast.LENGTH_LONG).show();
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

        tvRoomName.setText(valueOrDefault(room.getRoomName(), "Phong"));
        tvRoomType.setText(valueOrDefault(room.getRoomType(), "Hang phong tieu chuan"));
        tvRoomDescription.setText(valueOrDefault(room.getDescription(), "Phong dang cap nhat mo ta chi tiet."));

        String bed = valueOrDefault(room.getBedType(), "Tieu chuan");
        int adults = room.getCapacityAdults();
        int children = room.getCapacityChildren();
        String sizeText = room.getRoomSize() > 0 ? formatNumber(room.getRoomSize()) + " m2 - " : "";
        tvRoomInfo.setText(sizeText + "Giuong: " + bed + " - Suc chua: " + adults + " nguoi lon, " + children + " tre em");
    }

    private void setupDatePickers() {
        btnCheckInDate.setOnClickListener(v -> showDatePicker(true));
        btnCheckOutDate.setOnClickListener(v -> {
            if (checkInDateMillis <= 0) {
                Toast.makeText(this, "Vui long chon ngay den truoc", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Ngay den khong duoc nho hon hom nay", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        checkInDateMillis = selectedMillis;
                        btnCheckInDate.setText(dateFormat.format(new Date(checkInDateMillis)));
                        if (checkOutDateMillis > 0 && checkOutDateMillis <= checkInDateMillis) {
                            checkOutDateMillis = 0;
                            btnCheckOutDate.setText("Bam chon ngay di");
                        }
                        return;
                    }

                    if (checkInDateMillis <= 0) {
                        Toast.makeText(this, "Hay chon ngay den truoc", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedMillis <= checkInDateMillis) {
                        Toast.makeText(this, "Ngay di phai sau ngay den", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    checkOutDateMillis = selectedMillis;
                    btnCheckOutDate.setText(dateFormat.format(new Date(checkOutDateMillis)));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.setTitle(isCheckIn ? "Chon ngay den" : "Chon ngay di");
        dialog.getDatePicker().setMinDate(isCheckIn ? todayStartMillis() : checkoutMinDateMillis());
        dialog.show();
    }

    private void loadSections() {
        layoutSections.removeAllViews();
        layoutSections.addView(createText("Dang tai hang phong...", false));
        db.collection("sections")
                .whereEqualTo("room_id", roomId)
                .get()
                .addOnSuccessListener(this::showSectionsOrTryCamelCase)
                .addOnFailureListener(e -> showRoomAsDefaultSection());
    }

    private void showSectionsOrTryCamelCase(QuerySnapshot querySnapshot) {
        if (!querySnapshot.isEmpty()) {
            showSections(querySnapshot);
            return;
        }
        db.collection("sections")
                .whereEqualTo("roomId", roomId)
                .get()
                .addOnSuccessListener(secondSnapshot -> {
                    if (secondSnapshot.isEmpty()) {
                        showRoomAsDefaultSection();
                    } else {
                        showSections(secondSnapshot);
                    }
                })
                .addOnFailureListener(e -> showRoomAsDefaultSection());
    }

    private void showSections(QuerySnapshot querySnapshot) {
        layoutSections.removeAllViews();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Section section = document.toObject(Section.class);
            if (section == null) {
                continue;
            }
            section.setId(document.getId());
            addSectionView(section, availableRoomsForSection(document));
        }
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
                valueOrDefault(room.getRoomName(), "Phong")
        );
        addSectionView(fallback, room.getAvailableRooms());
    }

    private void addSectionView(Section section, int availableRooms) {
        LinearLayout box = createBox();
        String style = valueOrDefault(section.getRoomStyle(), valueOrDefault(room.getRoomName(), "Hang phong"));
        double price = section.getBasePrice() > 0 ? section.getBasePrice() : room.getPricePerNight();
        int maxGuests = section.getMaxGuests() > 0 ? section.getMaxGuests() : room.getCapacityAdults() + room.getCapacityChildren();

        box.addView(createText(style, true));
        box.addView(createText(formatMoney(price) + " / dem", true));
        if (maxGuests > 0) {
            box.addView(createText("Toi da " + maxGuests + " khach", false));
        }
        box.addView(createText(section.isRefundable() ? "Co hoan tien" : "Khong hoan tien", false));
        box.addView(createText(section.isReschedulable() ? "Co doi lich" : "Khong doi lich", false));
        box.addView(createText("Con " + availableRooms + " phong", false));

        Button chooseButton = new Button(this);
        chooseButton.setText("Chon hang nay");
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
        tvSelectedSection.setText("Da chon: " + valueOrDefault(section.getRoomStyle(), room.getRoomName()));
    }

    private void continueToConfirm() {
        if (selectedSection == null) {
            Toast.makeText(this, "Vui long chon hang phong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkInDateMillis <= 0 || checkOutDateMillis <= 0) {
            Toast.makeText(this, "Vui long chon ngay den va ngay di", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkOutDateMillis <= checkInDateMillis) {
            Toast.makeText(this, "Ngay di phai sau ngay den", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAvailableRooms <= 0) {
            Toast.makeText(this, "Hang phong da chon khong con trong", Toast.LENGTH_SHORT).show();
            return;
        }
        int maxGuests = selectedSection.getMaxGuests() > 0
                ? selectedSection.getMaxGuests()
                : room.getCapacityAdults() + room.getCapacityChildren();
        if (maxGuests > 0 && getGuestCount() > maxGuests) {
            Toast.makeText(this, "So khach vuot qua suc chua", Toast.LENGTH_SHORT).show();
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
        intent.putExtra("EXTRA_HOTEL", hotel);
        intent.putExtra("EXTRA_SECTION", selectedSection);
        intent.putExtra("EXTRA_AVAILABLE_ROOMS", selectedAvailableRooms);
        intent.putExtra("EXTRA_CHECK_IN", checkInDateMillis);
        intent.putExtra("EXTRA_CHECK_OUT", checkOutDateMillis);
        startActivity(intent);
    }

    private int getGuestCount() {
        try {
            return Math.max(1, Integer.parseInt(edtGuestCount.getText().toString().trim()));
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

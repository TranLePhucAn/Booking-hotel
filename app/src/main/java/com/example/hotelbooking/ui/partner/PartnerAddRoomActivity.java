package com.example.hotelbooking.ui.partner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PartnerAddRoomActivity extends AppCompatActivity {

    private EditText etRoomName, etPrice, etCapacity, etBedType, etTotalRooms, etDescription, etRoomStyle, etBasePrice, etRoomImageUrl;
    private Spinner spinnerRoomType;
    private CheckBox cbWifi, cbAirConditioner, cbTv, cbIsRefundable, cbIsReschedulable;
    private Button btnSaveRoom;

    private String hotelId;
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_add_room);

        Intent intent = getIntent();
        if(intent != null) {
            hotelId = intent.getStringExtra("EXTRA_HOTEL_ID");
        }

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews();

        btnSaveRoom.setOnClickListener(view -> {
            if(validateInput()) {
                saveRoomAndSectionData();
            }
        });
    }

    private void saveRoomAndSectionData() {
        btnSaveRoom.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String roomName = etRoomName.getText().toString().trim();
        String roomType = spinnerRoomType.getSelectedItem().toString();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        long capacityAdults = Long.parseLong(etCapacity.getText().toString().trim());
        String bedType = etBedType.getText().toString().trim();
        long totalRooms = Long.parseLong(etTotalRooms.getText().toString().trim());
        String description = etDescription.getText().toString().trim();

        String imageUrl = etRoomImageUrl.getText().toString().trim();
        if (imageUrl.isEmpty()) {
            imageUrl = "https://images.unsplash.com/photo-1590490360182-c33d57733427";
        }

        ArrayList<String> amenitiesList = new ArrayList<>();
        if(cbWifi.isChecked()) amenitiesList.add("Wifi");
        if (cbAirConditioner.isChecked()) amenitiesList.add("Máy lạnh");
        if (cbTv.isChecked()) amenitiesList.add("TV");

        String roomStyle = etRoomStyle.getText().toString().trim();
        double basePrice = Double.parseDouble(etBasePrice.getText().toString().trim());
        boolean isRefundable = cbIsRefundable.isChecked();
        boolean isReschedulable = cbIsReschedulable.isChecked();

        if(roomStyle.isEmpty()) {
            roomStyle = roomName;
        }

        String newSectionId = db.collection("sections").document().getId();
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("hotel_id", hotelId);
        sectionData.put("owner_id", ownerId);
        sectionData.put("room_style", roomStyle);
        sectionData.put("base_price", basePrice);
        sectionData.put("max_guests", capacityAdults);
        sectionData.put("is_refundable", isRefundable);
        sectionData.put("is_reschedulable", isReschedulable);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("section_id", newSectionId);
        roomData.put("hotel_id", hotelId);
        roomData.put("owner_id", ownerId);
        roomData.put("room_name", roomName);
        roomData.put("room_type", roomType);
        roomData.put("price_per_night", price);
        roomData.put("capacity_adults", capacityAdults);
        roomData.put("capacity_children", 0L); // mặc định
        roomData.put("bed_type", bedType);
        roomData.put("total_rooms", totalRooms);
        roomData.put("description", description);
        roomData.put("amenities", amenitiesList);
        roomData.put("status", "AVAILABLE");
        roomData.put("floor", 1L);
        roomData.put("room_size", 25.0);
        roomData.put("image_url", imageUrl);

        // tiến hành insert
        db.collection("sections").document(newSectionId).set(sectionData)
                .addOnSuccessListener(runnable -> {
                    db.collection("rooms").add(roomData)
                            .addOnSuccessListener(runnable1 -> {
                                Toast.makeText(PartnerAddRoomActivity.this, "Đăng ký hạng phòng lên khách sạn thành công!", Toast.LENGTH_LONG).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnSaveRoom.setEnabled(true);
                                Toast.makeText(PartnerAddRoomActivity.this, "Lỗi tạo thông tin phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSaveRoom.setEnabled(true);
                    Toast.makeText(PartnerAddRoomActivity.this, "Lỗi tạo phân đoạn dịch vụ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput() {
        if (etRoomName.getText().toString().trim().isEmpty()) { etRoomName.setError("Vui lòng nhập tên phòng"); return false; }
        if (etPrice.getText().toString().trim().isEmpty()) { etPrice.setError("Vui lòng nhập giá phòng"); return false; }
        if (etCapacity.getText().toString().trim().isEmpty()) { etCapacity.setError("Vui lòng nhập sức chứa"); return false; }
        if (etBedType.getText().toString().trim().isEmpty()) { etBedType.setError("Vui lòng nhập loại giường"); return false; }
        if (etTotalRooms.getText().toString().trim().isEmpty()) { etTotalRooms.setError("Vui lòng nhập tổng số phòng"); return false; }
        if (hotelId == null || hotelId.isEmpty()) {
            Toast.makeText(this, "Lỗi hệ thống: Không tìm thấy ID khách sạn!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ownerId == null || ownerId.isEmpty()) {
            Toast.makeText(this, "Vui lÃ²ng Ä‘Äƒng nháº­p láº¡i Ä‘á»ƒ thÃªm phÃ²ng", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initViews() {
        etRoomName = findViewById(R.id.et_room_name);
        spinnerRoomType = findViewById(R.id.spinner_room_type);
        etPrice = findViewById(R.id.et_price);
        etCapacity = findViewById(R.id.et_capacity);
        etBedType = findViewById(R.id.et_bed_type);
        etTotalRooms = findViewById(R.id.et_total_rooms);
        etDescription = findViewById(R.id.et_description);

        cbWifi = findViewById(R.id.cb_wifi);
        cbAirConditioner = findViewById(R.id.cb_air_conditioner);
        cbTv = findViewById(R.id.cb_tv);

        etRoomStyle = findViewById(R.id.et_room_style);
        etBasePrice = findViewById(R.id.et_base_price);
        cbIsRefundable = findViewById(R.id.cb_is_refundable);
        cbIsReschedulable = findViewById(R.id.cb_is_reschedulable);

        btnSaveRoom = findViewById(R.id.btn_save_room);
        etRoomImageUrl = findViewById(R.id.et_room_image_url);
    }
}

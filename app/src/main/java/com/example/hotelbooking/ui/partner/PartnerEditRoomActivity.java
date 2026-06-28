package com.example.hotelbooking.ui.partner;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PartnerEditRoomActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String roomId, sectionId;

    private EditText etRoomName, etBedType, etCapacity, etTotalRooms, etPrice, etBasePrice, etRoomImageUrl;

    // Các thành phần lựa chọn
    private Spinner spinnerRoomType;
    private CheckBox cbWifi, cbAir, cbTv, cbIsRefundable, cbIsReschedulable;
    private androidx.appcompat.widget.SwitchCompat switchHideRoom, switchDisableRoom;

    // Nút bấm thực thi
    private Button btnUpdateRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_edit_room);

        db = FirebaseFirestore.getInstance();

        roomId = getIntent().getStringExtra("EXTRA_ROOM_ID");
        sectionId = getIntent().getStringExtra("EXTRA_SECTION_ID");

        initViews();
        loadOldData();

        // lưu update
        btnUpdateRoom.setOnClickListener(view -> {
            updateRoomAndSectionData();
        });
    }

    private void updateRoomAndSectionData() {
        String name = etRoomName.getText().toString().trim();
        long totalRooms = Long.parseLong(etTotalRooms.getText().toString().trim());
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        double basePrice = Double.parseDouble(etBasePrice.getText().toString().trim());
        String newImageUrl = etRoomImageUrl.getText().toString().trim();

        btnUpdateRoom.setEnabled(false);

        ArrayList<String> updatedAmenities = new ArrayList<>();
        if(cbWifi.isChecked()) updatedAmenities.add("Wifi");
        if(cbAir.isChecked()) updatedAmenities.add("Máy lạnh");
        if(cbTv.isChecked()) updatedAmenities.add("TV");

        WriteBatch batch = db.batch();

        DocumentReference roomRef = db.collection("rooms").document(roomId);
        Map<String, Object> roomUpdates = new HashMap<>();
        roomUpdates.put("room_name", name);
        roomUpdates.put("room_type", spinnerRoomType.getSelectedItem().toString());
        roomUpdates.put("bed_type", etBedType.getText().toString().trim());
        roomUpdates.put("capacity_adults", Long.parseLong(etCapacity.getText().toString().trim()));
        roomUpdates.put("total_rooms", totalRooms);
        roomUpdates.put("price_per_night", price);

        roomUpdates.put("amenities", updatedAmenities);

        roomUpdates.put("status", switchDisableRoom.isChecked() ? "SUSPENDED" : "AVAILABLE");
        roomUpdates.put("image_url", newImageUrl);

        batch.update(roomRef, roomUpdates);

        DocumentReference sectionRef = db.collection("sections").document(sectionId);
        Map<String, Object> sectionUpdates = new HashMap<>();
        sectionUpdates.put("room_style", name);
        sectionUpdates.put("base_price", basePrice);
        sectionUpdates.put("is_refundable", cbIsRefundable.isChecked());
        sectionUpdates.put("is_reschedulable", cbIsReschedulable.isChecked());
//        sectionUpdates.put("is_hidden", switchHideRoom.isChecked());

        batch.update(sectionRef, sectionUpdates);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PartnerEditRoomActivity.this, "Cập nhật hạng phòng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnUpdateRoom.setEnabled(true);
                    Toast.makeText(PartnerEditRoomActivity.this, "Lỗi khi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadOldData() {
        if(roomId == null || sectionId == null) {
            Toast.makeText(this, "Không tìm thấy mã phòng để chỉnh sửa!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("rooms").document(roomId).get()
                .addOnSuccessListener(roomDoc -> {
                    if(roomDoc.exists()) {
                        etRoomName.setText(roomDoc.getString("room_name"));

                        if(roomDoc.contains("image_url")) {
                            etRoomImageUrl.setText(roomDoc.getString("image_url"));
                        }

                        String roomType = roomDoc.getString("room_type");
                        if(roomType != null) {
                            ArrayAdapter adapter = (ArrayAdapter) spinnerRoomType.getAdapter();
                            int spinnerPosition = adapter.getPosition(roomType); // index
                            spinnerRoomType.setSelection(spinnerPosition); // hiển thị phần tử ở vị trí spinnerPosition
                        }

                        etBedType.setText(roomDoc.getString("bed_type"));

                        long capacity = roomDoc.get("capacity_adults") != null ? roomDoc.getLong("capacity_adults") : 0;
                        etCapacity.setText(String.valueOf(capacity));

                        long total = roomDoc.get("total_rooms") != null ? roomDoc.getLong("total_rooms") : 0;
                        etTotalRooms.setText(String.valueOf(total));

                        if (roomDoc.get("price_per_night") != null) {
                            double price = roomDoc.getDouble("price_per_night");
                            etPrice.setText(String.valueOf((long) price));
                        }

                        if (roomDoc.get("amenities") != null) {
                            ArrayList<String> amenitiesList = (ArrayList<String>) roomDoc.get("amenities");
                            if (amenitiesList != null) {
                                cbWifi.setChecked(amenitiesList.contains("Wifi"));
                                cbAir.setChecked(amenitiesList.contains("Máy lạnh"));
                                cbTv.setChecked(amenitiesList.contains("TV"));
                            }
                        }

                        // đọc trạng thái tạm ẩn của phòng

                        // đọc trạng thái kinh doanh của phòng
                        String roomStatus = roomDoc.getString("status");
                        if (roomStatus != null) {
                            switchDisableRoom.setChecked("SUSPENDED".equals(roomStatus));
                        } else {
                            switchDisableRoom.setChecked(false); // Mặc định là AVAILABLE
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("EDIT_ROOM", "Lỗi tải dữ liệu phòng: " + e.getMessage()));

        db.collection("sections").document(sectionId).get()
                .addOnSuccessListener(sectionDoc -> {
                    if (sectionDoc.exists()) {
                        if (sectionDoc.get("base_price") != null) {
                            double basePrice = sectionDoc.getDouble("base_price");
                            etBasePrice.setText(String.valueOf((long) basePrice));
                        }

                        if (sectionDoc.getBoolean("is_refundable") != null) {
                            boolean isRefundable = sectionDoc.getBoolean("is_refundable");
                            cbIsRefundable.setChecked(isRefundable);
                        }

                        if (sectionDoc.getBoolean("is_reschedulable") != null) {
                            boolean isReschedulable = sectionDoc.getBoolean("is_reschedulable");
                            cbIsReschedulable.setChecked(isReschedulable);
                        }

//                        if (sectionDoc.getBoolean("is_hidden") != null) {
//                            switchHideRoom.setChecked(sectionDoc.getBoolean("is_hidden"));
//                        } else {
//                            switchHideRoom.setChecked(false);
//                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("EDIT_ROOM", "Lỗi tải dữ liệu hạng phòng: " + e.getMessage()));
    }

    private void initViews() {
        etRoomName = findViewById(R.id.et_edit_room_name);
        spinnerRoomType = findViewById(R.id.spinner_edit_room_type);
        etBedType = findViewById(R.id.et_edit_bed_type);

        etCapacity = findViewById(R.id.et_edit_capacity);
        etTotalRooms = findViewById(R.id.et_edit_total_rooms);

        etPrice = findViewById(R.id.et_edit_price);
        etBasePrice = findViewById(R.id.et_edit_base_price);
        cbWifi = findViewById(R.id.cb_edit_wifi);
        cbAir = findViewById(R.id.cb_edit_air);
        cbTv = findViewById(R.id.cb_edit_tv);

        cbIsRefundable = findViewById(R.id.cb_is_refundable);
        cbIsReschedulable = findViewById(R.id.cb_is_reschedulable);

        switchHideRoom = findViewById(R.id.switch_hide_room);
        switchDisableRoom = findViewById(R.id.switch_disable_room);

        btnUpdateRoom = findViewById(R.id.btn_update_room);
        etRoomImageUrl = findViewById(R.id.et_edit_room_image_url);
    }
}

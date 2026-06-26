package com.example.hotelbooking.ui.owner;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRoomActivity extends AppCompatActivity {

    private String hotelId;
    private String hotelName;
    private EditText edtRoomName;
    private EditText edtRoomType;
    private EditText edtRoomDescription;
    private EditText edtRoomPrice;
    private EditText edtAdults;
    private EditText edtChildren;
    private EditText edtBedType;
    private EditText edtRoomSize;
    private EditText edtAvailableRooms;
    private EditText edtRoomImage;
    private EditText edtRoomAmenities;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        db = FirebaseFirestore.getInstance();
        hotelId = getIntent().getStringExtra("hotel_id");
        hotelName = getIntent().getStringExtra("hotel_name");

        TextView tvAddRoomTitle = findViewById(R.id.tvAddRoomTitle);
        tvAddRoomTitle.setText("Them phong cho " + (hotelName == null ? "khach san" : hotelName));

        bindViews();

        Button btnSaveRoom = findViewById(R.id.btnSaveRoom);
        Button btnFinishHotelPost = findViewById(R.id.btnFinishHotelPost);
        btnSaveRoom.setOnClickListener(v -> saveRoom());
        btnFinishHotelPost.setOnClickListener(v -> finishPost());
    }

    private void bindViews() {
        edtRoomName = findViewById(R.id.edtRoomName);
        edtRoomType = findViewById(R.id.edtRoomType);
        edtRoomDescription = findViewById(R.id.edtRoomDescription);
        edtRoomPrice = findViewById(R.id.edtRoomPrice);
        edtAdults = findViewById(R.id.edtAdults);
        edtChildren = findViewById(R.id.edtChildren);
        edtBedType = findViewById(R.id.edtBedType);
        edtRoomSize = findViewById(R.id.edtRoomSize);
        edtAvailableRooms = findViewById(R.id.edtAvailableRooms);
        edtRoomImage = findViewById(R.id.edtRoomImage);
        edtRoomAmenities = findViewById(R.id.edtRoomAmenities);
    }

    private void saveRoom() {
        String roomName = textOf(edtRoomName);
        if (TextUtils.isEmpty(hotelId) || TextUtils.isEmpty(roomName)) {
            Toast.makeText(this, "Vui long nhap ten phong", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
        int availableRooms = intOf(edtAvailableRooms, 0);

        Map<String, Object> room = new HashMap<>();
        room.put("hotel_id", hotelId);
        room.put("hotel_name", hotelName == null ? "" : hotelName);
        room.put("owner_id", ownerId);
        room.put("room_name", roomName);
        room.put("room_type", textOf(edtRoomType));
        room.put("description", textOf(edtRoomDescription));
        room.put("price_per_night", doubleOf(edtRoomPrice, 0));
        room.put("capacity_adults", intOf(edtAdults, 0));
        room.put("capacity_children", intOf(edtChildren, 0));
        room.put("capacity", intOf(edtAdults, 0) + " nguoi lon, " + intOf(edtChildren, 0) + " tre em");
        room.put("bed_type", textOf(edtBedType));
        room.put("room_size", doubleOf(edtRoomSize, 0));
        room.put("area", doubleOf(edtRoomSize, 0));
        room.put("available_rooms", availableRooms);
        room.put("image_url", textOf(edtRoomImage));
        room.put("amenities", listOf(textOf(edtRoomAmenities)));
        room.put("status", availableRooms > 0 ? "AVAILABLE" : "SUSPENDED");
        room.put("created_at", System.currentTimeMillis());

        db.collection("rooms")
                .add(room)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Da luu phong. Co the them phong tiep theo.", Toast.LENGTH_SHORT).show();
                    clearRoomForm();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong luu duoc phong: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void finishPost() {
        if (TextUtils.isEmpty(hotelId)) {
            finish();
            return;
        }

        db.collection("hotels")
                .document(hotelId)
                .update("status", "pending")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Bai dang da gui duyet va chua hien cong khai", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong cap nhat duoc trang thai: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void clearRoomForm() {
        edtRoomName.setText("");
        edtRoomType.setText("");
        edtRoomDescription.setText("");
        edtRoomPrice.setText("");
        edtAdults.setText("");
        edtChildren.setText("");
        edtBedType.setText("");
        edtRoomSize.setText("");
        edtAvailableRooms.setText("");
        edtRoomImage.setText("");
        edtRoomAmenities.setText("");
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

    private int intOf(EditText editText, int fallback) {
        try {
            String value = textOf(editText);
            return value.isEmpty() ? fallback : Integer.parseInt(value);
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

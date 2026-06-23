package com.example.hotelbooking.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OwnerDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String ownerId;
    private LinearLayout layoutOwnerRooms;
    private LinearLayout layoutOwnerBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ownerId = user != null ? user.getUid() : "";

        TextView tvOwnerEmail = findViewById(R.id.tvOwnerEmail);
        Button btnAddHotel = findViewById(R.id.btnAddHotel);
        Button btnImportRooms = findViewById(R.id.btnImportRooms);
        Button btnManageRooms = findViewById(R.id.btnManageRooms);
        Button btnLogoutOwner = findViewById(R.id.btnLogoutOwner);
        layoutOwnerRooms = findViewById(R.id.layoutOwnerRooms);
        layoutOwnerBookings = findViewById(R.id.layoutOwnerBookings);

        if (user != null) {
            tvOwnerEmail.setText(user.getEmail());
        }

        btnAddHotel.setOnClickListener(v ->
                startActivity(new Intent(this, AddHotelActivity.class)));
        btnImportRooms.setOnClickListener(v ->
                Toast.makeText(this, "Chuc nang import Excel se bo sung sau", Toast.LENGTH_SHORT).show());
        btnManageRooms.setOnClickListener(v -> refreshOwnerData());
        btnLogoutOwner.setOnClickListener(v -> logout());

        refreshOwnerData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshOwnerData();
    }

    private void refreshOwnerData() {
        if (ownerId == null || ownerId.isEmpty()) {
            return;
        }
        loadRooms();
        loadBookings();
    }

    private void loadRooms() {
        layoutOwnerRooms.removeAllViews();
        db.collection("rooms")
                .whereEqualTo("owner_id", ownerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        layoutOwnerRooms.addView(createText("Chua co phong nao", false));
                        return;
                    }
                    for (DocumentSnapshot room : querySnapshot.getDocuments()) {
                        addRoomCard(room);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong tai duoc phong: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void addRoomCard(DocumentSnapshot room) {
        LinearLayout box = createBox();
        String status = stringValue(room, "status", "AVAILABLE");
        int availableRooms = intValue(room, "available_rooms", 0);

        box.addView(createText(stringValue(room, "room_name", "Phong"), true));
        box.addView(createText("Khach san: " + stringValue(room, "hotel_name", stringValue(room, "hotel_id", "")), false));
        box.addView(createText("Gia: " + doubleValue(room, "price_per_night", 0), false));
        box.addView(createText("Trang thai: " + roomStatusLabel(status, availableRooms), true));

        EditText edtAvailable = new EditText(this);
        edtAvailable.setHint("So phong trong");
        edtAvailable.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtAvailable.setText(String.valueOf(availableRooms));
        edtAvailable.setBackgroundResource(R.drawable.bg_input);
        edtAvailable.setPadding(12, 8, 12, 8);
        box.addView(edtAvailable);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button btnSave = new Button(this);
        Button btnToggle = new Button(this);
        btnSave.setText("Cap nhat so luong");
        btnToggle.setText(isRoomOpen(status, availableRooms) ? "Dong phong" : "Mo phong");
        row.addView(btnSave, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(btnToggle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.addView(row);

        btnSave.setOnClickListener(v -> updateRoomAvailability(room, edtAvailable));
        btnToggle.setOnClickListener(v -> toggleRoomStatus(room, status));

        layoutOwnerRooms.addView(box);
    }

    private void updateRoomAvailability(DocumentSnapshot room, EditText edtAvailable) {
        int value = 0;
        try {
            value = Integer.parseInt(edtAvailable.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("available_rooms", value);
        updates.put("status", value > 0 ? "AVAILABLE" : "SUSPENDED");
        room.getReference().update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Da cap nhat phong", Toast.LENGTH_SHORT).show();
                    loadRooms();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong cap nhat duoc phong: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void toggleRoomStatus(DocumentSnapshot room, String currentStatus) {
        boolean open = isRoomOpen(currentStatus, intValue(room, "available_rooms", 0));
        room.getReference()
                .update("status", open ? "SUSPENDED" : "AVAILABLE")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, open ? "Da dong phong" : "Da mo phong", Toast.LENGTH_SHORT).show();
                    loadRooms();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong doi duoc trang thai: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadBookings() {
        layoutOwnerBookings.removeAllViews();
        db.collection("bookings")
                .whereEqualTo("owner_id", ownerId)
                .whereEqualTo("status", "pending_confirmation")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        layoutOwnerBookings.addView(createText("Chua co don moi", false));
                        return;
                    }
                    for (DocumentSnapshot booking : querySnapshot.getDocuments()) {
                        addBookingCard(booking);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong tai duoc don dat phong: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void addBookingCard(DocumentSnapshot booking) {
        LinearLayout box = createBox();
        box.addView(createText(stringValue(booking, "hotel_name", "Khach san"), true));
        box.addView(createText("Phong: " + stringValue(booking, "room_name", ""), false));
        box.addView(createText("Khach: " + stringValue(booking, "guest_name", ""), false));
        box.addView(createText("Lien he: " + stringValue(booking, "guest_phone", ""), false));
        box.addView(createText("Ngay: " + stringValue(booking, "check_in", "") + " - " + stringValue(booking, "check_out", ""), false));

        Button btnConfirm = new Button(this);
        btnConfirm.setText("Xac nhan dat phong");
        btnConfirm.setOnClickListener(v -> confirmBooking(booking));
        box.addView(btnConfirm);

        layoutOwnerBookings.addView(box);
    }

    private void confirmBooking(DocumentSnapshot booking) {
        booking.getReference()
                .update("status", "confirmed")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Da xac nhan dat phong", Toast.LENGTH_SHORT).show();
                    loadBookings();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong xac nhan duoc: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private boolean isRoomOpen(String status, int availableRooms) {
        return "AVAILABLE".equalsIgnoreCase(status) && availableRooms > 0;
    }

    private String roomStatusLabel(String status, int availableRooms) {
        if (!isRoomOpen(status, availableRooms)) {
            return "Het phong / Tam ngung phuc vu";
        }
        return "Dang mo - con " + availableRooms + " phong";
    }

    private LinearLayout createBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(16, 14, 16, 14);
        box.setBackgroundResource(R.drawable.bg_input);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 10);
        box.setLayoutParams(params);
        return box;
    }

    private TextView createText(String text, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getColor(R.color.text_primary));
        textView.setTextSize(14);
        if (bold) {
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
        }
        textView.setPadding(0, 3, 0, 3);
        return textView;
    }

    private String stringValue(DocumentSnapshot doc, String field, String fallback) {
        String value = doc.getString(field);
        return value == null ? fallback : value;
    }

    private int intValue(DocumentSnapshot doc, String field, int fallback) {
        Object value = doc.get(field);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return fallback;
    }

    private double doubleValue(DocumentSnapshot doc, String field, double fallback) {
        Object value = doc.get(field);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return fallback;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

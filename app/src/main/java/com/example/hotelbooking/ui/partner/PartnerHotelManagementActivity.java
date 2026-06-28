package com.example.hotelbooking.ui.partner;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class PartnerHotelManagementActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private LinearLayout hotelContainer;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        hotelContainer = new LinearLayout(this);
        hotelContainer.setOrientation(LinearLayout.VERTICAL);
        hotelContainer.setPadding(dp(16), dp(16), dp(16), dp(16));
        scrollView.addView(hotelContainer);
        setContentView(scrollView);

        TextView titleView = createText("Khách sạn của tôi", 22, true);
        hotelContainer.addView(titleView);

        loadPartnerHotels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hotelContainer != null && currentUserId != null) {
            loadPartnerHotels();
        }
    }

    private void loadPartnerHotels() {
        hotelContainer.removeViews(1, Math.max(0, hotelContainer.getChildCount() - 1));

        firestore.collection(AppConstants.COLLECTION_HOTELS)
                .whereEqualTo("owner_id", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        hotelContainer.addView(createText("Bạn chưa đăng khách sạn nào", 15, false));
                        return;
                    }

                    querySnapshot.getDocuments().forEach(document -> {
                        Hotel hotel = Hotel.fromDocument(document);
                        addHotelCard(hotel);
                    });
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Không tải được khách sạn: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void addHotelCard(Hotel hotel) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundResource(R.drawable.bg_input);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, dp(12), 0, 0);
        card.setLayoutParams(cardParams);

        card.addView(createText(valueOrDefault(hotel.getHotelName(), "Khách sạn"), 17, true));
        card.addView(createText("Địa chỉ: " + valueOrDefault(hotel.getAddress(), "Đang cập nhật"), 14, false));
        card.addView(createText("Giá từ: " + String.format(Locale.getDefault(), "%,.0f VNĐ", hotel.getPrice()), 14, false));
        card.addView(createText("Trạng thái: " + displayApprovalStatus(hotel), 14, true));

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, dp(8), 0, 0);

        Button previewButton = createButton("Preview");
        Button editButton = createButton("Sửa");
        Button roomButton = createButton("Thêm phòng");

        previewButton.setOnClickListener(v -> openPreview(hotel));
        editButton.setOnClickListener(v -> openEdit(hotel));
        roomButton.setOnClickListener(v -> openAddRoom(hotel));

        buttonRow.addView(previewButton);
        buttonRow.addView(editButton);
        buttonRow.addView(roomButton);
        card.addView(buttonRow);

        hotelContainer.addView(card);
    }

    private void openPreview(Hotel hotel) {
        Intent intent = new Intent(this, HotelDetailActivity.class);
        intent.putExtra("hotel", hotel);
        intent.putExtra("hotel_id", hotel.getId());
        intent.putExtra("mode", "admin_preview");
        startActivity(intent);
    }

    private void openEdit(Hotel hotel) {
        Intent intent = new Intent(this, PartnerEditHotelActivity.class);
        intent.putExtra("hotel_id", hotel.getId());
        startActivity(intent);
    }

    private void openAddRoom(Hotel hotel) {
        Intent intent = new Intent(this, AddRoomActivity.class);
        intent.putExtra("hotel_id", hotel.getId());
        intent.putExtra("hotel_name", hotel.getHotelName());
        startActivity(intent);
    }

    private Button createButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);
        return button;
    }

    private TextView createText(String text, int sizeSp, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(sizeSp);
        textView.setTextColor(getColor(R.color.text_primary));
        textView.setPadding(0, dp(3), 0, dp(3));
        if (bold) {
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        }
        return textView;
    }

    private String displayApprovalStatus(Hotel hotel) {
        String status = hotel.getApprovalStatus();
        if (AppConstants.STATUS_APPROVED.equalsIgnoreCase(valueOrDefault(status, "")) && hotel.isActive()) {
            return "Đã duyệt";
        }
        if (AppConstants.STATUS_REJECTED.equalsIgnoreCase(valueOrDefault(status, ""))) {
            return "Bị từ chối";
        }
        return "Chờ duyệt";
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

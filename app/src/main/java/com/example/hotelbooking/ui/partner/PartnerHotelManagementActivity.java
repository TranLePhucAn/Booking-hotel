package com.example.hotelbooking.ui.partner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.ui.adapter.PartnerHotelManagementAdapter;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PartnerHotelManagementActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private PartnerHotelManagementAdapter adapter;
    private final List<DocumentSnapshot> hotels = new ArrayList<>();
    private TextView textSummary;
    private ProgressBar progressLoading;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_hotel_management);

        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupRecyclerView();
        loadPartnerHotels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadPartnerHotels();
        }
    }

    private void bindViews() {
        TextView buttonBack = findViewById(R.id.buttonBack);
        Button buttonAddHotel = findViewById(R.id.buttonAddHotel);
        textSummary = findViewById(R.id.textSummary);
        progressLoading = findViewById(R.id.progressLoading);

        buttonBack.setOnClickListener(v -> finish());
        buttonAddHotel.setOnClickListener(v -> startActivity(new Intent(this, AddHotelActivity.class)));
    }

    private void setupRecyclerView() {
        RecyclerView recyclerHotels = findViewById(R.id.recyclerHotels);
        recyclerHotels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PartnerHotelManagementAdapter(new PartnerHotelManagementAdapter.OnHotelActionListener() {
            @Override
            public void onViewDetail(Hotel hotel) {
                openPreview(hotel);
            }

            @Override
            public void onEdit(Hotel hotel) {
                openEdit(hotel);
            }

            @Override
            public void onAddRoom(Hotel hotel) {
                openAddRoom(hotel);
            }
        });
        recyclerHotels.setAdapter(adapter);
    }

    private void loadPartnerHotels() {
        progressLoading.setVisibility(View.VISIBLE);
        textSummary.setText("Đang tải khách sạn...");

        firestore.collection(AppConstants.COLLECTION_HOTELS)
                .whereEqualTo("owner_id", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    hotels.clear();
                    hotels.addAll(querySnapshot.getDocuments());
                    hotels.sort(Comparator.comparing(this::hotelNameForSort));
                    adapter.updateData(hotels);
                    progressLoading.setVisibility(View.GONE);

                    if (hotels.isEmpty()) {
                        textSummary.setText("Bạn chưa đăng khách sạn nào.");
                    } else {
                        textSummary.setText("Đang hiển thị " + hotels.size() + " khách sạn của bạn");
                    }
                })
                .addOnFailureListener(error -> {
                    progressLoading.setVisibility(View.GONE);
                    textSummary.setText("Không tải được khách sạn");
                    Toast.makeText(this, "Không tải được khách sạn: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
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
        Intent intent = new Intent(this, PartnerAddRoomActivity.class);
        intent.putExtra("EXTRA_HOTEL_ID", hotel.getId());
        intent.putExtra("hotel_name", hotel.getHotelName());
        startActivity(intent);
    }

    private String hotelNameForSort(DocumentSnapshot document) {
        String name = document.getString("hotel_name");
        if (name == null || name.trim().isEmpty()) {
            name = document.getString("name");
        }
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}

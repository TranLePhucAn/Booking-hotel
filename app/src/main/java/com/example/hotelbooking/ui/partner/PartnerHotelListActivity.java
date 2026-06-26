package com.example.hotelbooking.ui.partner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.ui.adapter.PartnerHotelAdapter;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PartnerHotelListActivity extends AppCompatActivity {

    private PartnerHotelAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView rvHotels;
    private FirebaseFirestore db;
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_hotel_list);

        ownerId = FirebaseAuth.getInstance().getUid();
        if (ownerId == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        ImageView btnBack = findViewById(R.id.btnBackHotelList);
        progressBar = findViewById(R.id.progressHotelList);
        tvEmpty = findViewById(R.id.tvEmptyHotelList);
        rvHotels = findViewById(R.id.rvMyHotels);
        Button btnAddHotel = findViewById(R.id.btnAddHotelFromList);

        rvHotels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PartnerHotelAdapter();
        rvHotels.setAdapter(adapter);

        adapter.setListener(new PartnerHotelAdapter.OnHotelActionListener() {
            @Override
            public void onEdit(Hotel hotel) {
                // Mở màn chỉnh sửa khách sạn (Phúc An phụ trách)
                Toast.makeText(PartnerHotelListActivity.this,
                        "Chức năng sửa khách sạn: " + hotel.getHotelName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onManageRooms(Hotel hotel) {
                // Mở màn quản lý phòng
                Intent intent = new Intent(PartnerHotelListActivity.this, AddRoomActivity.class);
                intent.putExtra("hotel_id", hotel.getId());
                intent.putExtra("hotel_name", hotel.getHotelName());
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnAddHotel.setOnClickListener(v ->
                startActivity(new Intent(this, AddHotelActivity.class)));

        loadMyHotels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyHotels();
    }

    private void loadMyHotels() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvHotels.setVisibility(View.GONE);

        db.collection(AppConstants.COLLECTION_HOTELS)
                .whereEqualTo("owner_id", ownerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    List<Hotel> hotels = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Hotel hotel = Hotel.fromDocument(doc);
                            if (hotel != null) {
                                hotels.add(hotel);
                            }
                        }
                    }
                    if (hotels.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvHotels.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvHotels.setVisibility(View.VISIBLE);
                        adapter.setList(hotels);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }
}

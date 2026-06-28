package com.example.hotelbooking.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.ui.adapter.AdminHotelManagementAdapter;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminHotelManagementActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private AdminHotelManagementAdapter adapter;
    private final List<DocumentSnapshot> allHotels = new ArrayList<>();

    private TextView textSummary;
    private ProgressBar progressLoading;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_hotel_management);

        firestore = FirebaseFirestore.getInstance();
        bindViews();
        setupRecyclerView();
        loadHotels();
    }

    private void bindViews() {
        TextView buttonBack = findViewById(R.id.buttonBack);
        Button buttonRefresh = findViewById(R.id.buttonRefresh);
        Button filterAll = findViewById(R.id.filterAll);
        Button filterPending = findViewById(R.id.filterPending);
        Button filterApproved = findViewById(R.id.filterApproved);
        Button filterRejected = findViewById(R.id.filterRejected);
        Button filterInactive = findViewById(R.id.filterInactive);

        textSummary = findViewById(R.id.textSummary);
        progressLoading = findViewById(R.id.progressLoading);

        buttonBack.setOnClickListener(v -> finish());
        buttonRefresh.setOnClickListener(v -> loadHotels());
        filterAll.setOnClickListener(v -> applyFilter("all"));
        filterPending.setOnClickListener(v -> applyFilter(AppConstants.STATUS_PENDING));
        filterApproved.setOnClickListener(v -> applyFilter(AppConstants.STATUS_APPROVED));
        filterRejected.setOnClickListener(v -> applyFilter(AppConstants.STATUS_REJECTED));
        filterInactive.setOnClickListener(v -> applyFilter("inactive"));
    }

    private void setupRecyclerView() {
        RecyclerView recyclerHotels = findViewById(R.id.recyclerHotels);
        recyclerHotels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminHotelManagementAdapter(new AdminHotelManagementAdapter.OnHotelActionListener() {
            @Override
            public void onPreview(DocumentSnapshot hotelDocument) {
                openHotelPreview(hotelDocument);
            }

            @Override
            public void onApprove(DocumentSnapshot hotelDocument) {
                confirmApproveHotel(hotelDocument);
            }

            @Override
            public void onReject(DocumentSnapshot hotelDocument) {
                showRejectDialog(hotelDocument);
            }

            @Override
            public void onToggleActive(DocumentSnapshot hotelDocument) {
                confirmToggleActive(hotelDocument);
            }
        });
        recyclerHotels.setAdapter(adapter);
    }

    private void loadHotels() {
        progressLoading.setVisibility(View.VISIBLE);
        textSummary.setText("Đang tải khách sạn...");

        firestore.collection(AppConstants.COLLECTION_HOTELS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allHotels.clear();
                    allHotels.addAll(querySnapshot.getDocuments());
                    allHotels.sort(Comparator.comparing(this::hotelNameForSort));
                    progressLoading.setVisibility(View.GONE);
                    applyFilter(currentFilter);
                })
                .addOnFailureListener(error -> {
                    progressLoading.setVisibility(View.GONE);
                    textSummary.setText("Không tải được danh sách khách sạn");
                    Toast.makeText(this, "Không tải được khách sạn: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        List<DocumentSnapshot> result = new ArrayList<>();

        for (DocumentSnapshot document : allHotels) {
            String approvalStatus = firstString(document, AppConstants.STATUS_PENDING, "approval_status", "status");
            boolean active = booleanValue(document, "is_active", false);

            boolean matches;
            if ("all".equals(filter)) {
                matches = true;
            } else if ("inactive".equals(filter)) {
                matches = !active;
            } else {
                matches = filter.equalsIgnoreCase(approvalStatus);
            }

            if (matches) {
                result.add(document);
            }
        }

        adapter.updateData(result);
        textSummary.setText("Đang hiển thị " + result.size() + "/" + allHotels.size() + " khách sạn");
    }

    private void openHotelPreview(DocumentSnapshot document) {
        Hotel hotel = Hotel.fromDocument(document);
        Intent intent = new Intent(this, HotelDetailActivity.class);
        intent.putExtra("hotel", hotel);
        intent.putExtra("hotel_id", document.getId());
        intent.putExtra("mode", "admin_preview");
        startActivity(intent);
    }

    private void confirmApproveHotel(DocumentSnapshot document) {
        String name = firstString(document, "khách sạn này", "hotel_name", "name");
        new AlertDialog.Builder(this)
                .setTitle("Duyệt khách sạn")
                .setMessage("Bạn muốn duyệt " + name + "? Khách sạn sẽ được bật hoạt động.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đồng ý", (dialog, which) -> updateHotelApproval(document.getId(), AppConstants.STATUS_APPROVED, true, ""))
                .show();
    }

    private void showRejectDialog(DocumentSnapshot document) {
        EditText noteInput = new EditText(this);
        noteInput.setHint("Lý do từ chối");
        noteInput.setMinLines(2);

        new AlertDialog.Builder(this)
                .setTitle("Từ chối khách sạn")
                .setView(noteInput)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    String note = noteInput.getText().toString().trim();
                    if (note.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập lý do từ chối", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateHotelApproval(document.getId(), AppConstants.STATUS_REJECTED, false, note);
                })
                .show();
    }

    private void confirmToggleActive(DocumentSnapshot document) {
        boolean active = booleanValue(document, "is_active", false);
        String approvalStatus = firstString(document, AppConstants.STATUS_PENDING, "approval_status", "status");

        if (!active && !AppConstants.STATUS_APPROVED.equalsIgnoreCase(approvalStatus)) {
            Toast.makeText(this, "Chỉ khách sạn đã duyệt mới được bật hoạt động", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = active ? "Tắt hoạt động" : "Bật hoạt động";
        String message = active
                ? "Khách sạn sẽ tạm thời không hiển thị cho người dùng."
                : "Khách sạn sẽ được hiển thị lại cho người dùng.";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đồng ý", (dialog, which) -> updateHotelActive(document.getId(), !active))
                .show();
    }

    private void updateHotelApproval(String hotelId, String approvalStatus, boolean active, String adminNote) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("approval_status", approvalStatus);
        updates.put("is_active", active);
        updates.put("admin_note", adminNote);
        updates.put("updated_at", Timestamp.now());

        firestore.collection(AppConstants.COLLECTION_HOTELS)
                .document(hotelId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật khách sạn", Toast.LENGTH_SHORT).show();
                    loadHotels();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Không cập nhật được: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateHotelActive(String hotelId, boolean active) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_active", active);
        updates.put("updated_at", Timestamp.now());

        firestore.collection(AppConstants.COLLECTION_HOTELS)
                .document(hotelId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, active ? "Đã bật hoạt động" : "Đã tắt hoạt động", Toast.LENGTH_SHORT).show();
                    loadHotels();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, "Không cập nhật được: " + error.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String hotelNameForSort(DocumentSnapshot document) {
        return firstString(document, "", "hotel_name", "name").toLowerCase(Locale.ROOT);
    }

    private String firstString(DocumentSnapshot document, String fallback, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return fallback;
    }

    private boolean booleanValue(DocumentSnapshot document, String field, boolean fallback) {
        Boolean value = document.getBoolean(field);
        return value == null ? fallback : value;
    }
}

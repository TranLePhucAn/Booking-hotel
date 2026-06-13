package com.example.hotelbooking.ui.owner;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout layoutPendingBusinesses;
    private LinearLayout layoutPendingHotels;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        layoutPendingBusinesses = findViewById(R.id.layoutPendingBusinesses);
        layoutPendingHotels = findViewById(R.id.layoutPendingHotels);
        Button btnRefreshAdmin = findViewById(R.id.btnRefreshAdmin);

        btnRefreshAdmin.setOnClickListener(v -> loadPendingData());
        loadPendingData();
    }

    private void loadPendingData() {
        loadPendingBusinesses();
        loadPendingHotels();
    }

    private void loadPendingBusinesses() {
        layoutPendingBusinesses.removeAllViews();
        db.collection("businesses")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        layoutPendingBusinesses.addView(createText("Khong co doanh nghiep cho duyet.", false));
                        return;
                    }
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        addBusinessReviewCard(doc);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong tai duoc businesses: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadPendingHotels() {
        layoutPendingHotels.removeAllViews();
        db.collection("hotels")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        layoutPendingHotels.addView(createText("Khong co bai khach san cho duyet.", false));
                        return;
                    }
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        addHotelReviewCard(doc);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong tai duoc hotels: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void addBusinessReviewCard(DocumentSnapshot doc) {
        LinearLayout box = createBox();
        box.addView(createText("ID: " + doc.getId(), true));
        box.addView(createText("Doanh nghiep: " + value(doc, "business_name"), true));
        box.addView(createText("Nguoi dai dien: " + value(doc, "owner_name"), false));
        box.addView(createText("Email: " + value(doc, "email"), false));
        box.addView(createText("Dien thoai: " + value(doc, "phone"), false));
        box.addView(createText("Dia chi: " + value(doc, "address"), false));

        EditText reason = createReasonInput();
        box.addView(reason);
        box.addView(createActionRow(
                () -> approveBusiness(doc.getId()),
                () -> rejectDocument("businesses", doc.getId(), reason.getText().toString().trim())
        ));
        layoutPendingBusinesses.addView(box);
    }

    private void addHotelReviewCard(DocumentSnapshot doc) {
        LinearLayout box = createBox();
        box.addView(createText("ID: " + doc.getId(), true));
        box.addView(createText("Khach san: " + value(doc, "hotel_name"), true));
        box.addView(createText("Mo ta: " + value(doc, "description"), false));
        box.addView(createText("Dia chi: " + value(doc, "address_text"), false));
        box.addView(createText("Gia tu: " + value(doc, "price_from"), false));
        box.addView(createText("Anh chinh: " + value(doc, "image_url"), false));
        box.addView(createText("Tien ich: " + value(doc, "amenities"), false));

        EditText reason = createReasonInput();
        box.addView(reason);
        box.addView(createActionRow(
                () -> updateDocumentStatus("hotels", doc.getId(), "active", ""),
                () -> rejectDocument("hotels", doc.getId(), reason.getText().toString().trim())
        ));
        layoutPendingHotels.addView(box);
    }

    private void approveBusiness(String businessId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "approved");
        updates.put("updated_at", System.currentTimeMillis());

        db.collection("businesses").document(businessId).update(updates)
                .addOnSuccessListener(unused -> db.collection("users").document(businessId)
                        .update("partner_status", "approved")
                        .addOnSuccessListener(userUnused -> {
                            Toast.makeText(this, "Da duyet doanh nghiep", Toast.LENGTH_SHORT).show();
                            loadPendingBusinesses();
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong duyet duoc doanh nghiep: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void rejectDocument(String collection, String documentId, String reason) {
        if (reason.isEmpty()) {
            Toast.makeText(this, "Vui long nhap ly do tu choi", Toast.LENGTH_SHORT).show();
            return;
        }
        updateDocumentStatus(collection, documentId, "rejected", reason);
    }

    private void updateDocumentStatus(String collection, String documentId, String status, String reason) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("reject_reason", reason);
        updates.put("updated_at", System.currentTimeMillis());

        db.collection(collection).document(documentId).update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Da cap nhat " + collection + " = " + status, Toast.LENGTH_SHORT).show();
                    loadPendingData();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Khong cap nhat duoc: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private LinearLayout createBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(getColor(R.color.white));
        box.setPadding(16, 14, 16, 14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);
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

    private EditText createReasonInput() {
        EditText editText = new EditText(this);
        editText.setHint("Ly do tu choi neu co");
        editText.setMinLines(2);
        editText.setBackgroundResource(R.drawable.bg_input);
        editText.setPadding(12, 8, 12, 8);
        return editText;
    }

    private LinearLayout createActionRow(Runnable approveAction, Runnable rejectAction) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button approve = new Button(this);
        Button reject = new Button(this);
        approve.setText("Duyet");
        reject.setText("Tu choi");
        approve.setOnClickListener(v -> approveAction.run());
        reject.setOnClickListener(v -> rejectAction.run());

        row.addView(approve, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(reject, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private String value(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        return value == null ? "" : String.valueOf(value);
    }
}

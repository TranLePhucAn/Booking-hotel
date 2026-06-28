package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {
    private LinearLayout listNotifications;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String currentUserId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        listNotifications = findViewById(R.id.listNotifications);
        progressBar = findViewById(R.id.progressNotifications);

        View backButton = findViewById(R.id.btnBackNotifications);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        listNotifications.removeAllViews();

        db.collection(AppConstants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("user_id", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    documents.sort((first, second) -> Long.compare(notificationTime(second), notificationTime(first)));

                    if (documents.isEmpty()) {
                        addEmptyView();
                        return;
                    }

                    for (DocumentSnapshot document : documents) {
                        addNotificationView(document);
                        if (!Boolean.TRUE.equals(document.getBoolean("read"))) {
                            document.getReference().update("read", true);
                        }
                    }
                })
                .addOnFailureListener(error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Không tải được thông báo: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addNotificationView(DocumentSnapshot document) {
        TextView item = new TextView(this);
        String title = valueOrDefault(document.getString("title"), "Thông báo");
        String message = valueOrDefault(document.getString("message"), "");
        String time = formatTime(notificationTime(document));
        boolean read = Boolean.TRUE.equals(document.getBoolean("read"));

        item.setText(title + "\n" + message + "\n" + time);
        item.setTextSize(15);
        item.setTextColor(getColor(R.color.text_primary));
        item.setPadding(dp(14), dp(12), dp(14), dp(12));
        item.setBackgroundResource(R.drawable.bg_input);
        item.setAlpha(read ? 0.72f : 1f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        listNotifications.addView(item, params);
    }

    private void addEmptyView() {
        TextView empty = new TextView(this);
        empty.setText("Chưa có thông báo nào");
        empty.setTextColor(getColor(R.color.text_secondary));
        empty.setTextSize(16);
        empty.setPadding(dp(16), dp(28), dp(16), dp(28));
        listNotifications.addView(empty);
    }

    private long notificationTime(DocumentSnapshot document) {
        Object value = document.get("time");
        if (value instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) value).toDate().getTime();
        }
        return 0;
    }

    private String formatTime(long millis) {
        return millis <= 0 ? "" : dateFormat.format(new Date(millis));
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

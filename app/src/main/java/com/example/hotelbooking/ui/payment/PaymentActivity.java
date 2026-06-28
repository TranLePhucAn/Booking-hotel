package com.example.hotelbooking.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.ui.home.HomeActivity;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {
    private String reservationId;
    private double totalPrice;

    private TextView tvHotelName, tvRatingScore, tvReviewCount, tvOldTotalPrice, tvTotalPrice;
    private RatingBar ratingBar;
    private RadioGroup rgPaymentMethods;
    private RadioButton rbCreditCard, rbDigitalPayment;
    private LinearLayout layoutCreditCardInfo, layoutDigitalPaymentInfo;
    private EditText etCardName, etCardNumber, etExpiryDate, etCvv;
    private CheckBox cbAgreeTerms;
    private Button btnBooking;
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        receiveIntentData();
        setupPaymentSelectionLogic();
        setupSubmitPaymentLogic();
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            reservationId = intent.getStringExtra("RESERVATION_ID");
            totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0);
            hotel = (Hotel) intent.getSerializableExtra("EXTRA_HOTEL");

            if(hotel != null) {
                double reviewScore = hotel.getReviewScore();
                int reviewCount = hotel.getReviewCount();
                String scoreText = reviewScore > 0 ? formatNumber(reviewScore) + "/10" : "Chưa có điểm";
                String reviewText = reviewCount > 0 ? reviewCount + " đánh giá" : "Chưa có đánh giá";

                tvHotelName.setText(hotel.getHotelName());
                ratingBar.setRating((float) hotel.getRatingStar());
                tvRatingScore.setText(scoreText);
                tvReviewCount.setText(reviewText);
            }

            tvTotalPrice.setText(formatVND(totalPrice));
        }
    }

    private String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private void initViews() {
        tvHotelName = findViewById(R.id.textView);
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingScore = findViewById(R.id.textView2);
        tvReviewCount = findViewById(R.id.textView3);

        tvOldTotalPrice = findViewById(R.id.tv_old_total_price);
        tvTotalPrice = findViewById(R.id.tv_total_price);

        rgPaymentMethods = findViewById(R.id.rg_payment_methods);
        rbCreditCard = findViewById(R.id.rb_credit_card);
        rbDigitalPayment = findViewById(R.id.rb_digital_payment);

        layoutCreditCardInfo = findViewById(R.id.layout_credit_card_info);
        layoutDigitalPaymentInfo = findViewById(R.id.layout_digital_payment_info);

        etCardName = findViewById(R.id.et_card_name);
        etCardNumber = findViewById(R.id.et_card_number);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etCvv = findViewById(R.id.et_cvv);

        cbAgreeTerms = findViewById(R.id.cb_agree_terms);
        btnBooking = findViewById(R.id.btn_booking);
    }

    private void setupPaymentSelectionLogic() {
        rgPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_credit_card) {
                layoutCreditCardInfo.setVisibility(View.VISIBLE);
                layoutDigitalPaymentInfo.setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_digital_payment) {
                layoutCreditCardInfo.setVisibility(View.GONE);
                layoutDigitalPaymentInfo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSubmitPaymentLogic() {
        btnBooking.setOnClickListener(view -> {
            if(!cbAgreeTerms.isChecked()) {
                Toast.makeText(this, "Vui lòng đọc và đồng ý với điều khoản dịch vụ!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedMethodId = rgPaymentMethods.getCheckedRadioButtonId();

            if(selectedMethodId == R.id.rb_credit_card) {
                if (validateCardFields()) {
                    processFirebasePaymentUpdate("CREDIT_CARD");
                }
            } else if(selectedMethodId == R.id.rb_digital_payment) {
                showQRDialog();
            }
        });
    }

    private void showQRDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.activity_payment_qr, null);
        builder.setView(dialogView);

        ImageView imgQrCode = dialogView.findViewById(R.id.img_qr_code);
        Button btnPaidConfirm = dialogView.findViewById(R.id.btn_paid_confirm);

        String bankId = "MB"; // Mã định danh ngân hàng
        String accountNo = "0342689642"; // Số tài khoản ngân hàng thật
        String accountName = "NGUYEN THI HONG HANH"; // Tên chủ tài khoản

        String description = "Thanh toán mã phòng " + reservationId;

        String qrUrl = "https://img.vietqr.io/image/" + bankId + "-" + accountNo + "-compact.jpg"
                + "?amount=" + (int) totalPrice
                + "&addInfo=" + description.replace(" ", "%20")
                + "&accountName=" + accountName.replace(" ", "%20");

        Glide.with(this).load(qrUrl).into(imgQrCode);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnPaidConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            processFirebasePaymentUpdate("QR_CODE");
        });

        dialog.show();
    }

    private void processFirebasePaymentUpdate(String paymentMethod) {
        // todo: thanh toán chuyển trạng thái thủ công
        btnBooking.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.COLLECTION_RESERVATIONS).document(reservationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()) {
                        Timestamp paymentDeadline = documentSnapshot.getTimestamp("payment_deadline");
                        Timestamp now = Timestamp.now();

                        if(paymentDeadline != null && now.compareTo(paymentDeadline) > 0) {
                            db.collection(AppConstants.COLLECTION_RESERVATIONS).document(reservationId)
                                    .update("status", AppConstants.BOOKING_EXPIRED)
                                    .addOnCompleteListener(task -> {
                                        Toast.makeText(PaymentActivity.this,
                                                "Đơn đặt phòng của bạn đã hết hạn 20 phút giữ phòng! Vui lòng đặt lại.",
                                                Toast.LENGTH_LONG).show();

                                        Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                            return;
                        }

                        String roomId = documentSnapshot.getString("room_id");
                        String sectionId = documentSnapshot.getString("section_id");
                        long roomQuantity = 1;
                        Object roomQuantityValue = documentSnapshot.get("room_quantity");
                        if (roomQuantityValue instanceof Number) {
                            roomQuantity = Math.max(1, ((Number) roomQuantityValue).longValue());
                        }

                        resolveRoomAndCompletePayment(db, roomId, sectionId, roomQuantity, now, paymentMethod);
                    } else {
                        btnBooking.setEnabled(true);
                        Toast.makeText(PaymentActivity.this, "Đơn đặt phòng không tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnBooking.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "Lỗi kết nối hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resolveRoomAndCompletePayment(FirebaseFirestore db, String roomId, String sectionId,
                                               long roomQuantity, Timestamp now, String paymentMethod) {
        if (roomId != null && !roomId.isEmpty()) {
            db.collection(AppConstants.COLLECTION_ROOMS).document(roomId).get()
                    .addOnSuccessListener(roomSnapshot -> {
                        if (roomSnapshot.exists()) {
                            completePaymentTransaction(db, roomId, sectionId, roomQuantity, now, paymentMethod);
                        } else {
                            resolveRoomBySectionAndCompletePayment(db, sectionId, roomQuantity, now, paymentMethod);
                        }
                    })
                    .addOnFailureListener(e -> {
                        btnBooking.setEnabled(true);
                        Toast.makeText(PaymentActivity.this, "Không kiểm tra được phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            return;
        }

        resolveRoomBySectionAndCompletePayment(db, sectionId, roomQuantity, now, paymentMethod);
    }

    private void resolveRoomBySectionAndCompletePayment(FirebaseFirestore db, String sectionId,
                                                        long roomQuantity, Timestamp now, String paymentMethod) {
        if (sectionId == null || sectionId.isEmpty()) {
            btnBooking.setEnabled(true);
            Toast.makeText(this, "Đơn chưa có thông tin phòng/hạng phòng. Vui lòng đặt lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(AppConstants.COLLECTION_ROOMS)
                .whereEqualTo("section_id", sectionId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        btnBooking.setEnabled(true);
                        Toast.makeText(this, "Không tìm thấy phòng theo hạng phòng. Vui lòng đặt lại.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String resolvedRoomId = querySnapshot.getDocuments().get(0).getId();
                    completePaymentTransaction(db, resolvedRoomId, sectionId, roomQuantity, now, paymentMethod);
                })
                .addOnFailureListener(e -> {
                    btnBooking.setEnabled(true);
                    Toast.makeText(this, "Không tìm được phòng theo hạng phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void completePaymentTransaction(FirebaseFirestore db, String roomId, String sectionId,
                                            long roomQuantity, Timestamp now, String paymentMethod) {
        final long quantityToReserve = roomQuantity;
        final String selectedRoomId = roomId;
        final String selectedSectionId = sectionId;

        db.runTransaction(transaction -> {
                    DocumentReference roomRef = db.collection(AppConstants.COLLECTION_ROOMS).document(selectedRoomId);
                    DocumentSnapshot roomSnapshot = transaction.get(roomRef);
                    DocumentReference inventoryRef = roomRef;
                    DocumentSnapshot inventorySnapshot = roomSnapshot;

                    if (selectedSectionId != null && !selectedSectionId.isEmpty()) {
                        DocumentReference sectionRef = db.collection(AppConstants.COLLECTION_SECTIONS).document(selectedSectionId);
                        DocumentSnapshot sectionSnapshot = transaction.get(sectionRef);
                        if (sectionSnapshot.exists() && sectionSnapshot.contains("available_rooms")) {
                            inventoryRef = sectionRef;
                            inventorySnapshot = sectionSnapshot;
                        }
                    }

                    if (!inventorySnapshot.exists()) {
                        throw new RuntimeException("Không tìm thấy phòng trong hệ thống. Vui lòng đặt lại.");
                    }

                    long availableRooms;
                    if (inventorySnapshot.contains("available_rooms")) {
                        Long value = inventorySnapshot.getLong("available_rooms");
                        availableRooms = value == null ? 0 : value;
                    } else {
                        throw new RuntimeException("Phòng chưa có thông tin số lượng còn trống.");
                    }

                    if (availableRooms < quantityToReserve) {
                        throw new RuntimeException("Rất tiếc, loại phòng này chỉ còn "
                                + availableRooms + " phòng trống.");
                    }

                    transaction.update(inventoryRef, "available_rooms", availableRooms - quantityToReserve);

                    DocumentReference reservationRef = db.collection(AppConstants.COLLECTION_RESERVATIONS).document(reservationId);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", AppConstants.BOOKING_CONFIRMED);
                    updates.put("payment_status", AppConstants.PAYMENT_PAID);
                    updates.put("payment_method", paymentMethod);
                    updates.put("paid_at", now);
                    updates.put("room_id", selectedRoomId);
                    transaction.update(reservationRef, updates);

                    return null;
                })
                .addOnSuccessListener(runnable -> {
                    Toast.makeText(PaymentActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnBooking.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "Lỗi cập nhật thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateCardFields() {
        String name = etCardName.getText().toString().trim();
        String number = etCardNumber.getText().toString().trim();
        String expiry = etExpiryDate.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();
        if (name.isEmpty()) { etCardName.setError("Nhập tên chủ thẻ"); return false; }
        if (number.length() < 16) { etCardNumber.setError("Số thẻ phải gồm 16 chữ số"); return false; }
        if (expiry.isEmpty() || !expiry.contains("/")) { etExpiryDate.setError("Định dạng MM/YY"); return false; }
        if (cvv.length() < 3) { etCvv.setError("Mã CVV gồm 3 số"); return false; }
        return true;
    }

    private String formatVND(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(amount) + " đ";
    }
}

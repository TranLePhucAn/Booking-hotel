package com.example.hotelbooking.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.model.Section;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHotelName, tvRoomStyle, tvBasePrice, tvTaxPrice, tvTotalPrice, tvOldTotalPrice;
    private EditText etPromoCode, etGuestName, etGuestPhone, etGuestEmail;
    private Button btnApplyPromo, btnConfirmBooking;
    private Hotel hotel;
    private Section section;
    private double finalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        initViews();
        receiveDataAndDisplay();
    }

    private void initViews() {
        tvHotelName = findViewById(R.id.textView); // ID của Tên khách sạn
        tvRoomStyle = findViewById(R.id.textView5); // ID của Tên hạng phòng
        tvBasePrice = findViewById(R.id.tv_base_price);
        tvTaxPrice = findViewById(R.id.tv_tax_price);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvOldTotalPrice = findViewById(R.id.tv_old_total_price);

        etPromoCode = findViewById(R.id.et_promo_code);
        etGuestName = findViewById(R.id.et_guest_name);
        etGuestPhone = findViewById(R.id.et_guest_phone);
        etGuestEmail = findViewById(R.id.et_guest_email);

        btnApplyPromo = findViewById(R.id.btn_apply_promo);
        btnConfirmBooking = findViewById(R.id.btn_confirm_booking);
    }

    private void receiveDataAndDisplay() {
        Intent intent = getIntent();
        if(intent != null) {
            hotel = (Hotel) intent.getSerializableExtra("EXTRA_HOTEL");
            section = (Section) intent.getSerializableExtra("EXTRA_SECTION");
            if(hotel != null && section != null) {
                tvHotelName.setText(hotel.getHotelName());
                tvRoomStyle.setText("(1x) " + section.getRoomStyle());

                double basePrice = section.getBasePrice();
                double taxPrice = basePrice * 0.1; // thuế 10%
                finalPrice = basePrice + taxPrice;

                tvBasePrice.setText(formatVND(basePrice));
                tvTaxPrice.setText(formatVND(taxPrice));
                tvTotalPrice.setText(formatVND(finalPrice));
            }
        }
    }

    // Xử lý nút áp dụng mã giảm giá
    private void setupPromoLogic() {
        // todo
    }

    // xử lý nút xác nhận đặt phòng
    private void setupBookingLogic() {
        btnConfirmBooking.setOnClickListener(view -> {
            String name = etGuestName.getText().toString().trim();
            String phone = etGuestPhone.getText().toString().trim();
            String email = etGuestEmail.getText().toString().trim();

            // kiểm tra dl
            if (name.isEmpty()) { etGuestName.setError("Vui lòng nhập tên"); return; }
            if (phone.isEmpty()) { etGuestPhone.setError("Vui lòng nhập số điện thoại"); return; }
            if (email.isEmpty()) { etGuestEmail.setError("Vui lòng nhập Email"); return; }

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MINUTE, 20);
            Date deadline = calendar.getTime();

            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("hotel_id", hotel.getId()); // Lấy Document ID của khách sạn
            reservationData.put("section_id", section.getId()); // Lấy Document ID của hạng phòng
//            reservationData.put("customer_id", realUserId);

            reservationData.put("guest_name", name);
            reservationData.put("guest_phone", phone);
            reservationData.put("guest_email", email);

            reservationData.put("base_price", section.getBasePrice());
            reservationData.put("tax_fee", section.getBasePrice() * 0.1);
//            reservationData.put("discount_price", tvOldTotalPrice.getVisibility() == View.VISIBLE ? discountValue : 0);
            reservationData.put("total_price", finalPrice);

            reservationData.put("status", "PENDING"); // chờ thanh toán
            reservationData.put("created_at", new Timestamp(now));
            reservationData.put("payment_deadline", new Timestamp(deadline)); // deadline thanh toán
            reservationData.put("room_id", ""); // Chưa xếp phòng vật lý cụ thể khi chưa trả tiền

            btnConfirmBooking.setEnabled(false);
            firestore.collection("reservations")
                    .add(reservationData)
                    .addOnSuccessListener(documentReference -> {
                        String reservationId = documentReference.getId();
                        Toast.makeText(ConfirmActivity.this, "Giữ phòng thành công! Đang chuyển tới trang thanh toán.", Toast.LENGTH_SHORT).show();

                        Intent intentPayment = new Intent(ConfirmActivity.this, PaymentActivity.class);
                        intentPayment.putExtra("RESERVATION_ID", reservationId);
                        intentPayment.putExtra("TOTAL_PRICE", finalPrice);
                        startActivity(intentPayment);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnConfirmBooking.setEnabled(true);
                        Toast.makeText(ConfirmActivity.this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private String formatVND(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#, ###");
        return decimalFormat.format(amount) + " đ";
    }
}

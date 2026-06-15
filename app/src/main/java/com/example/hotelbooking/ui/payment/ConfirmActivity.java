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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private Date checkInDate;
    private Date checkOutDate;
    private int numberOfNights = 1; // mặc định là 1 đêm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        initViews();
        receiveDataAndDisplay();

        checkUserLoginAndPrefill();

        setupPromoLogic();
        setupBookingLogic();
    }

    private void checkUserLoginAndPrefill() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            String phoneNumber = currentUser.getPhoneNumber();

            if (displayName != null && !displayName.isEmpty()) {
                etGuestName.setText(displayName);
            }

            if (email != null && !email.isEmpty()) {
                etGuestEmail.setText(email);
            }

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                etGuestPhone.setText(phoneNumber);
            }

            // reservationData.put("customer_id", currentUser.getUid());

        } else {
            /* Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(ConfirmActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            */
        }
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

            // Giả định nhận thêm ngày check-in/out từ bộ lọc tìm kiếm màn hình trước
            // nếu không có thì lấy ngày hôm nay và ngày mai làm mặc định mẫu
            long checkInMillis = intent.getLongExtra("EXTRA_CHECK_IN", System.currentTimeMillis());
            long checkOutMillis = intent.getLongExtra("EXTRA_CHECK_OUT", System.currentTimeMillis() + 86400000);
            checkInDate = new Date(checkInMillis);
            checkOutDate = new Date(checkOutMillis);

            // Tính số đêm (Đơn giản)
            long diff = checkOutMillis - checkInMillis;
            if (diff > 0) {
                numberOfNights = (int) (diff / (1000 * 60 * 60 * 24));
                if (numberOfNights == 0) numberOfNights = 1;
            }

            if(hotel != null && section != null) {
                tvHotelName.setText(hotel.getHotelName());
                tvRoomStyle.setText("(1x) " + section.getRoomStyle());

                double basePrice = section.getBasePrice() * numberOfNights;
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
        btnApplyPromo.setOnClickListener(view -> {
            String promoCode = etPromoCode.getText().toString().trim();
            // Todo: Code xử lý Firebase kiểm tra mã giảm giá của bạn ở đây
            Toast.makeText(this, "Mã giảm giá không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
        });
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
            calendar.add(Calendar.MINUTE, 20); // Giữ phòng trong 20 phút
            Date deadline = calendar.getTime();

            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("hotel_id", hotel.getId()); // Lấy Document ID của khách sạn
            reservationData.put("section_id", section.getId()); // Lấy Document ID của hạng phòng
//            reservationData.put("customer_id", realUserId);

            reservationData.put("check_in", new Timestamp(checkInDate));
            reservationData.put("check_out", new Timestamp(checkOutDate));
            reservationData.put("number_of_nights", numberOfNights);

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
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(amount) + " đ";
    }
}

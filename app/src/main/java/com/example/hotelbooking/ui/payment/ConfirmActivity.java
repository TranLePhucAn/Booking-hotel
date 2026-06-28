package com.example.hotelbooking.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.data.model.Section;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHotelName, tvRoomStyle, tvBasePrice, tvTaxPrice, tvTotalPrice, tvOldTotalPrice,
            tvAvailableRooms, tvDateFrom, tvDateEnd, tvNumberOfNights, tvCheckInTime, tvCheckOutTime;
    private TextView tvRatingScore, tvReviewCount, tvBasePriceLabel;
    private RatingBar ratingBar;

    private EditText etPromoCode, etGuestName, etGuestPhone, etGuestEmail;
    private Button btnApplyPromo, btnConfirmBooking;
    private Hotel hotel;
    private Section section;
    private double finalPrice;
    private double discountValue = 0;

    private Date checkInDate;
    private Date checkOutDate;
    private int numberOfNights = 1; // mặc định là 1 đêm
    private int availableRooms;
    private int roomQuantity = 1;
    private String selectedRoomId;
    private String selectedOwnerId;
    private String checkInText, checkOutText, checkInTimeText, checkOutTimeText;

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
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingScore = findViewById(R.id.textView2);
        tvReviewCount = findViewById(R.id.textView3);
        tvRoomStyle = findViewById(R.id.textView5); // ID của Tên hạng phòng
        tvBasePriceLabel = findViewById(R.id.tv_base_price_label);
        tvBasePrice = findViewById(R.id.tv_base_price);
        tvTaxPrice = findViewById(R.id.tv_tax_price);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvOldTotalPrice = findViewById(R.id.tv_old_total_price);
        tvAvailableRooms = findViewById(R.id.textView6);
        tvDateFrom = findViewById(R.id.textView8);
        tvCheckInTime = findViewById(R.id.textView9);
        tvNumberOfNights = findViewById(R.id.textView10);
        tvDateEnd = findViewById(R.id.textView12);
        tvCheckOutTime = findViewById(R.id.textView13);

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
            availableRooms = intent.getIntExtra("EXTRA_AVAILABLE_ROOMS", 1);
            roomQuantity = Math.max(1, intent.getIntExtra("EXTRA_ROOM_QUANTITY",
                    intent.getIntExtra("room_quantity", 1)));
            if (availableRooms > 0 && roomQuantity > availableRooms) {
                roomQuantity = availableRooms;
            }
            selectedRoomId = intent.getStringExtra("room_id");
            selectedOwnerId = intent.getStringExtra("owner_id");

            // Giả định nhận thêm ngày check-in/out từ bộ lọc tìm kiếm màn hình trước
            // nếu không có thì lấy ngày hôm nay và ngày mai làm mặc định mẫu
            long checkInMillis = intent.getLongExtra("EXTRA_CHECK_IN", System.currentTimeMillis());
            long checkOutMillis = intent.getLongExtra("EXTRA_CHECK_OUT", System.currentTimeMillis() + 86400000);

            checkInDate = new Date(checkInMillis);
            checkOutDate = new Date(checkOutMillis);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, d 'thg' M", new java.util.Locale("vi", "VN"));

            // ngày
            checkInText = sdf.format(checkInDate);
            checkOutText = sdf.format(checkOutDate);

            // giờ
            java.text.SimpleDateFormat sdfTime = new java.text.SimpleDateFormat("HH:mm", new java.util.Locale("vi", "VN"));
            checkInTimeText = sdfTime.format(checkInDate);
            checkOutTimeText = sdfTime.format(checkOutDate);

            numberOfNights = calculateNumberOfNights(checkInMillis, checkOutMillis);

            if(hotel != null && section != null) {
                double reviewScore = hotel.getReviewScore();
                int reviewCount = hotel.getReviewCount();
                String scoreText = reviewScore > 0 ? formatNumber(reviewScore) + "/10" : "Chưa có điểm";
                String reviewText = reviewCount > 0 ? reviewCount + " đánh giá" : "Chưa có đánh giá";

                tvHotelName.setText(hotel.getHotelName());
                ratingBar.setRating((float) hotel.getRatingStar());
                tvRatingScore.setText(scoreText);
                tvReviewCount.setText(reviewText);
                tvRoomStyle.setText("(" + roomQuantity + "x) " + section.getRoomStyle());
                tvAvailableRooms.setText("Chỉ còn " + availableRooms + " phòng");
                tvDateFrom.setText(checkInText);
                tvCheckInTime.setText("Từ " + checkInTimeText);
                tvNumberOfNights.setText(numberOfNights + " đêm");
                tvDateEnd.setText(checkOutText);
                tvCheckOutTime.setText("Đến " + checkOutTimeText);

                double basePrice = section.getBasePrice() * numberOfNights * roomQuantity;
                double taxPrice = basePrice * 0.1; // thuế 10%
                finalPrice = basePrice + taxPrice;

                if (tvBasePriceLabel != null) {
                    tvBasePriceLabel.setText("Giá phòng (" + roomQuantity + " phòng x " + numberOfNights + " đêm)");
                }
                tvBasePrice.setText(formatVND(basePrice));
                tvTaxPrice.setText(formatVND(taxPrice));
                tvTotalPrice.setText(formatVND(finalPrice));
            }
        }
    }

    private String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    // Xử lý nút áp dụng mã giảm giá
    private int calculateNumberOfNights(long checkInMillis, long checkOutMillis) {
        Calendar checkInCalendar = Calendar.getInstance();
        checkInCalendar.setTimeInMillis(checkInMillis);
        resetToStartOfDay(checkInCalendar);

        Calendar checkOutCalendar = Calendar.getInstance();
        checkOutCalendar.setTimeInMillis(checkOutMillis);
        resetToStartOfDay(checkOutCalendar);

        long diff = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        if (diff <= 0) {
            return 1;
        }
        return Math.max(1, (int) (diff / (1000 * 60 * 60 * 24)));
    }

    private void resetToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setupPromoLogic() {
        btnApplyPromo.setOnClickListener(view -> {
            String promoCode = etPromoCode.getText().toString().trim().toUpperCase(Locale.ROOT);
            applyPromoCode(promoCode);
        });
    }

    private void applyPromoCode(String promoCode) {
        if (section == null) {
            Toast.makeText(this, "Chưa có thông tin phòng để áp dụng mã", Toast.LENGTH_SHORT).show();
            return;
        }

        double basePrice = section.getBasePrice() * numberOfNights * roomQuantity;
        double taxPrice = basePrice * 0.1;
        double originalTotal = basePrice + taxPrice;

        double discountRate;
        switch (promoCode) {
            case "STAYHUB10":
                discountRate = 0.10;
                break;
            case "SUMMER15":
                discountRate = 0.15;
                break;
            case "WELCOME5":
                discountRate = 0.05;
                break;
            default:
                discountValue = 0;
                finalPrice = originalTotal;
                tvOldTotalPrice.setVisibility(View.GONE);
                tvTotalPrice.setText(formatVND(finalPrice));
                Toast.makeText(this, "Mã giảm giá không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                return;
        }

        discountValue = originalTotal * discountRate;
        finalPrice = Math.max(0, originalTotal - discountValue);
        tvOldTotalPrice.setVisibility(View.VISIBLE);
        tvOldTotalPrice.setText("Giá gốc: " + formatVND(originalTotal));
        tvTotalPrice.setText(formatVND(finalPrice));
        Toast.makeText(this, "Đã áp dụng mã giảm giá", Toast.LENGTH_SHORT).show();
    }

    // xử lý nút xác nhận đặt phòng
    private void setupBookingLogic() {
        btnConfirmBooking.setOnClickListener(view -> {

            btnConfirmBooking.setEnabled(false);

            if (!canAcceptBooking()) {
                Toast.makeText(this, "Rất tiếc, khách sạn này hiện tại không tiếp nhận đặt phòng!", Toast.LENGTH_LONG).show();
                btnConfirmBooking.setEnabled(true);
                return;
            }

            String name = etGuestName.getText().toString().trim();
            String phone = etGuestPhone.getText().toString().trim();
            String email = etGuestEmail.getText().toString().trim();

            // kiểm tra dl
            if (name.isEmpty()) { etGuestName.setError("Vui lòng nhập tên"); return; }
            if (phone.isEmpty()) { etGuestPhone.setError("Vui lòng nhập số điện thoại"); return; }
            if (email.isEmpty()) { etGuestEmail.setError("Vui lòng nhập Email"); return; }

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            String customerId = "";
            if(firebaseUser != null) {
                customerId = firebaseAuth.getUid();
            } else {
                Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MINUTE, 20); // Giữ phòng trong 20 phút
            Date deadline = calendar.getTime();

            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("hotel_id", hotel.getId()); // Lấy Document ID của khách sạn
            reservationData.put("section_id", section.getId()); // Lấy Document ID của hạng phòng
            reservationData.put("customer_id", customerId);

            if (selectedOwnerId != null && !selectedOwnerId.isEmpty()) {
                reservationData.put("owner_id", selectedOwnerId);
            } else if (hotel != null && hotel.getOwnerId() != null) {
                reservationData.put("owner_id", hotel.getOwnerId());
            } else {
                reservationData.put("owner_id", "");
            }

            reservationData.put("day_start", new Timestamp(checkInDate));
            reservationData.put("day_end", new Timestamp(checkOutDate));
            reservationData.put("number_of_nights", numberOfNights);
            reservationData.put("room_quantity", roomQuantity);

            reservationData.put("guest_name", name);
            reservationData.put("guest_phone", phone);
            reservationData.put("guest_email", email);

            double basePrice = section.getBasePrice() * numberOfNights * roomQuantity;
            double taxPrice = basePrice * 0.1;
            reservationData.put("price_per_night", section.getBasePrice());
            reservationData.put("base_price", basePrice);
            reservationData.put("tax_fee", taxPrice);
            reservationData.put("discount_price", discountValue);
            reservationData.put("total_price", finalPrice);

            reservationData.put("status", AppConstants.BOOKING_PENDING_PAYMENT);
            reservationData.put("payment_status", AppConstants.PAYMENT_UNPAID);
            reservationData.put("created_at", new Timestamp(now));
            reservationData.put("payment_deadline", new Timestamp(deadline)); // deadline thanh toán
            reservationData.put("room_id", selectedRoomId == null ? "" : selectedRoomId);

            firestore.collection(AppConstants.COLLECTION_RESERVATIONS)
                    .add(reservationData)
                    .addOnSuccessListener(documentReference -> {
                        String reservationId = documentReference.getId();
                        Toast.makeText(ConfirmActivity.this, "Giữ phòng thành công! Đang chuyển tới trang thanh toán.", Toast.LENGTH_SHORT).show();

                        Intent intentPayment = new Intent(ConfirmActivity.this, PaymentActivity.class);
                        intentPayment.putExtra("RESERVATION_ID", reservationId);
                        intentPayment.putExtra("TOTAL_PRICE", finalPrice);
                        intentPayment.putExtra("EXTRA_HOTEL", hotel);
                        startActivity(intentPayment);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnConfirmBooking.setEnabled(true);
                        Toast.makeText(ConfirmActivity.this, "Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private boolean canAcceptBooking() {
        if (hotel == null) {
            return false;
        }
        String status = hotel.getStatus();
        String approvalStatus = hotel.getApprovalStatus();
        return hotel.isActive()
                || AppConstants.STATUS_APPROVED.equalsIgnoreCase(valueOrEmpty(status))
                || AppConstants.STATUS_APPROVED.equalsIgnoreCase(valueOrEmpty(approvalStatus))
                || "active".equalsIgnoreCase(valueOrEmpty(status));
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatVND(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(amount) + " đ";
    }
}

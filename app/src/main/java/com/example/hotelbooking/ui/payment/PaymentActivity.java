package com.example.hotelbooking.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;

import java.text.DecimalFormat;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        receiveIntentData();
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            reservationId = intent.getStringExtra("RESERVATION_ID");
            totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0);

            tvTotalPrice.setText(formatVND(totalPrice));
        }
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

    private String formatVND(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(amount) + " đ";
    }
}

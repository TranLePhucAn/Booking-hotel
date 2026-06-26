package com.example.hotelbooking.ui.home;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private final List<DocumentSnapshot> bookings = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void updateData(List<DocumentSnapshot> newBookings) {
        bookings.clear();
        bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot booking = bookings.get(position);

        String docId = booking.getId();
        String status = stringValue(booking, "status", "BOOKED").toUpperCase();
        double totalPrice = doubleValue(booking, "total_price", 0);
        String guestName = stringValue(booking, "guest_name", "Khách hàng");

        Date dayStart = booking.getDate("check_in");
        Date dayEnd = booking.getDate("check_out");

        final String checkInStr = (dayStart != null) ? dateFormat.format(dayStart) : "N/A";
        final String checkOutStr = (dayEnd != null) ? dateFormat.format(dayEnd) : "N/A";

        holder.tvDate.setText("📅 " + checkInStr + " - " + checkOutStr);
        holder.tvPrice.setText(formatMoney(totalPrice));
        holder.tvStatus.setText(status);

        if (holder.tvBookingId != null) {
            String displayId = docId.length() > 6 ? docId.substring(0, 6) : docId;
            holder.tvBookingId.setText("Mã: #" + displayId);
        }

        if ("PAID".equals(status) || "CONFIRMED".equals(status) || "BOOKED".equals(status)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#007BFF"));
        } else if ("COMPLETED".equals(status)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#28A745"));
        } else if ("CANCELLED".equals(status)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#DC3545"));
        }

        String hotelId = stringValue(booking, "hotel_id", "");
        String roomId = stringValue(booking, "room_id", "");

        holder.tvHotelName.setText("Đang tải tên khách sạn...");
        final String[] hotelDetails = {"Khách sạn chưa rõ", ""};
        final String[] roomDetails = {"Phòng chưa rõ"};

        if (!hotelId.isEmpty()) {
            db.collection("hotels").document(hotelId).get().addOnSuccessListener(hotelDoc -> {
                if (hotelDoc.exists()) {
                    hotelDetails[0] = stringValue(hotelDoc, "name", "Tên khách sạn");

                    String imgUrl = hotelDoc.getString("image_url");
                    if (imgUrl == null) {
                        imgUrl = hotelDoc.getString("imageUrl");
                    }
                    hotelDetails[1] = (imgUrl != null) ? imgUrl : "";

                    holder.tvHotelName.setText(hotelDetails[0]);

                    if (!hotelDetails[1].isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(hotelDetails[1])
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .into(holder.imgThumbnail);
                    }
                }
            }).addOnFailureListener(e -> Log.e("Adapter", "Lỗi tải Hotel: " + e.getMessage()));
        }

        if (!roomId.isEmpty()) {
            db.collection("rooms").document(roomId).get().addOnSuccessListener(roomDoc -> {
                if (roomDoc.exists()) {
                    roomDetails[0] = stringValue(roomDoc, "name", "Tên phòng");
                }
            });
        }

        holder.btnViewDetail.setOnClickListener(v -> {
            android.content.Context context = holder.itemView.getContext();
            Dialog dialog = new Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.item_detail_booking_history);

            ImageButton btnBack = dialog.findViewById(R.id.btnBack);
            TextView tvDetailBookingId = dialog.findViewById(R.id.tvDetailBookingId);
            TextView tvDetailStatus = dialog.findViewById(R.id.tvDetailStatus);
            ShapeableImageView ivDetailRoomImage = dialog.findViewById(R.id.ivDetailRoomImage);
            TextView tvDetailHotelName = dialog.findViewById(R.id.tvDetailHotelName);
            TextView tvDetailCheckIn = dialog.findViewById(R.id.tvDetailCheckIn);
            TextView tvDetailCheckOut = dialog.findViewById(R.id.tvDetailCheckOut);
            TextView tvDetailTotalPrice = dialog.findViewById(R.id.tvDetailTotalPrice);
            TextView tvDetailGuestName = dialog.findViewById(R.id.tvDetailGuestName);

            Button btnCancelReservation = dialog.findViewById(R.id.btnCancelReservation);
            Button btnReviewReservation = dialog.findViewById(R.id.btnReviewReservation);

            if (tvDetailBookingId != null) tvDetailBookingId.setText("Mã đơn hàng: #" + docId);
            if (tvDetailStatus != null) tvDetailStatus.setText(status);
            if (tvDetailCheckIn != null) tvDetailCheckIn.setText("📅 Nhận phòng: " + checkInStr);
            if (tvDetailCheckOut != null) tvDetailCheckOut.setText("📅 Trả phòng: " + checkOutStr);
            if (tvDetailTotalPrice != null) tvDetailTotalPrice.setText(formatMoney(totalPrice));
            if (tvDetailGuestName != null) tvDetailGuestName.setText("Khách đặt: " + guestName);

            if (tvDetailHotelName != null) {
                tvDetailHotelName.setText(hotelDetails[0] + " - " + roomDetails[0]);
            }

            if (ivDetailRoomImage != null && !hotelDetails[1].isEmpty()) {
                Glide.with(context)
                        .load(hotelDetails[1])
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivDetailRoomImage);
            }

            if (btnCancelReservation != null && btnReviewReservation != null) {
                btnCancelReservation.setVisibility(View.GONE);
                btnReviewReservation.setVisibility(View.GONE);

                if ("PAID".equals(status) || "CONFIRMED".equals(status) || "BOOKED".equals(status) || "PENDING".equals(status)) {
                    btnCancelReservation.setVisibility(View.VISIBLE);
                } else if ("COMPLETED".equals(status)) {
                    btnReviewReservation.setVisibility(View.VISIBLE);
                }
            }

            if (btnBack != null) btnBack.setOnClickListener(view -> dialog.dismiss());

            if (btnCancelReservation != null) {
                btnCancelReservation.setOnClickListener(view -> {
                    booking.getReference().update("status", "CANCELLED")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã hủy đơn đặt phòng!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                });
            }

            if (btnReviewReservation != null) {
                btnReviewReservation.setOnClickListener(view -> {
                    Toast.makeText(context, "Tính năng đánh giá đang phát triển", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private String stringValue(DocumentSnapshot document, String field, String fallback) {
        String value = document.getString(field);
        return value == null ? fallback : value;
    }

    private double doubleValue(DocumentSnapshot document, String field, double fallback) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return fallback;
    }

    private String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0f đ", value);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgThumbnail;
        TextView tvHotelName;
        TextView tvStatus;
        TextView tvDate;
        TextView tvPrice;
        TextView tvBookingId;
        Button btnViewDetail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgHistoryThumbnail);
            tvHotelName = itemView.findViewById(R.id.tvHistoryHotelName);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvPrice = itemView.findViewById(R.id.tvHistoryPrice);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
    }
}
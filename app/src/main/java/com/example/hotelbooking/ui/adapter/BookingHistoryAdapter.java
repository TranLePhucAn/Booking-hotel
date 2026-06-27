package com.example.hotelbooking.ui.adapter;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Reservation;
import com.example.hotelbooking.utils.AppConstants;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private static final long CANCEL_LOCK_WINDOW_MS = 24L * 60L * 60L * 1000L;

    private final List<Reservation> bookings = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void updateData(List<Reservation> newBookings) {
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
        Reservation res = bookings.get(position);

        String docId = res.getId();
        String status = normalizeAndAutoComplete(res);
        String displayStatus = displayStatus(status);
        double totalPrice = res.getTotalPrice();
        String guestName = (res.getGuestName() != null ? res.getGuestName() : "Khách hàng");

        final String checkInStr = (res.getDayStart() != null) ? dateFormat.format(res.getDayStart().toDate()) : "N/A";
        final String checkOutStr = (res.getDayEnd() != null) ? dateFormat.format(res.getDayEnd().toDate()) : "N/A";

        holder.tvDate.setText(checkInStr + " - " + checkOutStr);
        holder.tvPrice.setText(formatMoney(totalPrice));
        holder.tvStatus.setText(displayStatus);

        if (holder.tvBookingId != null && docId != null) {
            String displayId = docId.length() > 6 ? docId.substring(0, 6) : docId;
            holder.tvBookingId.setText("Mã: #" + displayId);
        }

        // Cập nhật màu sắc trạng thái
        if ("PAID".equals(status) || "CONFIRMED".equals(status) || "BOOKED".equals(status)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#007BFF"));
        } else if ("COMPLETED".equals(status)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#28A745"));
        } else if ("CANCELLED".equals(status)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#DC3545"));
        }

        String hotelId = res.getHotelId();
        String roomId = res.getRoomId();

        holder.tvHotelName.setText("Đang tải tên khách sạn...");
        final String[] hotelDetails = {"Khách sạn chưa rõ", ""};
        final String[] roomDetails = {"Phòng chưa rõ"};

        if (hotelId != null && !hotelId.isEmpty()) {
            db.collection(AppConstants.COLLECTION_HOTELS).document(hotelId).get().addOnSuccessListener(hotelDoc -> {
                if (hotelDoc.exists()) {
                    hotelDetails[0] = hotelDoc.getString("hotel_name");
                    if (hotelDetails[0] == null) hotelDetails[0] = hotelDoc.getString("name");
                    
                    String imgUrl = hotelDoc.getString("image_url");
                    if (imgUrl == null) imgUrl = hotelDoc.getString("imageUrl");
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
            }).addOnFailureListener(e -> Log.e("Adapter", "Lỗi tải khách sạn: " + e.getMessage()));
        }

        if (roomId != null && !roomId.isEmpty()) {
            db.collection(AppConstants.COLLECTION_ROOMS).document(roomId).get().addOnSuccessListener(roomDoc -> {
                if (roomDoc.exists()) {
                    roomDetails[0] = roomDoc.getString("name");
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
            TextView tvDetailNights = dialog.findViewById(R.id.tvDetailNights);
            TextView tvDetailTotalPrice = dialog.findViewById(R.id.tvDetailTotalPrice);
            TextView tvDetailBasePrice = dialog.findViewById(R.id.tvDetailBasePrice);
            TextView tvDetailTaxFee = dialog.findViewById(R.id.tvDetailTaxFee);
            TextView tvDetailGuestName = dialog.findViewById(R.id.tvDetailGuestName);
            TextView tvDetailGuestPhone = dialog.findViewById(R.id.tvDetailGuestPhone);
            TextView tvDetailGuestEmail = dialog.findViewById(R.id.tvDetailGuestEmail);

            Button btnCancelReservation = dialog.findViewById(R.id.btnCancelReservation);
            Button btnReviewReservation = dialog.findViewById(R.id.btnReviewReservation);

            if (tvDetailBookingId != null) tvDetailBookingId.setText("Mã đơn hàng: #" + docId);
            if (tvDetailStatus != null) tvDetailStatus.setText(displayStatus);
            if (tvDetailCheckIn != null) tvDetailCheckIn.setText("Nhận phòng: " + checkInStr);
            if (tvDetailCheckOut != null) tvDetailCheckOut.setText("Trả phòng: " + checkOutStr);
            if (tvDetailTotalPrice != null) tvDetailTotalPrice.setText(formatMoney(totalPrice));
            if (tvDetailGuestName != null) tvDetailGuestName.setText("Khách đặt: " + guestName);

            if (tvDetailGuestPhone != null) tvDetailGuestPhone.setText("Số điện thoại: " + safeText(res.getGuestPhone(), "Chưa cập nhật"));
            if (tvDetailGuestEmail != null) tvDetailGuestEmail.setText("Email: " + safeText(res.getGuestEmail(), "Chưa cập nhật"));
            if (tvDetailBasePrice != null) tvDetailBasePrice.setText(formatMoney(res.getBasePrice()));
            if (tvDetailTaxFee != null) tvDetailTaxFee.setText(formatMoney(res.getTaxFee()));
            if (tvDetailNights != null) tvDetailNights.setText("Số đêm lưu trú: " + calculateNights(res) + " đêm");

            if (docId != null) {
                db.collection(AppConstants.COLLECTION_RESERVATIONS).document(docId).get()
                        .addOnSuccessListener(reservationDoc -> {
                            int roomQuantity = intValue(reservationDoc.get("room_quantity"), 1);
                            int numberOfNights = intValue(reservationDoc.get("number_of_nights"), calculateNights(res));
                            double pricePerNight = doubleValue(reservationDoc.get("price_per_night"), 0);
                            if (tvDetailNights != null) {
                                tvDetailNights.setText("Số đêm lưu trú: " + numberOfNights + " đêm - Số lượng phòng: " + roomQuantity);
                            }
                            if (tvDetailBasePrice != null) {
                                String baseLabel = pricePerNight > 0
                                        ? formatMoney(pricePerNight) + " x " + numberOfNights + " đêm x " + roomQuantity + " phòng = " + formatMoney(res.getBasePrice())
                                        : formatMoney(res.getBasePrice());
                                tvDetailBasePrice.setText(baseLabel);
                            }
                        });
            }

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

                if (canCurrentUserActOn(res) && canCancel(res, status)) {
                    btnCancelReservation.setVisibility(View.VISIBLE);
                } else if (canCurrentUserActOn(res) && "COMPLETED".equals(status)) {
                    btnReviewReservation.setVisibility(View.VISIBLE);
                }
            }

            if (btnBack != null) btnBack.setOnClickListener(view -> dialog.dismiss());

            if (btnCancelReservation != null && docId != null) {
                btnCancelReservation.setOnClickListener(view -> {
                    if (!canCancel(res, normalizeStatus(res.getStatus()))) {
                        Toast.makeText(context, "Đã quá thời hạn hủy phòng", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    showCancelPolicyDialog(context, res, docId, holder.getBindingAdapterPosition(), dialog);
                });
            }

            if (btnReviewReservation != null) {
                btnReviewReservation.setOnClickListener(view -> showReviewDialog(context, res, dialog));
            }

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                );
            }
        });

        bindCancelButton(holder, res, docId, status);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private String formatMoney(double value) {
        return String.format(Locale.getDefault(), "%,.0f đ", value);
    }

    private void bindCancelButton(ViewHolder holder, Reservation reservation, String reservationId, String status) {
        if (holder.btnViewDetail == null) {
            return;
        }
        if (reservationId == null || !canCurrentUserActOn(reservation) || !canCancel(reservation, status)) {
            holder.btnViewDetail.setVisibility(View.GONE);
            holder.btnViewDetail.setOnClickListener(null);
            return;
        }

        holder.btnViewDetail.setVisibility(View.VISIBLE);
        holder.btnViewDetail.setText("Hủy đặt phòng");
        holder.btnViewDetail.setOnClickListener(v ->
                showCancelPolicyDialog(
                        holder.itemView.getContext(),
                        reservation,
                        reservationId,
                        holder.getBindingAdapterPosition(),
                        null
                ));
    }

    private String displayStatus(String status) {
        if ("CANCELLED".equals(status)) {
            return "Đã hủy";
        }
        if ("COMPLETED".equals(status)) {
            return "Đã đi";
        }
        if ("EXPIRED".equals(status)) {
            return "Đã hết hạn";
        }
        if ("CONFIRMED".equals(status) || "PAID".equals(status) || "BOOKED".equals(status)) {
            return "Đã thanh toán";
        }
        if ("PENDING_PAYMENT".equals(status) || "PENDING".equals(status)) {
            return "Chờ thanh toán";
        }
        return status;
    }

    private void showCancelPolicyDialog(android.content.Context context, Reservation reservation,
                                        String reservationId, int adapterPosition, Dialog detailDialog) {
        double cancellationFee = reservation.getTotalPrice() * 0.35;
        double refundAmount = Math.max(0, reservation.getTotalPrice() - cancellationFee);
        String message = "Nếu hủy đơn, khách sẽ mất 35% số tiền đã thanh toán.\n"
                + "Phí hủy: "
                + formatMoney(cancellationFee)
                + ".\nSố tiền dự kiến hoàn lại: "
                + formatMoney(refundAmount)
                + ".\nHệ thống sẽ hoàn tiền sau 3 ngày.";

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận hủy đơn")
                .setMessage(message)
                .setNegativeButton("Không hủy", null)
                .setPositiveButton("Đồng ý hủy", (confirmDialog, which) ->
                        cancelReservation(context, reservation, reservationId, adapterPosition, detailDialog))
                .show();
    }

    private void cancelReservation(android.content.Context context, Reservation reservation,
                                   String reservationId, int adapterPosition, Dialog detailDialog) {
        db.collection(AppConstants.COLLECTION_RESERVATIONS)
                .document(reservationId)
                .update("status", AppConstants.BOOKING_CANCELLED)
                .addOnSuccessListener(aVoid -> {
                    reservation.setStatus(AppConstants.BOOKING_CANCELLED);
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(adapterPosition);
                    } else {
                        notifyDataSetChanged();
                    }
                    Toast.makeText(context, "Đã hủy đơn. Tiền hoàn sẽ được xử lý sau 3 ngày.", Toast.LENGTH_LONG).show();
                    if (detailDialog != null) {
                        detailDialog.dismiss();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Không hủy được đơn: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String normalizeAndAutoComplete(Reservation reservation) {
        String status = normalizeStatus(reservation.getStatus());
        if (shouldAutoComplete(reservation, status)) {
            reservation.setStatus(AppConstants.BOOKING_COMPLETED);
            if (reservation.getId() != null && !reservation.getId().isEmpty()) {
                db.collection(AppConstants.COLLECTION_RESERVATIONS)
                        .document(reservation.getId())
                        .update("status", AppConstants.BOOKING_COMPLETED)
                        .addOnFailureListener(e -> Log.e("Adapter", "Khong cap nhat COMPLETED: " + e.getMessage()));
            }
            return "COMPLETED";
        }
        return status;
    }

    private String normalizeStatus(String status) {
        return status == null || status.trim().isEmpty() ? "BOOKED" : status.trim().toUpperCase(Locale.ROOT);
    }

    private boolean shouldAutoComplete(Reservation reservation, String status) {
        if (!canAutoCompleteStatus(status) || reservation.getDayEnd() == null) {
            return false;
        }
        return System.currentTimeMillis() >= reservation.getDayEnd().toDate().getTime();
    }

    private boolean canCancel(Reservation reservation, String status) {
        if (!isActiveStatus(status) || reservation.getDayStart() == null) {
            return false;
        }
        long cancelDeadline = reservation.getDayStart().toDate().getTime() - CANCEL_LOCK_WINDOW_MS;
        return System.currentTimeMillis() < cancelDeadline;
    }

    private boolean isActiveStatus(String status) {
        return AppConstants.BOOKING_PENDING_PAYMENT.toUpperCase(Locale.ROOT).equals(status)
                || AppConstants.BOOKING_CONFIRMED.toUpperCase(Locale.ROOT).equals(status)
                || AppConstants.BOOKING_CHECKED_IN.toUpperCase(Locale.ROOT).equals(status)
                || "PAID".equals(status)
                || "CONFIRMED".equals(status)
                || "BOOKED".equals(status)
                || "PENDING".equals(status);
    }

    private boolean canAutoCompleteStatus(String status) {
        return AppConstants.BOOKING_CONFIRMED.toUpperCase(Locale.ROOT).equals(status)
                || AppConstants.BOOKING_CHECKED_IN.toUpperCase(Locale.ROOT).equals(status)
                || "PAID".equals(status)
                || "CONFIRMED".equals(status)
                || "BOOKED".equals(status);
    }

    private int calculateNights(Reservation reservation) {
        if (reservation.getDayStart() == null || reservation.getDayEnd() == null) {
            return 1;
        }
        long diff = reservation.getDayEnd().toDate().getTime() - reservation.getDayStart().toDate().getTime();
        return Math.max(1, (int) (diff / (24L * 60L * 60L * 1000L)));
    }

    private int intValue(Object value, int fallback) {
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private double doubleValue(Object value, double fallback) {
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private boolean canCurrentUserActOn(Reservation reservation) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return false;
        }
        String customerId = reservation.getCustomerId();
        if (customerId != null && customerId.equals(user.getUid())) {
            return true;
        }
        String guestEmail = reservation.getGuestEmail();
        return guestEmail != null && user.getEmail() != null && guestEmail.equalsIgnoreCase(user.getEmail());
    }

    private void showReviewDialog(android.content.Context context, Reservation reservation, Dialog detailDialog) {
        if (reservation.getHotelId() == null || reservation.getHotelId().isEmpty()) {
            Toast.makeText(context, "Không tìm thấy khách sạn để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !canCurrentUserActOn(reservation)) {
            Toast.makeText(context, "Bạn không có quyền đánh giá đơn này", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(AppConstants.COLLECTION_REVIEWS)
                .whereEqualTo("reservation_id", reservation.getId())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(context, "Đơn này đã được đánh giá", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    openReviewForm(context, reservation, user, detailDialog);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Không kiểm tra được đánh giá cũ", Toast.LENGTH_SHORT).show());
    }

    private void openReviewForm(android.content.Context context, Reservation reservation,
                                FirebaseUser user, Dialog detailDialog) {
        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(32, 8, 32, 0);

        RatingBar ratingBar = new RatingBar(context);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setRating(5);

        EditText commentInput = new EditText(context);
        commentInput.setHint("Nhập nội dung đánh giá");
        commentInput.setMinLines(3);
        commentInput.setPadding(12, 8, 12, 8);

        form.addView(ratingBar);
        form.addView(commentInput);

        new AlertDialog.Builder(context)
                .setTitle("Đánh giá dịch vụ")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String comment = commentInput.getText().toString().trim();
                    if (comment.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitReview(context, reservation, user, ratingBar.getRating(), comment, detailDialog);
                })
                .show();
    }

    private void submitReview(android.content.Context context, Reservation reservation, FirebaseUser user,
                              float rating, String comment, Dialog detailDialog) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("hotel_id", reservation.getHotelId());
        reviewData.put("reservation_id", reservation.getId());
        reviewData.put("user_id", user.getUid());
        reviewData.put("user_name", user.getDisplayName() == null ? reservation.getGuestName() : user.getDisplayName());
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);
        reviewData.put("created_at", Timestamp.now());
        reviewData.put("created_at_millis", new Date().getTime());

        db.collection(AppConstants.COLLECTION_REVIEWS)
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                    detailDialog.dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Không gửi được đánh giá: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgThumbnail;
        TextView tvHotelName, tvStatus, tvDate, tvPrice, tvBookingId;
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


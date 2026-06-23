package com.example.hotelbooking.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private final List<DocumentSnapshot> bookings = new ArrayList<>();

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
        holder.tvHotelName.setText(stringValue(booking, "hotel_name", "Khach san"));
        holder.tvStatus.setText(stringValue(booking, "status", "booked"));
        holder.tvDate.setText(stringValue(booking, "check_in", "") + " - " + stringValue(booking, "check_out", ""));
        holder.tvPrice.setText(formatMoney(doubleValue(booking, "total_price", 0)));

        String imageUrl = stringValue(booking, "hotel_image", "");
        if (!imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgThumbnail);
        }

        holder.btnCancel.setOnClickListener(v ->
                booking.getReference().update("status", "cancelled"));
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
        return String.format(Locale.getDefault(), "%,.0f VND", value);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgThumbnail;
        TextView tvHotelName;
        TextView tvStatus;
        TextView tvDate;
        TextView tvPrice;
        Button btnCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgHistoryThumbnail);
            tvHotelName = itemView.findViewById(R.id.tvHistoryHotelName);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvPrice = itemView.findViewById(R.id.tvHistoryPrice);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}

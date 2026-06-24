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
import com.example.hotelbooking.data.model.Reservation;
import com.example.hotelbooking.utils.AppConstants;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private final List<Reservation> reservations = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void updateData(List<Reservation> newReservations) {
        reservations.clear();
        reservations.addAll(newReservations);
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
        Reservation res = reservations.get(position);
        holder.tvHotelName.setText("Mã Đặt Phòng: " + res.getId());
        holder.tvStatus.setText(res.getStatus());
        
        String dateStr = "";
        if (res.getDayStart() != null && res.getDayEnd() != null) {
            dateStr = dateFormat.format(res.getDayStart().toDate()) + " - " + dateFormat.format(res.getDayEnd().toDate());
        }
        holder.tvDate.setText(dateStr);
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", res.getTotalPrice()));
        if (AppConstants.BOOKING_CONFIRMED.equals(res.getStatus()) ||
            AppConstants.BOOKING_PENDING_PAYMENT.equals(res.getStatus())) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        holder.btnCancel.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection(AppConstants.COLLECTION_RESERVATIONS)
                    .document(res.getId())
                    .update("status", AppConstants.BOOKING_CANCELLED)
                    .addOnSuccessListener(aVoid -> {
                        res.setStatus(AppConstants.BOOKING_CANCELLED);
                        notifyItemChanged(position);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
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

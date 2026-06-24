package com.example.hotelbooking.ui.partner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Reservation;
import com.example.hotelbooking.utils.AppConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PartnerBookingAdapter extends RecyclerView.Adapter<PartnerBookingAdapter.ViewHolder> {
    private List<Reservation> list = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onCancelClick(Reservation reservation);
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<Reservation> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation res = list.get(position);

        // Gán dữ liệu chuẩn xác lên các ô giao diện riêng biệt
        holder.tvId.setText("Mã đơn: " + res.getId());
        holder.tvGuest.setText("👤 Khách: " + res.getGuestName());
        holder.tvStatus.setText(res.getStatus());

        if (res.getDayStart() != null && res.getDayEnd() != null) {
            holder.tvDate.setText("📅 " + dateFormat.format(res.getDayStart().toDate()) + " - " + dateFormat.format(res.getDayEnd().toDate()));
        }

        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", res.getTotalPrice()));

        // Logic điều khiển nút Từ chối dựa theo trạng thái đơn hàng
        if (AppConstants.BOOKING_PENDING_PAYMENT.equals(res.getStatus())) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setText("Từ chối");

            holder.btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(res);
                }
            });
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvGuest, tvStatus, tvDate, tvPrice;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ khớp chuẩn 100% các ID có trong file XML
            tvId = itemView.findViewById(R.id.tvHistoryHotelName);
            tvGuest = itemView.findViewById(R.id.tvHistoryGuestName); // Hết lỗi đỏ!
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvPrice = itemView.findViewById(R.id.tvHistoryPrice);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}
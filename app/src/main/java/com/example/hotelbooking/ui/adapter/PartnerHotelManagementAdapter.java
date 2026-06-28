package com.example.hotelbooking.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PartnerHotelManagementAdapter extends RecyclerView.Adapter<PartnerHotelManagementAdapter.ViewHolder> {
    private final List<DocumentSnapshot> hotels = new ArrayList<>();
    private final OnHotelActionListener listener;

    public interface OnHotelActionListener {
        void onViewDetail(Hotel hotel);
        void onEdit(Hotel hotel);
        void onAddRoom(Hotel hotel);
    }

    public PartnerHotelManagementAdapter(OnHotelActionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<DocumentSnapshot> newHotels) {
        hotels.clear();
        hotels.addAll(newHotels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partner_hotel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot document = hotels.get(position);
        Hotel hotel = Hotel.fromDocument(document);

        holder.textHotelName.setText(valueOrDefault(hotel.getHotelName(), "Khách sạn"));
        holder.textHotelAddress.setText("Địa chỉ: " + valueOrDefault(hotel.getAddress(), "Đang cập nhật"));
        holder.textPriceFrom.setText(String.format(Locale.getDefault(), "Giá từ: %,.0f VNĐ", hotel.getPrice()));
        holder.textStatus.setText("Trạng thái: " + displayStatus(hotel));

        String adminNote = firstString(document, "", "admin_note", "reject_reason");
        if (AppConstants.STATUS_REJECTED.equalsIgnoreCase(valueOrDefault(hotel.getApprovalStatus(), "")) && !adminNote.isEmpty()) {
            holder.textAdminNote.setVisibility(View.VISIBLE);
            holder.textAdminNote.setText("Lý do từ chối: " + adminNote);
        } else {
            holder.textAdminNote.setVisibility(View.GONE);
        }

        holder.buttonViewDetail.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetail(hotel);
        });
        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(hotel);
        });
        holder.buttonAddRoom.setOnClickListener(v -> {
            if (listener != null) listener.onAddRoom(hotel);
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    private String displayStatus(Hotel hotel) {
        String status = valueOrDefault(hotel.getApprovalStatus(), AppConstants.STATUS_PENDING);
        if (AppConstants.STATUS_APPROVED.equalsIgnoreCase(status)) {
            return hotel.isActive() ? "Đã duyệt" : "Đã duyệt, đang tắt";
        }
        if (AppConstants.STATUS_REJECTED.equalsIgnoreCase(status)) {
            return "Bị từ chối";
        }
        return "Chờ duyệt";
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String firstString(DocumentSnapshot document, String fallback, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return fallback;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textHotelName;
        TextView textHotelAddress;
        TextView textPriceFrom;
        TextView textStatus;
        TextView textAdminNote;
        Button buttonViewDetail;
        Button buttonEdit;
        Button buttonAddRoom;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textHotelName = itemView.findViewById(R.id.textHotelName);
            textHotelAddress = itemView.findViewById(R.id.textHotelAddress);
            textPriceFrom = itemView.findViewById(R.id.textPriceFrom);
            textStatus = itemView.findViewById(R.id.textStatus);
            textAdminNote = itemView.findViewById(R.id.textAdminNote);
            buttonViewDetail = itemView.findViewById(R.id.buttonViewDetail);
            buttonEdit = itemView.findViewById(R.id.buttonEditHotel);
            buttonAddRoom = itemView.findViewById(R.id.buttonAddRoom);
        }
    }
}

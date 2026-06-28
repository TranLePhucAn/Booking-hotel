package com.example.hotelbooking.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;
import com.example.hotelbooking.utils.AppConstants;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminHotelManagementAdapter extends RecyclerView.Adapter<AdminHotelManagementAdapter.ViewHolder> {
    private final List<DocumentSnapshot> hotels = new ArrayList<>();
    private final OnHotelActionListener listener;

    public interface OnHotelActionListener {
        void onPreview(DocumentSnapshot hotelDocument);
        void onApprove(DocumentSnapshot hotelDocument);
        void onReject(DocumentSnapshot hotelDocument);
        void onToggleActive(DocumentSnapshot hotelDocument);
    }

    public AdminHotelManagementAdapter(OnHotelActionListener listener) {
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_hotel_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot hotelDocument = hotels.get(position);

        String name = firstString(hotelDocument, "Chưa cập nhật tên khách sạn", "hotel_name", "name");
        String address = firstString(hotelDocument, "Chưa cập nhật địa chỉ", "address", "address_text", "location");
        String ownerId = firstString(hotelDocument, "Chưa có owner_id", "owner_id", "ownerId");
        String approvalStatus = firstString(hotelDocument, AppConstants.STATUS_PENDING, "approval_status", "status");
        boolean active = booleanValue(hotelDocument, "is_active", false);
        double price = firstDouble(hotelDocument, 0.0, "price_from", "price");

        holder.textHotelName.setText(name);
        holder.textHotelAddress.setText(address);
        holder.textOwnerId.setText("Owner ID: " + ownerId);
        holder.textPriceFrom.setText(String.format(Locale.getDefault(), "Giá từ: %,.0f VNĐ", price));
        holder.textApprovalStatus.setText("Duyệt: " + displayApprovalStatus(approvalStatus));
        holder.textActiveStatus.setText(active ? "Hoạt động: Đang bật" : "Hoạt động: Đang tắt");

        holder.buttonToggleActive.setText(active ? "Tắt" : "Bật");
        holder.buttonToggleActive.setBackgroundColor(Color.parseColor(active ? "#F57C00" : "#2E7D32"));
        holder.buttonApprove.setEnabled(!AppConstants.STATUS_APPROVED.equalsIgnoreCase(approvalStatus));
        holder.buttonReject.setEnabled(!AppConstants.STATUS_REJECTED.equalsIgnoreCase(approvalStatus));

        holder.buttonPreview.setOnClickListener(v -> {
            if (listener != null) listener.onPreview(hotelDocument);
        });
        holder.buttonApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(hotelDocument);
        });
        holder.buttonReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(hotelDocument);
        });
        holder.buttonToggleActive.setOnClickListener(v -> {
            if (listener != null) listener.onToggleActive(hotelDocument);
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    private String displayApprovalStatus(String status) {
        if (AppConstants.STATUS_APPROVED.equalsIgnoreCase(status)) {
            return "Đã duyệt";
        }
        if (AppConstants.STATUS_REJECTED.equalsIgnoreCase(status)) {
            return "Đã từ chối";
        }
        return "Chờ duyệt";
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

    private double firstDouble(DocumentSnapshot document, double fallback, String... fields) {
        for (String field : fields) {
            Object value = document.get(field);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return fallback;
    }

    private boolean booleanValue(DocumentSnapshot document, String field, boolean fallback) {
        Boolean value = document.getBoolean(field);
        return value == null ? fallback : value;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textHotelName;
        TextView textHotelAddress;
        TextView textOwnerId;
        TextView textPriceFrom;
        TextView textApprovalStatus;
        TextView textActiveStatus;
        Button buttonPreview;
        Button buttonApprove;
        Button buttonReject;
        Button buttonToggleActive;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textHotelName = itemView.findViewById(R.id.textHotelName);
            textHotelAddress = itemView.findViewById(R.id.textHotelAddress);
            textOwnerId = itemView.findViewById(R.id.textOwnerId);
            textPriceFrom = itemView.findViewById(R.id.textPriceFrom);
            textApprovalStatus = itemView.findViewById(R.id.textApprovalStatus);
            textActiveStatus = itemView.findViewById(R.id.textActiveStatus);
            buttonPreview = itemView.findViewById(R.id.buttonPreviewHotel);
            buttonApprove = itemView.findViewById(R.id.buttonApproveHotel);
            buttonReject = itemView.findViewById(R.id.buttonRejectHotel);
            buttonToggleActive = itemView.findViewById(R.id.buttonToggleActiveHotel);
        }
    }
}

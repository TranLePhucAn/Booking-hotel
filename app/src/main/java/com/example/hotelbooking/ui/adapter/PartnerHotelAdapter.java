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
import com.example.hotelbooking.data.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class PartnerHotelAdapter extends RecyclerView.Adapter<PartnerHotelAdapter.ViewHolder> {

    private List<Hotel> list = new ArrayList<>();
    private OnHotelActionListener listener;

    public interface OnHotelActionListener {
        void onEdit(Hotel hotel);
        void onManageRooms(Hotel hotel);
    }

    public void setListener(OnHotelActionListener listener) {
        this.listener = listener;
    }

    public void setList(List<Hotel> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_partner_hotel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hotel hotel = list.get(position);

        holder.tvName.setText(hotel.getHotelName() != null ? hotel.getHotelName() : "Không có tên");
        holder.tvAddress.setText(hotel.getAddress() != null ? hotel.getAddress() : "Chưa có địa chỉ");

        double price = hotel.getPrice();
        holder.tvPrice.setText(String.format("Giá: %,.0f VND/đêm", price));

        // Trạng thái duyệt
        String status = hotel.getApprovalStatus();
        if (status == null) status = hotel.getStatus();
        if (status == null) status = "pending";

        switch (status) {
            case "approved":
                holder.tvStatus.setText("✓ Đã duyệt");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#27AE60"));
                break;
            case "rejected":
                holder.tvStatus.setText("✗ Bị từ chối");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#E74C3C"));
                break;
            default:
                holder.tvStatus.setText("⏳ Đang chờ");
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F39C12"));
                break;
        }

        // Hiển thị admin_note nếu bị từ chối
        String adminNote = hotel.getAdminNote();
        if ("rejected".equals(status) && adminNote != null && !adminNote.isEmpty()) {
            holder.tvAdminNote.setVisibility(View.VISIBLE);
            holder.tvAdminNote.setText("Lý do: " + adminNote);
        } else {
            holder.tvAdminNote.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(hotel);
        });

        holder.btnManageRooms.setOnClickListener(v -> {
            if (listener != null) listener.onManageRooms(hotel);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvPrice, tvStatus, tvAdminNote;
        Button btnEdit, btnManageRooms;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHotelItemName);
            tvAddress = itemView.findViewById(R.id.tvHotelItemAddress);
            tvPrice = itemView.findViewById(R.id.tvHotelItemPrice);
            tvStatus = itemView.findViewById(R.id.tvHotelItemStatus);
            tvAdminNote = itemView.findViewById(R.id.tvHotelItemAdminNote);
            btnEdit = itemView.findViewById(R.id.btnEditHotelItem);
            btnManageRooms = itemView.findViewById(R.id.btnManageRooms);
        }
    }
}

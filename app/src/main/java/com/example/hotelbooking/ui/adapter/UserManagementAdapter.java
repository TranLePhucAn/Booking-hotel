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

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.ViewHolder> {
    private final List<DocumentSnapshot> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onViewDetail(DocumentSnapshot userDocument);
        void onToggleStatus(DocumentSnapshot userDocument);
    }

    public UserManagementAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<DocumentSnapshot> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot userDocument = users.get(position);

        String name = firstString(userDocument, "Chưa cập nhật tên", "fullName", "displayName", "name");
        String email = firstString(userDocument, "Chưa có email", "email");
        String phone = firstString(userDocument, "Chưa cập nhật", "phone", "phoneNumber");
        String role = firstString(userDocument, AppConstants.ROLE_USER, "role");
        String status = firstString(userDocument, "active", "status");
        String partnerStatus = firstString(userDocument, "", "partnerStatus", "partner_status");

        holder.textUserName.setText(name);
        holder.textUserEmail.setText(email);
        holder.textUserPhone.setText("Số điện thoại: " + phone);
        holder.textUserRole.setText(role.toLowerCase(Locale.ROOT));
        holder.textUserStatus.setText("Trạng thái: " + displayStatus(status));

        if (partnerStatus.trim().isEmpty()) {
            holder.textPartnerStatus.setVisibility(View.GONE);
        } else {
            holder.textPartnerStatus.setVisibility(View.VISIBLE);
            holder.textPartnerStatus.setText("Partner: " + partnerStatus);
        }

        boolean blocked = AppConstants.STATUS_BLOCKED.equalsIgnoreCase(status);
        holder.buttonToggleStatus.setText(blocked ? "Mở khóa" : "Khóa");
        holder.buttonToggleStatus.setBackgroundColor(Color.parseColor(blocked ? "#2E7D32" : "#B00020"));

        holder.buttonViewDetail.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetail(userDocument);
        });
        holder.buttonToggleStatus.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(userDocument);
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetail(userDocument);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
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

    private String displayStatus(String status) {
        if (AppConstants.STATUS_BLOCKED.equalsIgnoreCase(status)) {
            return "Đã khóa";
        }
        return status == null || status.trim().isEmpty() ? "active" : status;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textUserEmail, textUserPhone, textUserRole, textUserStatus, textPartnerStatus;
        Button buttonViewDetail, buttonToggleStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textUserName);
            textUserEmail = itemView.findViewById(R.id.textUserEmail);
            textUserPhone = itemView.findViewById(R.id.textUserPhone);
            textUserRole = itemView.findViewById(R.id.textUserRole);
            textUserStatus = itemView.findViewById(R.id.textUserStatus);
            textPartnerStatus = itemView.findViewById(R.id.textPartnerStatus);
            buttonViewDetail = itemView.findViewById(R.id.buttonViewDetail);
            buttonToggleStatus = itemView.findViewById(R.id.buttonToggleStatus);
        }
    }
}

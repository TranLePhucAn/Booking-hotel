package com.example.hotelbooking.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.PartnerApplication;
import java.util.ArrayList;
import java.util.List;

public class PartnerApplicationAdapter extends RecyclerView.Adapter<PartnerApplicationAdapter.ViewHolder> {
    private List<PartnerApplication> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PartnerApplication app);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<PartnerApplication> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partner_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PartnerApplication app = list.get(position);
        holder.tvBusinessName.setText(app.getBusinessName());
        holder.tvRepresentativeName.setText("Người đại diện: " + app.getRepresentativeName());
        holder.tvEmail.setText("Email: " + app.getEmail());
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(app);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBusinessName, tvRepresentativeName, tvEmail;
        Button btnViewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBusinessName = itemView.findViewById(R.id.tvBusinessName);
            tvRepresentativeName = itemView.findViewById(R.id.tvRepresentativeName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}

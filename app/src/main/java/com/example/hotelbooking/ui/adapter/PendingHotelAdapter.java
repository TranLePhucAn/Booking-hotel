package com.example.hotelbooking.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PendingHotelAdapter extends RecyclerView.Adapter<PendingHotelAdapter.ViewHolder> {
    private List<Hotel> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Hotel hotel);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setList(List<Hotel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hotel hotel = list.get(position);
        holder.tvName.setText(hotel.getHotelName());
        holder.tvAddress.setText(hotel.getAddress());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", hotel.getPrice()));

        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(hotel.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivHotel);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(hotel);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotel;
        TextView tvName, tvAddress, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotel = itemView.findViewById(R.id.imgHotel);
            tvName = itemView.findViewById(R.id.txtName);
            tvAddress = itemView.findViewById(R.id.txtLocation);
            tvPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}

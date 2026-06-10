package com.example.hotelbooking.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.model.Hotel;

import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.ViewHolder> {

    private List<Hotel> hotels;
    private OnHotelClickListener listener;

    // Interface dùng để bắt sự kiện Click từ HomeActivity (Giữ từ main)
    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
        void onBookClick(Hotel hotel);
    }

    public HotelAdapter(List<Hotel> hotels, OnHotelClickListener listener) {
        this.hotels = hotels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        holder.txtName.setText(hotel.getName());
        holder.txtLocation.setText(hotel.getLocation()); // Thống nhất dùng getLocation()
        holder.txtPrice.setText(String.format(Locale.getDefault(), "$%.0f / night", hotel.getPrice()));

        // 1. Hiển thị số sao đánh giá (Bổ sung từ nhánhAn)
        if (holder.ratingBarSmall != null) {
            holder.ratingBarSmall.setRating(hotel.getRating());
        }

        // 2. Tải ảnh bằng thư viện Glide (Bổ sung từ nhánh An - dùng getImageUrl())
        if (holder.imgHotel != null) {
            Glide.with(holder.itemView.getContext())
                    .load(hotel.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgHotel);
        }

        // Bắt sự kiện click qua listener
        holder.itemView.setOnClickListener(v -> listener.onHotelClick(hotel));
        if (holder.btnBook != null) {
            holder.btnBook.setOnClickListener(v -> listener.onBookClick(hotel));
        }
    }

    @Override
    public int getItemCount() {
        return hotels != null ? hotels.size() : 0;
    }

    public void updateData(List<Hotel> newHotels) {
        this.hotels = newHotels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtLocation, txtPrice;
        Button btnBook;
        ImageView imgHotel;      // Khai báo thêm trường ảnh 
        RatingBar ratingBarSmall; // Khai báo thêm trường rating

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnBook = itemView.findViewById(R.id.btnBook);
            imgHotel = itemView.findViewById(R.id.imgHotel);
            ratingBarSmall = itemView.findViewById(R.id.ratingBarSmall);
        }
    }
}
package com.example.hotelbooking.ui.adapter;

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
    private boolean isHorizontal = false;

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel);
        void onBookClick(Hotel hotel);
    }

    public HotelAdapter(List<Hotel> hotels, OnHotelClickListener listener) {
        this.hotels = hotels;
        this.listener = listener;
    }

    public HotelAdapter(List<Hotel> hotels, boolean isHorizontal, OnHotelClickListener listener) {
        this.hotels = hotels;
        this.isHorizontal = isHorizontal;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel, parent, false);
        if (isHorizontal) {
            // Set fixed width for horizontal items
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = (int) (parent.getResources().getDisplayMetrics().widthPixels * 0.7);
            view.setLayoutParams(params);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        holder.txtName.setText(hotel.getHotelName());
        holder.txtLocation.setText(hotel.getAddress());
        holder.txtPrice.setText(String.format(Locale.getDefault(), "Tu %,.0f VND", hotel.getPrice()));

        if (holder.ratingBarSmall != null) {
            float rating = hotel.getReviewScore() > 0 ? (float) hotel.getReviewScore() / 2 : (float) hotel.getRatingStar();
            holder.ratingBarSmall.setRating(rating);
        }

        if (holder.imgHotel != null) {
            Glide.with(holder.itemView.getContext())
                    .load(hotel.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(holder.imgHotel);
        }

        // Setup Badges
        if (holder.tvBadge != null) {
            if (hotel.isSoldOut()) {
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText("Hết phòng");
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_red);
            } else if (hotel.isOffer()) {
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText("Ưu đãi");
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge);
            } else if (hotel.getReviewScore() >= 9.0 || hotel.getRatingStar() >= 4.5) {
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.tvBadge.setText("Đánh giá cao");
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_blue);
            } else {
                holder.tvBadge.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onHotelClick(hotel));
        if (holder.btnBook != null) {
            holder.btnBook.setOnClickListener(v -> listener.onBookClick(hotel));
            if (hotel.isSoldOut()) {
                holder.btnBook.setEnabled(false);
                holder.btnBook.setText("Hết chỗ");
                holder.btnBook.setAlpha(0.5f);
            } else {
                holder.btnBook.setEnabled(true);
                holder.btnBook.setText("Đặt ngay");
                holder.btnBook.setAlpha(1.0f);
            }
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
        TextView txtName, txtLocation, txtPrice, tvBadge;
        Button btnBook;
        ImageView imgHotel;
        RatingBar ratingBarSmall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnBook = itemView.findViewById(R.id.btnBook);
            imgHotel = itemView.findViewById(R.id.imgHotel);
            ratingBarSmall = itemView.findViewById(R.id.ratingBarSmall);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}

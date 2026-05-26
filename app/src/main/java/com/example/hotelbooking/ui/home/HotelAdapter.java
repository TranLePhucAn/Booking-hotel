package com.example.hotelbooking.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.data.model.Hotel;
import com.example.hotelbooking.databinding.ItemHotelBinding;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;

import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private Context context;

    public HotelAdapter(List<Hotel> hotelList, Context context) {
        this.hotelList = hotelList;
        this.context = context;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHotelBinding binding = ItemHotelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HotelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);
        holder.binding.txtName.setText(hotel.getName());
        holder.binding.txtLocation.setText(hotel.getAddress());
        holder.binding.txtPrice.setText(String.format(Locale.getDefault(), "$%.2f / night", hotel.getPrice()));
        holder.binding.ratingBarSmall.setRating(hotel.getRating());

        // Load image using Glide
        Glide.with(context)
                .load(hotel.getMainImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.imgHotel);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HotelDetailActivity.class);
            intent.putExtra("hotel", hotel);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotelList != null ? hotelList.size() : 0;
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        ItemHotelBinding binding;

        public HotelViewHolder(ItemHotelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

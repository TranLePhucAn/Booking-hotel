package com.example.hotelbooking.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotelbooking.R;
import com.example.hotelbooking.data.repository.WishlistRepository;
import com.example.hotelbooking.ui.hotel.HotelDetailActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private final List<Map<String, Object>> wishlistItems = new ArrayList<>();
    private final WishlistRepository wishlistRepository = new WishlistRepository();

    public void updateData(List<Map<String, Object>> newItems) {
        wishlistItems.clear();
        if (newItems != null) {
            wishlistItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = wishlistItems.get(position);
        Context context = holder.itemView.getContext();

        String hotelId = (String) item.get("hotelId");
        String hotelName = (String) item.get("hotelName");
        String hotelImage = (String) item.get("hotelImage");
        String address = (String) item.get("address");

        double basePrice = 0;
        if (item.get("basePrice") instanceof Double) {
            basePrice = (Double) item.get("basePrice");
        } else if (item.get("basePrice") instanceof Long) {
            basePrice = ((Long) item.get("basePrice")).doubleValue();
        }

        float rating = 5.0f;
        if (item.get("rating") instanceof Double) {
            rating = ((Double) item.get("rating")).floatValue();
        } else if (item.get("rating") instanceof Float) {
            rating = (Float) item.get("rating");
        }

        holder.txtName.setText(hotelName != null ? hotelName : "Khách sạn");
        holder.txtLocation.setText(address != null ? address : "Chưa cập nhật địa chỉ");
        holder.txtPrice.setText(String.format(Locale.getDefault(), "Từ %,.0f đ/đêm", basePrice));
        holder.ratingBarSmall.setRating(rating);

        if (hotelImage != null && !hotelImage.isEmpty()) {
            Glide.with(context)
                    .load(hotelImage)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgHotel);
        } else {
            holder.imgHotel.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);

        holder.btnFavorite.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

            if (userId != null && hotelId != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;

                wishlistRepository.removeFromWishlist(userId, hotelId)
                        .addOnSuccessListener(aVoid -> {
                            wishlistItems.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            notifyItemRangeChanged(currentPosition, wishlistItems.size());
                            Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Không thể bỏ yêu thích!", Toast.LENGTH_SHORT).show());
            }
        });


        View.OnClickListener openDetailIntent = v -> {
            if (hotelId != null && !hotelId.isEmpty()) {
                Intent intent = new Intent(context, HotelDetailActivity.class);
                intent.putExtra("hotel_id", hotelId);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Mã khách sạn không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        };

        holder.itemView.setOnClickListener(openDetailIntent);
        if (holder.btnBook != null) {
            holder.btnBook.setOnClickListener(openDetailIntent);
        }
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHotel;
        ImageButton btnFavorite;
        TextView txtName, txtLocation, txtPrice;
        RatingBar ratingBarSmall;
        Button btnBook;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHotel = itemView.findViewById(R.id.imgHotel);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            txtName = itemView.findViewById(R.id.txtName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            ratingBarSmall = itemView.findViewById(R.id.ratingBarSmall);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}
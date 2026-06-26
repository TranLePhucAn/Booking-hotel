package com.example.hotelbooking.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<String> categories;
    private OnItemClickListener listener;
    private int selectedPosition = 0; // Mặc định chọn "Tất cả" (vị trí 0)

    public interface OnItemClickListener {
        void onItemClick(String category);
    }

    public CategoryAdapter(List<String> categories, OnItemClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);

        // Xử lý hiển thị dựa trên trạng thái chọn
        if (position == selectedPosition) {
            // Khi được chọn: Nền xanh, chữ trắng
            holder.tvCategoryName.setBackgroundResource(R.drawable.bg_category_selected);
            holder.tvCategoryName.setTextColor(Color.WHITE);
        } else {
            // Khi không chọn: Nền xám nhạt, chữ đen
            holder.tvCategoryName.setBackgroundResource(R.drawable.bg_category_unselected);
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.id.tvCategoryName == 0 ? android.R.color.black : R.color.text_primary));
            // Sửa lại dòng trên để an toàn hơn
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            // Cập nhật lại giao diện cho mục cũ và mục mới
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            listener.onItemClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}

package com.example.hotelbooking.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hotelbooking.R;

import java.util.ArrayList;
import java.util.Map;

public class PartnerRoomAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Map<String, Object>> roomList;

    public PartnerRoomAdapter(Context context, ArrayList<Map<String, Object>> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int i) {
        return roomList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_partner_room, viewGroup, false);

            viewHolder = new ViewHolder();
            viewHolder.imgItemRoom = view.findViewById(R.id.img_item_room);
            viewHolder.tvItemRoomName = view.findViewById(R.id.tv_item_room_name);
            viewHolder.tvItemRoomType = view.findViewById(R.id.tv_item_room_type);
            viewHolder.tvItemRoomQty = view.findViewById(R.id.tv_item_room_qty);
            viewHolder.tvItemRoomPrice = view.findViewById(R.id.tv_item_room_price);
        } else {
            viewHolder = (ViewHolder) view.getTag(); // tái sd lại khung nhìn cũ
        }

        // lấy dl map của phòng hiện tại
        Map<String, Object> room = roomList.get(i);
        String name = (String) room.get("room_name");
        String type = (String) room.get("room_type");
        String bed = (String) room.get("bed_type");

        long total = room.get("total_rooms") != null ? (long) room.get("total_rooms") : 0;
        long available = room.get("available_rooms") != null ? (long) room.get("available_rooms") : 0;
        double price = room.get("price_per_night") != null ? (double) room.get("price_per_night") : 0.0;

        viewHolder.tvItemRoomName.setText(name);
        viewHolder.tvItemRoomType.setText("Loại: " + type + " | " + bed);
        viewHolder.tvItemRoomQty.setText("Trống: " + available + " / Tổng: " + total + " phòng");
        viewHolder.tvItemRoomPrice.setText(String.format("%,.0f VND / đêm", price));

        return view;
    }

    private class ViewHolder {
        ImageView imgItemRoom;
        TextView tvItemRoomName, tvItemRoomType, tvItemRoomQty, tvItemRoomPrice;
    }
}

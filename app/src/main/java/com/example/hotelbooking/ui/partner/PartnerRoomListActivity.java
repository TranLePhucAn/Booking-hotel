package com.example.hotelbooking.ui.partner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.adapter.PartnerRoomAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;

public class PartnerRoomListActivity extends AppCompatActivity {

    private PartnerRoomAdapter adapter;
    private ArrayList<Map<String, Object>> roomList;

    private String hotelId = "d8OtAy1EnpURXX6NlNnt";

    private ListView listView;
    private FloatingActionButton fabAddRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_list_room);

        if (getIntent().getStringExtra("EXTRA_HOTEL_ID") != null) {
            hotelId = getIntent().getStringExtra("EXTRA_HOTEL_ID");
        }

        listView = findViewById(R.id.lv_partner_rooms);
        fabAddRoom = findViewById(R.id.fab_add_room);

        roomList = new ArrayList<>();
        adapter = new PartnerRoomAdapter(this, roomList);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, i, id) -> {
            Map<String, Object> clickedRoom = roomList.get(i);
            String roomId = (String) clickedRoom.get("room_id");
            String sectionId = (String) clickedRoom.get("section_id");

            Intent intent = new Intent(PartnerRoomListActivity.this, PartnerEditRoomActivity.class);
            intent.putExtra("EXTRA_ROOM_ID", roomId);
            intent.putExtra("EXTRA_SECTION_ID", sectionId);
            startActivity(intent);
        });

        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(PartnerRoomListActivity.this, PartnerAddRoomActivity.class);
            intent.putExtra("EXTRA_HOTEL_ID", hotelId);
            startActivity(intent);
        });
    }
}

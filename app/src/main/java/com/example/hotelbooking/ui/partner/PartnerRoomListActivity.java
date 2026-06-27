package com.example.hotelbooking.ui.partner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.adapter.PartnerRoomAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Map;

public class PartnerRoomListActivity extends AppCompatActivity {

    private PartnerRoomAdapter adapter;
    private ArrayList<Map<String, Object>> roomList;

    private String hotelId = "d8OtAy1EnpURXX6NlNnt";

    private ListView listView;
    private FloatingActionButton fabAddRoom;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_list_room);

        db = FirebaseFirestore.getInstance();

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

        listView.setOnItemLongClickListener((adapterView, view, i, id) -> {
            Map<String, Object> clickedRoom = roomList.get(i);
            String roomId = (String) clickedRoom.get("room_id");
            String roomName = (String) clickedRoom.get("room_name");

            new AlertDialog.Builder(PartnerRoomListActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa phòng '" + roomName + "' không?")
                    .setPositiveButton("Xóa", (dialogInterface, i1) -> {
                        db.collection("sections")
                                .whereEqualTo("room_id", roomId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    // tạo một WriteBatch để xóa nhiều doc cùng một lúc
                                    WriteBatch batch = db.batch();
                                    for(DocumentSnapshot sectionDoc : queryDocumentSnapshots.getDocuments()) {
                                        batch.delete(sectionDoc.getReference());
                                    }

                                    // xóa phòng
                                    DocumentReference roomRef = db.collection("rooms").document(roomId);
                                    batch.delete(roomRef);

                                    // thực thi xóa toàn bộ
                                    batch.commit()
                                            .addOnSuccessListener(runnable -> {
                                                Toast.makeText(PartnerRoomListActivity.this,
                                                        "Đã xóa phòng và toàn bộ hạng phòng liên quan thành công!",
                                                        Toast.LENGTH_SHORT).show();
                                                // gọi hàm load lại ds
                                                loadPartnerRooms();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(PartnerRoomListActivity.this,
                                                        "Lỗi khi xóa dữ liệu liên quan: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(PartnerRoomListActivity.this,
                                            "Không thể truy cập dữ liệu hạng phòng: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPartnerRooms();
    }

    private void loadPartnerRooms() {
        db.collection("rooms")
                .whereEqualTo("hotel_id", hotelId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    roomList.clear();

                    if(!queryDocumentSnapshots.isEmpty()) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Map<String, Object> roomData = doc.getData();
                            if(roomData != null) {
                                roomData.put("room_id", doc.getId());
                                roomList.add(roomData);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Khách sạn này chưa có phòng nào!", Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Lỗi tải danh sách phòng: " + e.getMessage());
                    Toast.makeText(this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
                });
    }
}

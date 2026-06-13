package com.example.hotelbooking.ui.owner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class OwnerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        TextView tvOwnerEmail = findViewById(R.id.tvOwnerEmail);
        Button btnAddHotel = findViewById(R.id.btnAddHotel);
        Button btnImportRooms = findViewById(R.id.btnImportRooms);
        Button btnManageRooms = findViewById(R.id.btnManageRooms);
        Button btnLogoutOwner = findViewById(R.id.btnLogoutOwner);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tvOwnerEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        btnAddHotel.setOnClickListener(v ->
                startActivity(new Intent(this, AddHotelActivity.class)));

        btnImportRooms.setOnClickListener(v -> {
            // TODO: Open Excel file picker and batch upload rooms to Firestore.
        });

        btnManageRooms.setOnClickListener(v -> {
            // TODO: Add room management screen.
        });

        btnLogoutOwner.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}

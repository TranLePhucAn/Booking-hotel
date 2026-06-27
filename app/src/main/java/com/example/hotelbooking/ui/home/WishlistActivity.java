package com.example.hotelbooking.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotelbooking.R;
import com.example.hotelbooking.ui.adapter.WishlistAdapter;
import com.example.hotelbooking.viewmodels.WishlistViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class WishlistActivity extends AppCompatActivity {

    private WishlistViewModel wishlistViewModel;
    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        ImageView btnBack = findViewById(R.id.btnBackWishlist);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        rvWishlist = findViewById(R.id.rvWishlist);
        rvWishlist.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WishlistAdapter();
        rvWishlist.setAdapter(adapter);

        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);

        wishlistViewModel.wishlistItems.observe(this, items -> {
            if (items != null && !items.isEmpty()) {

                adapter.updateData(items);
            } else {
                adapter.updateData(new ArrayList<>());
                Toast.makeText(this, "Danh sách yêu thích trống", Toast.LENGTH_SHORT).show();
            }
        });

        wishlistViewModel.error.observe(this, err -> {
            if (err != null) Toast.makeText(this, "Lỗi: " + err, Toast.LENGTH_SHORT).show();
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            wishlistViewModel.fetchWishlist(user.getUid());
        }
    }
}
package com.example.hotelbooking.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.data.remote.FirebaseClient;
import com.example.hotelbooking.ui.auth.LoginActivity;
import com.example.hotelbooking.utils.LoadingDialog;
import com.example.hotelbooking.utils.RoleRouter;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseClient.getAuth().getCurrentUser();
            if (currentUser != null) {
                RoleRouter.routeCurrentUser(this, new LoadingDialog(this));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }, 1200);
    }
}

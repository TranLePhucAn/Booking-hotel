package com.example.hotelbooking.data.remote;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseClient {

    private static FirebaseAuth auth;
    private static DatabaseReference databaseReference;

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static DatabaseReference getDatabaseReference() {
        if (databaseReference == null) {
            // Lấy tham chiếu gốc của Realtime Database
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }
}
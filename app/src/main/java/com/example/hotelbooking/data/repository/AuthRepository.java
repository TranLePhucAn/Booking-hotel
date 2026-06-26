package com.example.hotelbooking.data.repository;

import com.example.hotelbooking.data.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final DatabaseReference database;

    public AuthRepository() {
        auth = FirebaseClient.getAuth();
        database = FirebaseClient.getDatabaseReference();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public DatabaseReference getDatabase() {
        return database;
    }
}
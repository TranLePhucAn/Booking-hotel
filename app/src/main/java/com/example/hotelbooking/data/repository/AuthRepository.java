package com.example.hotelbooking.data.repository;

import com.example.hotelbooking.data.remote.FirebaseClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final DatabaseReference database;

    public AuthRepository() {
        auth = FirebaseClient.getAuth();
        database = FirebaseClient.getDatabaseReference();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public void logout() {
        auth.signOut();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public DatabaseReference getDatabase() {
        return database;
    }
}
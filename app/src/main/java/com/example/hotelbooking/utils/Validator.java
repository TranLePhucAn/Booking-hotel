package com.example.hotelbooking.utils;

import android.util.Patterns;

public class Validator {

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}
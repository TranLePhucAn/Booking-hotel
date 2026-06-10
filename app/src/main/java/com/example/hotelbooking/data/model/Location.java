package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
// class thể hiện vùng có thuộc tính city
public class Location implements Serializable {
    private String id;
    private String city;

    public Location(String id, String city) {
        this.id = id;
        this.city = city;
    }

    public Location() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @PropertyName("city")
    public String getCity() {
        return city;
    }
    @PropertyName("city")
    public void setCity(String city) {
        this.city = city;
    }
}

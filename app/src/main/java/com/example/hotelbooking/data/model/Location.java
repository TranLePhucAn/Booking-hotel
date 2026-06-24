package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
public class Location implements Serializable {
    private String id;
    private String city;
    private String district;

    public Location() {}

    public Location(String id, String city, String district) {
        this.id = id;
        this.city = city;
        this.district = district;
    }

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
    @PropertyName("district")
    public String getDistrict() { return district; }
    @PropertyName("district")
    public void setDistrict(String district) { this.district = district; }
}
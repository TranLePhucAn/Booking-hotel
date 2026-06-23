package com.example.hotelbooking.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String comment;
    private Timestamp createdAt;
    private String hotelId;
    private double rating;
    private String userName;

    public Review() {}

    public Review(String id, String comment, Timestamp createdAt, String hotelId, double rating, String userName) {
        this.id = id;
        this.comment = comment;
        this.createdAt = createdAt;
        this.hotelId = hotelId;
        this.rating = rating;
        this.userName = userName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    @PropertyName("created_at")
    public Timestamp getCreatedAt() { return createdAt; }
    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @PropertyName("hotel_id")
    public String getHotelId() { return hotelId; }
    @PropertyName("hotel_id")
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    @PropertyName("user_name")
    public String getUserName() { return userName; }
    @PropertyName("user_name")
    public void setUserName(String userName) { this.userName = userName; }
}

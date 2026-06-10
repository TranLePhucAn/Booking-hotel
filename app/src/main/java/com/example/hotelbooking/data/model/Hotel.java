package com.example.hotelbooking.data.model;

import java.io.Serializable;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;
// class khách sạn
public class Hotel implements Serializable {
    private String id;
    private String hotelName;
    private String imageUrl;
    private String locationId;
    private int reviewCount;
    private double ratingStar;
    private double reviewScore;
    private String category;
    private String description;
    private List<String> secondaryImages = new ArrayList<>();
    // danh sách tiện ích
    private List<String> amenities = new ArrayList<>();
    // vĩ độ
    private double latitude;
    // kinh độ
    private double longitude;

    public Hotel() {}

    public Hotel(String id, String hotelName, String imageUrl, String locationId, int reviewCount, double ratingStar, double reviewScore, String category, String description, double latitude, double longitude) {
        this.id = id;
        this.hotelName = hotelName;
        this.imageUrl = imageUrl;
        this.locationId = locationId;
        this.reviewCount = reviewCount;
        this.ratingStar = ratingStar;
        this.reviewScore = reviewScore;
        this.category = category;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSecondaryImages() {
        return secondaryImages;
    }

    public void setSecondaryImages(List<String> secondaryImages) {
        this.secondaryImages = secondaryImages;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @PropertyName("hotel_name")
    public String getHotelName() { return hotelName; }

    @PropertyName("hotel_name")
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    @PropertyName("image_url")
    public String getImageUrl() { return imageUrl; }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("location_id")
    public String getLocationId() { return locationId; }

    @PropertyName("location_id")
    public void setLocationId(String locationId) { this.locationId = locationId; }

    @PropertyName("rating_star")
    public double getRatingStar() { return ratingStar; }

    @PropertyName("rating_star")
    public void setRatingStar(double ratingStar) { this.ratingStar = ratingStar; }

    @PropertyName("review_count")
    public int getReviewCount() { return reviewCount; }

    @PropertyName("review_count")
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    @PropertyName("review_score")
    public double getReviewScore() { return reviewScore; }

    @PropertyName("review_score")
    public void setReviewScore(double reviewScore) { this.reviewScore = reviewScore; }
}

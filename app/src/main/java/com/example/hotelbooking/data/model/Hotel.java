package com.example.hotelbooking.data.model;

import java.io.Serializable;
import java.util.List;

public class Hotel implements Serializable {
    private String id;
    private String name;
    private String location;     // Lấy từ main (thay cho address )
    private double price;
    private float rating;
    private String imageUrl;     // Lấy từ main (thay cho mainImage )
    private String category;     // Bắt buộc phải có từ main để lọc danh mục
    private String description;
    
    // Các thuộc tính thêm vào cho màn hình Product Detail 
    private List<String> secondaryImages; 
    private List<String> amenities;
    private double latitude;
    private double longitude;

    public Hotel() {
    }

    // Constructor đã gộp đầy đủ các trường dữ liệu
    public Hotel(String id, String name, String location, double price, float rating, 
                 String imageUrl, String category, String description, 
                 List<String> secondaryImages, List<String> amenities, 
                 double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.category = category;
        this.description = description;
        this.secondaryImages = secondaryImages;
        this.amenities = amenities;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- Danh sách Getters và Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getSecondaryImages() { return secondaryImages; }
    public void setSecondaryImages(List<String> secondaryImages) { this.secondaryImages = secondaryImages; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
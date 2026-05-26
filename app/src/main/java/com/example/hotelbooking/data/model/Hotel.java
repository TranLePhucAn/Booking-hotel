package com.example.hotelbooking.data.model;

import java.io.Serializable;
import java.util.List;

public class Hotel implements Serializable {
    private String id;
    private String name;
    private String address;
    private String description;
    private double price;
    private float rating;
    private String mainImage;
    private List<String> secondaryImages;
    private List<String> amenities;
    private double latitude;
    private double longitude;

    public Hotel() {
    }

    public Hotel(String id, String name, String address, String description, double price, float rating, String mainImage, List<String> secondaryImages, List<String> amenities, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.mainImage = mainImage;
        this.secondaryImages = secondaryImages;
        this.amenities = amenities;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public List<String> getSecondaryImages() { return secondaryImages; }
    public void setSecondaryImages(List<String> secondaryImages) { this.secondaryImages = secondaryImages; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}

package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
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
    private String locationId;
    private int ratingStar;
    private int reviewCount;
    private String status;
    private String ownerId;

    public Hotel() {
    }

    public Hotel(String id, String name, String location, double price, float rating,
                 String imageUrl, String category, String description) {
        this(id, name, location, price, rating, imageUrl, category, description,
                null, null, 0, 0);
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

    public String getAddress() { return location; }
    public void setAddress(String address) { this.location = address; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getMainImage() { return imageUrl; }
    public void setMainImage(String mainImage) { this.imageUrl = mainImage; }

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

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public int getRatingStar() { return ratingStar; }
    public void setRatingStar(int ratingStar) { this.ratingStar = ratingStar; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public static Hotel fromDocument(DocumentSnapshot document) {
        Hotel hotel = new Hotel();
        hotel.setId(document.getId());
        hotel.setName(stringValue(document, "hotel_name", stringValue(document, "name", "")));
        hotel.setAddress(stringValue(document, "address_text", stringValue(document, "address", "")));
        hotel.setDescription(stringValue(document, "description", ""));
        hotel.setPrice(doubleValue(document, "price_from", doubleValue(document, "price", 0)));
        hotel.setRating((float) doubleValue(document, "review_score", doubleValue(document, "rating", 0)));
        hotel.setRatingStar((int) doubleValue(document, "rating_star", 0));
        hotel.setReviewCount((int) doubleValue(document, "review_count", 0));
        hotel.setImageUrl(stringValue(document, "image_url", stringValue(document, "mainImage", "")));
        hotel.setSecondaryImages(stringListValue(document, "image_urls"));
        hotel.setAmenities(stringListValue(document, "amenities"));
        hotel.setLocationId(stringValue(document, "location_id", ""));
        hotel.setCategory(stringValue(document, "category", stringValue(document, "business_type", "Khach san")));
        hotel.setStatus(stringValue(document, "status", ""));
        hotel.setOwnerId(stringValue(document, "owner_id", stringValue(document, "business_id", "")));
        return hotel;
    }

    private static String stringValue(DocumentSnapshot document, String field, String fallback) {
        String value = document.getString(field);
        return value == null ? fallback : value;
    }

    private static double doubleValue(DocumentSnapshot document, String field, double fallback) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return fallback;
    }

    private static List<String> stringListValue(DocumentSnapshot document, String field) {
        List<String> result = new ArrayList<>();
        Object value = document.get(field);
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
        }
        return result;
    }
}

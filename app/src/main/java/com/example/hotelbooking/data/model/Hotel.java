package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.PropertyName;
import com.example.hotelbooking.utils.AppConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class thông tin khách sạn
 */
public class Hotel implements Serializable {
    private String id;
    private String hotelName;
    private String address;
    private double price;
    private String imageUrl;
    private String category;
    private String description;

    private List<String> secondaryImages = new ArrayList<>();
    private List<String> amenities = new ArrayList<>();

    private double latitude;
    private double longitude;
    private String locationId;

    private double ratingStar;
    private double reviewScore;
    private int reviewCount;

    private String ownerId;
    private String approvalStatus;
    private boolean active;

    // Thuộc tính hỗ trợ sắp xếp, lọc và badges
    private boolean isFeatured;
    private long createdAt;
    private boolean isOffer;
    private boolean isSoldOut;

    public Hotel() {
        this.createdAt = System.currentTimeMillis();
    }

    public Hotel(String id, String hotelName, String address, double price, float ratingFallback,
                 String imageUrl, String category, String description,
                 List<String> secondaryImages, List<String> amenities,
                 double latitude, double longitude) {
        this.id = id;
        this.hotelName = hotelName;
        this.address = address;
        this.price = price;
        this.reviewScore = ratingFallback;
        this.imageUrl = imageUrl;
        this.category = category;
        this.description = description;
        this.secondaryImages = secondaryImages;
        this.amenities = amenities;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = System.currentTimeMillis();
    }

    public Hotel(String id, String hotelName, String address, double price, String imageUrl, String category, String description, List<String> secondaryImages, List<String> amenities, double latitude, double longitude, String locationId, double ratingStar, double reviewScore, int reviewCount, String status, String ownerId) {
        this.id = id;
        this.hotelName = hotelName;
        this.address = address;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.description = description;
        this.secondaryImages = secondaryImages;
        this.amenities = amenities;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationId = locationId;
        this.ratingStar = ratingStar;
        this.reviewScore = reviewScore;
        this.reviewCount = reviewCount;
        setStatus(status);
        this.ownerId = ownerId;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getStatus() { return approvalStatus; }
    public void setStatus(String status) {
        if ("active".equalsIgnoreCase(status)) {
            this.approvalStatus = AppConstants.STATUS_APPROVED;
            this.active = true;
        } else {
            this.approvalStatus = status;
        }
    }

    @PropertyName("approval_status")
    public String getApprovalStatus() { return approvalStatus; }
    @PropertyName("approval_status")
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    @PropertyName("is_active")
    public boolean isActive() { return active; }
    @PropertyName("is_active")
    public void setActive(boolean active) { this.active = active; }

    @PropertyName("hotel_name")
    public String getHotelName() { return hotelName; }
    @PropertyName("hotel_name")
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    @PropertyName("price")
    public double getPrice() { return price; }
    @PropertyName("price")
    public void setPrice(double price) { this.price = price; }

    @PropertyName("image_url")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("image_urls")
    public List<String> getSecondaryImages() { return secondaryImages; }
    @PropertyName("image_urls")
    public void setSecondaryImages(List<String> secondaryImages) { this.secondaryImages = secondaryImages; }

    @PropertyName("amenities")
    public List<String> getAmenities() { return amenities; }
    @PropertyName("amenities")
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    @PropertyName("location_id")
    public String getLocationId() { return locationId; }
    @PropertyName("location_id")
    public void setLocationId(String locationId) { this.locationId = locationId; }

    @PropertyName("rating_star")
    public double getRatingStar() { return ratingStar; }
    @PropertyName("rating_star")
    public void setRatingStar(double ratingStar) { this.ratingStar = ratingStar; }

    @PropertyName("review_score")
    public double getReviewScore() { return reviewScore; }
    @PropertyName("review_score")
    public void setReviewScore(double reviewScore) { this.reviewScore = reviewScore; }

    @PropertyName("review_count")
    public int getReviewCount() { return reviewCount; }
    @PropertyName("review_count")
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    @PropertyName("owner_id")
    public String getOwnerId() { return ownerId; }
    @PropertyName("owner_id")
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    @PropertyName("is_featured")
    public boolean isFeatured() { return isFeatured; }
    @PropertyName("is_featured")
    public void setFeatured(boolean featured) { isFeatured = featured; }

    @PropertyName("created_at")
    public long getCreatedAt() { return createdAt; }
    @PropertyName("created_at")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("is_offer")
    public boolean isOffer() { return isOffer; }
    @PropertyName("is_offer")
    public void setOffer(boolean offer) { isOffer = offer; }

    @PropertyName("is_sold_out")
    public boolean isSoldOut() { return isSoldOut; }
    @PropertyName("is_sold_out")
    public void setSoldOut(boolean soldOut) { isSoldOut = soldOut; }

    public float getRating() { return (float) ratingStar; }

    public static Hotel fromDocument(DocumentSnapshot document) {
        Hotel hotel = new Hotel();
        hotel.setId(document.getId());

        hotel.setHotelName(firstStringValue(document, "Khách sạn", "hotel_name", "name"));
        hotel.setAddress(firstStringValue(document, "Chưa cập nhật địa chỉ", "address", "address_text", "location"));
        hotel.setDescription(document.contains("description") ? document.getString("description") : "Chưa cập nhật mô tả");
        hotel.setCategory(document.contains("category") ? document.getString("category") : "Hotel");
        String approvalStatus = firstStringValue(document, "", "approval_status");
        if (approvalStatus.isEmpty() && "active".equalsIgnoreCase(document.getString("status"))) {
            approvalStatus = AppConstants.STATUS_APPROVED;
        }
        hotel.setApprovalStatus(approvalStatus.isEmpty() ? AppConstants.STATUS_PENDING : approvalStatus);
        Boolean isActive = document.getBoolean("is_active");
        hotel.setActive(isActive != null ? isActive : AppConstants.STATUS_APPROVED.equals(hotel.getApprovalStatus()));
        hotel.setImageUrl(firstStringValue(document, "", "image_url", "imageUrl"));
        hotel.setLocationId(document.contains("location_id") ? document.getString("location_id") : "");
        hotel.setOwnerId(firstStringValue(document, "", "owner_id", "ownerId"));

        hotel.setPrice(firstDoubleValue(document, 0.0, "price", "price_from"));

        hotel.setReviewScore(document.contains("review_score") && document.get("review_score") != null ?
                ((Number) document.get("review_score")).doubleValue() : 0.0);

        hotel.setRatingStar(firstDoubleValue(document, 0.0, "rating_star", "rating"));

        hotel.setLatitude(document.contains("latitude") && document.get("latitude") != null ?
                ((Number) document.get("latitude")).doubleValue() : 0.0);

        hotel.setLongitude(document.contains("longitude") && document.get("longitude") != null ?
                ((Number) document.get("longitude")).doubleValue() : 0.0);

        if (document.contains("review_count") && document.get("review_count") != null) {
            hotel.setReviewCount(((Number) document.get("review_count")).intValue());
        } else {
            hotel.setReviewCount(0);
        }

        hotel.setFeatured(document.contains("is_featured") && document.getBoolean("is_featured") != null && document.getBoolean("is_featured"));
        hotel.setOffer(document.contains("is_offer") && document.getBoolean("is_offer") != null && document.getBoolean("is_offer"));
        hotel.setSoldOut(document.contains("is_sold_out") && document.getBoolean("is_sold_out") != null && document.getBoolean("is_sold_out"));

        if (document.contains("created_at") && document.get("created_at") != null) {
            hotel.setCreatedAt(((Number) document.get("created_at")).longValue());
        } else {
            hotel.setCreatedAt(0);
        }

        hotel.setSecondaryImages(imageListValue(document, hotel.getImageUrl()));
        hotel.setAmenities(stringListValue(document, "amenities"));

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

    private static String firstStringValue(DocumentSnapshot document, String fallback, String... fields) {
        for (String field : fields) {
            String value = document.getString(field);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return fallback;
    }

    private static double firstDoubleValue(DocumentSnapshot document, double fallback, String... fields) {
        for (String field : fields) {
            Object value = document.get(field);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
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

    private static List<String> imageListValue(DocumentSnapshot document, String mainImageUrl) {
        List<String> result = stringListValue(document, "image_urls");
        addIfMissing(result, mainImageUrl);
        addIfMissing(result, firstStringValue(document, "", "imageUrl", "image_url"));
        return result;
    }

    private static void addIfMissing(List<String> values, String value) {
        if (value == null || value.trim().isEmpty() || values.contains(value)) {
            return;
        }
        values.add(value);
    }
}

package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class thông tin khách sạn
 */
public class Hotel implements Serializable {
    private String id; // id
    private String hotelName; // tên khách sạn
    private String address; // địa chỉ của khách sạn
    private double price; // Giá phòng tiêu chuẩn hoặc giá trung bình tính theo đêm
    private String imageUrl; // Đường dẫn link ảnh đại diện chính của khách sạn (Kiểu String URL)
    private String category; // Danh mục/Phân loại khách sạn (Dùng để lọc: Resort, Villa, Khách sạn 3 sao...)
    private String description; // mô tả chi tiết giới thiệu về khách sạn

    // phục vụ giao diện detail
    private List<String> secondaryImages = new ArrayList<>(); // Danh sách các link ảnh phụ để chạy slide hình ảnh
    private List<String> amenities = new ArrayList<>(); // Danh sách tiện ích (Ví dụ: Wifi, Hồ bơi, Điều hòa...)

    // Bản đồ và Định vị GPS
    private double latitude; // Vĩ độ (Tọa độ GPS phục vụ việc cắm ghim trên Google Maps)
    private double longitude; // Kinh độ (Tọa độ GPS phục vụ việc cắm ghim trên Google Maps)
    private String locationId; // Mã ID liên kết sang bảng "locations" để lấy thông tin Thành phố (Ví dụ: Hồ Chí Minh)

    // Điểm số và Đánh giá từ khách hàng
    private double ratingStar; // Số sao cấu trúc của khách sạn (Ví dụ: 2 sao, 4 sao, 5 sao)
    private double reviewScore; // Điểm số đánh giá trung bình từ khách hàng (Ví dụ: 8.3/10)
    private int reviewCount; // Tổng số lượng bài đánh giá/bình luận của khách hàng (Ví dụ: 84 đánh giá)

    // Quản lý hệ thống
    private String status; // Trạng thái hoạt động của khách sạn (Ví dụ: ACTIVE, MAINTENANCE, CLOSED)
    private String ownerId; // Mã ID tài khoản của chủ khách sạn (Dùng để phân quyền quản lý)

    // Thuộc tính hỗ trợ sắp xếp và lọc
    private boolean isFeatured; // Khách sạn nổi bật
    private long createdAt; // Thời gian tạo (dùng để xác định khách sạn mới)

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
        this.status = status;
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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

    public float getRating() { return (float) ratingStar; }

    public static Hotel fromDocument(DocumentSnapshot document) {
        Hotel hotel = new Hotel();
        hotel.setId(document.getId());

        hotel.setHotelName(document.contains("hotel_name") ? document.getString("hotel_name") : "Khách sạn");
        hotel.setAddress(document.contains("address") ? document.getString("address") : "Chưa cập nhật địa chỉ");
        hotel.setDescription(document.contains("description") ? document.getString("description") : "Chưa cập nhật mô tả");
        hotel.setAddress(firstStringValue(document, hotel.getAddress(), "address", "address_text"));
        hotel.setCategory(document.contains("category") ? document.getString("category") : "Hotel");
        hotel.setStatus(document.contains("status") ? document.getString("status") : "active");
        hotel.setImageUrl(firstStringValue(document, "", "image_url", "imageUrl"));
        hotel.setLocationId(document.contains("location_id") ? document.getString("location_id") : "");
        hotel.setOwnerId(firstStringValue(document, "", "owner_id", "ownerId"));

        hotel.setPrice(firstDoubleValue(document, 0.0, "price", "price_from"));

        hotel.setReviewScore(document.contains("review_score") && document.get("review_score") != null ?
                ((Number) document.get("review_score")).doubleValue() : 0.0);

        hotel.setRatingStar(document.contains("rating_star") && document.get("rating_star") != null ?
                ((Number) document.get("rating_star")).doubleValue() : 0.0);

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

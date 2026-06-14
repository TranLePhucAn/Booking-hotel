package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
// class thông tin khách sạn
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

    public Hotel() {
    }

    public Hotel(String id, String hotelName, String address, double price, float ratingFallback,
                 String imageUrl, String category, String description,
                 List<String> secondaryImages, List<String> amenities,
                 double latitude, double longitude) {
        this.id = id;
        this.hotelName = hotelName;
        this.address = address;
        this.price = price;
        this.reviewScore = ratingFallback; // Điểm số đánh giá truyền từ số thực 8.3f
        this.imageUrl = imageUrl;
        this.category = category;
        this.description = description;
        this.secondaryImages = secondaryImages;
        this.amenities = amenities;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public float getRating() { return (float) ratingStar; }

    public static Hotel fromDocument(DocumentSnapshot document) {
        Hotel hotel = new Hotel();
        hotel.setId(document.getId()); // Lấy ID Document từ Firestore (ví dụ: "hotel_001")

        hotel.setHotelName(stringValue(document, "hotel_name", stringValue(document, "name", "Khách sạn")));

        hotel.setAddress(stringValue(document, "address", stringValue(document, "address_text", "Chưa cập nhật địa chỉ")));

        hotel.setDescription(stringValue(document, "description", "Chưa cập nhật mô tả"));
        hotel.setPrice(doubleValue(document, "price", doubleValue(document, "price_from", 0)));
        hotel.setCategory(stringValue(document, "category", stringValue(document, "business_type", "Hotel")));
        hotel.setStatus(stringValue(document, "status", "ACTIVE"));

        hotel.setReviewScore(doubleValue(document, "review_score", doubleValue(document, "rating", 0)));
        hotel.setRatingStar(doubleValue(document, "rating_star", 0));
        hotel.setReviewCount((int) doubleValue(document, "review_count", 0));

        hotel.setImageUrl(stringValue(document, "image_url", stringValue(document, "mainImage", "")));
        hotel.setSecondaryImages(stringListValue(document, "image_urls"));
        hotel.setAmenities(stringListValue(document, "amenities"));

        hotel.setLatitude(doubleValue(document, "latitude", 0));
        hotel.setLongitude(doubleValue(document, "longitude", 0));
        hotel.setLocationId(stringValue(document, "location_id", ""));

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
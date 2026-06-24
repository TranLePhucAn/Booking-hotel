package com.example.hotelbooking.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hotel implements Serializable {
    private String id;
    private String name;
    private String address;
    private String location;
    private String description;
    private double price;
    private double rating;
    private double ratingStar;
    private String imageUrl;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> amenities = new ArrayList<>();
    private double latitude;
    private double longitude;
    private String ownerId;
    private String approvalStatus;
    private boolean isActive;
    private String adminNote;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Hotel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("name")
    public String getName() { return name; }
    @PropertyName("name")
    public void setName(String name) { this.name = name; }

    // Fallback cho code cũ dùng hotel_name
    @PropertyName("hotel_name")
    public String getHotelName() { return name; }
    @PropertyName("hotel_name")
    public void setHotelName(String hotelName) { this.name = hotelName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    @PropertyName("location_id")
    public String getLocationId() { return location; }
    @PropertyName("location_id")
    public void setLocationId(String locationId) { this.location = locationId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }


    @PropertyName("review_score")
    public double getReviewScore() { return rating; }
    @PropertyName("review_score")
    public void setReviewScore(double reviewScore) { this.rating = reviewScore; }

    @PropertyName("rating_star")
    public double getRatingStar() { return ratingStar; }
    @PropertyName("rating_star")
    public void setRatingStar(double ratingStar) { this.ratingStar = ratingStar; }

    @PropertyName("imageUrl")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


    @PropertyName("image_url")
    public String getImageUrlOld() { return imageUrl; }
    @PropertyName("image_url")
    public void setImageUrlOld(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("image_urls")
    public List<String> getImageUrls() { return imageUrls; }
    @PropertyName("image_urls")
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @PropertyName("owner_id")
    public String getOwnerId() { return ownerId; }
    @PropertyName("owner_id")
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    @PropertyName("ownerId")
    public String getOwnerIdCamel() { return ownerId; }
    @PropertyName("ownerId")
    public void setOwnerIdCamel(String ownerId) { this.ownerId = ownerId; }

    @PropertyName("approval_status")
    public String getApprovalStatus() { return approvalStatus; }
    @PropertyName("approval_status")
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    @PropertyName("is_active")
    public boolean getIsActive() { return isActive; }
    @PropertyName("is_active")
    public void setIsActive(boolean active) { isActive = active; }

    @PropertyName("admin_note")
    public String getAdminNote() { return adminNote; }
    @PropertyName("admin_note")
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    @PropertyName("created_at")
    public Timestamp getCreatedAt() { return createdAt; }
    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @PropertyName("updated_at")
    public Timestamp getUpdatedAt() { return updatedAt; }
    @PropertyName("updated_at")
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public static Hotel fromDocument(DocumentSnapshot doc) {
        Hotel h = doc.toObject(Hotel.class);
        if (h != null) h.setId(doc.getId());
        return h;
    }
}

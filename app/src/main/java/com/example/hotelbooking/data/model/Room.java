package com.example.hotelbooking.data.model;

import java.io.Serializable;
import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.List;
public class Room implements Serializable {
    private String id;
    private List<String> amenities = new ArrayList<>();
    private int availableRooms; // Số lượng phòng trống thực tế
    private String bedType;
    private int capacityAdults;
    private int capacityChildren;
    private String description;
    private int floor;
    private String hotelId;
    private String imageUrl;
    private double pricePerNight;
    private String roomName;
    private double roomSize;
    private String roomType;
    private String sectionId; // ID liên kết ngược về hạng phòng Section
    private String status;

    public Room() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @PropertyName("available_rooms")
    public int getAvailableRooms() { return availableRooms; }
    @PropertyName("available_rooms")
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    @PropertyName("bed_type")
    public String getBedType() { return bedType; }
    @PropertyName("bed_type")
    public void setBedType(String bedType) { this.bedType = bedType; }

    @PropertyName("capacity_adults")
    public int getCapacityAdults() { return capacityAdults; }
    @PropertyName("capacity_adults")
    public void setCapacityAdults(int capacityAdults) { this.capacityAdults = capacityAdults; }

    @PropertyName("capacity_children")
    public int getCapacityChildren() { return capacityChildren; }
    @PropertyName("capacity_children")
    public void setCapacityChildren(int capacityChildren) { this.capacityChildren = capacityChildren; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    @PropertyName("hotel_id")
    public String getHotelId() { return hotelId; }
    @PropertyName("hotel_id")
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    @PropertyName("image_url")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("price_per_night")
    public double getPricePerNight() { return pricePerNight; }
    @PropertyName("price_per_night")
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    @PropertyName("room_name")
    public String getRoomName() { return roomName; }
    @PropertyName("room_name")
    public void setRoomName(String roomName) { this.roomName = roomName; }

    @PropertyName("room_size")
    public double getRoomSize() { return roomSize; }
    @PropertyName("room_size")
    public void setRoomSize(double roomSize) { this.roomSize = roomSize; }

    @PropertyName("room_type")
    public String getRoomType() { return roomType; }
    @PropertyName("room_type")
    public void setRoomType(String roomType) { this.roomType = roomType; }

    @PropertyName("section_id")
    public String getSectionId() { return sectionId; }
    @PropertyName("section_id")
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
}

package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
// class phòng chứa thông tin khách sạn, thông tin chi tiết phòng trạng thái (status) đã đặt phòng hay chưa
public class Room implements Serializable {
    private String id;
    private int floor;
    private String hotelId;
    private String sectionId;
    private String status;

    public Room(){}

    public Room(String id, int floor, String hotelId, String sectionId, String status) {
        this.id = id;
        this.floor = floor;
        this.hotelId = hotelId;
        this.sectionId = sectionId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("floor")
    public int getFloor() {
        return floor;
    }
    @PropertyName("floor")
    public void setFloor(int floor) {
        this.floor = floor;
    }
    @PropertyName("hotel_id")
    public String getHotelId() {
        return hotelId;
    }
    @PropertyName("hotel_id")
    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }
    @PropertyName("section_id")
    public String getSectionId() {
        return sectionId;
    }
    @PropertyName("section_id")
    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
    @PropertyName("status")
    public String getStatus() {
        return status;
    }
    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }
}

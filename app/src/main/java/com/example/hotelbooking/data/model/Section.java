package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
// class chứa thông tin Hạng phòng / Loại phòng
public class Section implements Serializable {
    private String id;
    private double basePrice;
    private String hotelId;
    private boolean isRefundable;
    private boolean isReschedulable;
    private int maxGuests;
    private String roomStyle;
    public Section() {}

    public Section(String id, double basePrice, String hotelId, boolean isRefundable, boolean isReschedulable, int maxGuests, String roomStyle) {
        this.id = id;
        this.basePrice = basePrice;
        this.hotelId = hotelId;
        this.isRefundable = isRefundable;
        this.isReschedulable = isReschedulable;
        this.maxGuests = maxGuests;
        this.roomStyle = roomStyle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @PropertyName("base_price")
    public double getBasePrice() {
        return basePrice;
    }
    @PropertyName("base_price")
    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
    @PropertyName("hotel_id")
    public String getHotelId() {
        return hotelId;
    }
    @PropertyName("hotel_id")
    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }
    @PropertyName("is_refundable")
    public boolean isRefundable() {
        return isRefundable;
    }
    @PropertyName("is_refundable")
    public void setRefundable(boolean refundable) {
        isRefundable = refundable;
    }
    @PropertyName("is_reschedulable")
    public boolean isReschedulable() {
        return isReschedulable;
    }
    @PropertyName("is_reschedulable")
    public void setReschedulable(boolean reschedulable) {
        isReschedulable = reschedulable;
    }
    @PropertyName("max_guests")
    public int getMaxGuests() {
        return maxGuests;
    }
    @PropertyName("max_guests")
    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }
    @PropertyName("room_style")
    public String getRoomStyle() {
        return roomStyle;
    }
    @PropertyName("room_style")
    public void setRoomStyle(String roomStyle) {
        this.roomStyle = roomStyle;
    }
}
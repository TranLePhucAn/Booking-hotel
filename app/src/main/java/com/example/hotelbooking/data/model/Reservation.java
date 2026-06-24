package com.example.hotelbooking.data.model;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.Timestamp; // ĐÃ SỬA: Dùng Timestamp chuẩn của Firebase

import java.io.Serializable;

// class xử lý lưu thông tin đặt phòng
public class Reservation implements Serializable {
    private String id;
    private double basePrice;
    private Timestamp createdAt;
    private String customerId;
    private Timestamp dayEnd;
    private Timestamp dayStart;
    private double discountPrice;
    private String guestEmail;
    private String guestName;
    private String guestPhone;
    private String hotelId;
    private Timestamp paymentDeadline;
    private String roomId;
    private String sectionId;
    private String status;
    private double taxFee;
    private double totalPrice;
    private String paymentMethod;
    private Timestamp paidAt;

    public Reservation() {}

    public Reservation(String id, double basePrice, Timestamp createdAt, String customerId, Timestamp dayEnd, Timestamp dayStart, double discountPrice, String guestEmail, String guestName, String guestPhone, String hotelId, Timestamp paymentDeadline, String roomId, String sectionId, String status, double taxFee, double totalPrice, String paymentMethod, Timestamp paidAt) {
        this.id = id;
        this.basePrice = basePrice;
        this.createdAt = createdAt;
        this.customerId = customerId;
        this.dayEnd = dayEnd;
        this.dayStart = dayStart;
        this.discountPrice = discountPrice;
        this.guestEmail = guestEmail;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.hotelId = hotelId;
        this.paymentDeadline = paymentDeadline;
        this.roomId = roomId;
        this.sectionId = sectionId;
        this.status = status;
        this.taxFee = taxFee;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
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

    @PropertyName("created_at")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("customer_id")
    public String getCustomerId() {
        return customerId;
    }

    @PropertyName("customer_id")
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @PropertyName("day_end")
    public Timestamp getDayEnd() {
        return dayEnd;
    }

    @PropertyName("day_end")
    public void setDayEnd(Timestamp dayEnd) {
        this.dayEnd = dayEnd;
    }

    @PropertyName("day_start")
    public Timestamp getDayStart() {
        return dayStart;
    }

    @PropertyName("day_start")
    public void setDayStart(Timestamp dayStart) {
        this.dayStart = dayStart;
    }

    @PropertyName("discount_price")
    public double getDiscountPrice() {
        return discountPrice;
    }

    @PropertyName("discount_price")
    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    @PropertyName("guest_email")
    public String getGuestEmail() {
        return guestEmail;
    }

    @PropertyName("guest_email")
    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    @PropertyName("guest_name")
    public String getGuestName() {
        return guestName;
    }

    @PropertyName("guest_name")
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    @PropertyName("guest_phone")
    public String getGuestPhone() {
        return guestPhone;
    }

    @PropertyName("guest_phone")
    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    @PropertyName("hotel_id")
    public String getHotelId() {
        return hotelId;
    }

    @PropertyName("hotel_id")
    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    @PropertyName("payment_deadline")
    public Timestamp getPaymentDeadline() {
        return paymentDeadline;
    }

    @PropertyName("payment_deadline")
    public void setPaymentDeadline(Timestamp paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    @PropertyName("payment_method")
    public String getPaymentMethod() {
        return paymentMethod;
    }

    @PropertyName("payment_method")
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @PropertyName("paid_at")
    public Timestamp getPaidAt() {
        return paidAt;
    }

    @PropertyName("paid_at")
    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    @PropertyName("room_id")
    public String getRoomId() {
        return roomId;
    }

    @PropertyName("room_id")
    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    @PropertyName("tax_fee")
    public double getTaxFee() {
        return taxFee;
    }

    @PropertyName("tax_fee")
    public void setTaxFee(double taxFee) {
        this.taxFee = taxFee;
    }

    @PropertyName("total_price")
    public double getTotalPrice() {
        return totalPrice;
    }

    @PropertyName("total_price")
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
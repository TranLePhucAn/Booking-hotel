package com.example.hotelbooking.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoHotelData {

    public static class DemoRoom {
        public final String id;
        public final String name;
        public final String type;
        public final String bedType;
        public final String capacity;
        public final double price;
        public final int availableRooms;
        public final String status;
        public final double size;

        public DemoRoom(String id, String name, String type, String bedType, String capacity,
                        double price, int availableRooms, String status, double size) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.bedType = bedType;
            this.capacity = capacity;
            this.price = price;
            this.availableRooms = availableRooms;
            this.status = status;
            this.size = size;
        }
    }

    public static class DemoReview {
        public final String userName;
        public final double rating;
        public final String comment;

        public DemoReview(String userName, double rating, String comment) {
            this.userName = userName;
            this.rating = rating;
            this.comment = comment;
        }
    }

    private DemoHotelData() {
    }

    public static List<Hotel> hotels() {
        List<Hotel> hotels = new ArrayList<>();

        Hotel passion = new Hotel(
                "demo_passion_lux",
                "The Passion Lux Airport",
                "119 Duong Bach Dang, Phuong 2, Tan Binh, TP.HCM",
                500000,
                8.3f,
                "https://pix10.agoda.net/hotelImages/56801030/-1/96cc40d590a674ca39caebda5879fe97.jpg",
                "Khach san",
                "Khach san gan san bay, phong hien dai, phu hop cho du lich va cong tac.",
                Arrays.asList(
                        "https://pix10.agoda.net/hotelImages/56801030/-1/96cc40d590a674ca39caebda5879fe97.jpg",
                        "https://pix10.agoda.net/hotelImages/56801030/-1/ea01218c2f62d0c80f2f22e54553d748.jpg",
                        "https://pix10.agoda.net/hotelImages/56801030/-1/f8c527a2f31b0c2a141ddc884b7d7cd8.jpg"
                ),
                Arrays.asList("Wifi mien phi", "May lanh", "Bai dau xe", "Le tan 24/7", "Dua don san bay"),
                10.8131,
                106.6658
        );
        passion.setRatingStar(2);
        passion.setReviewCount(84);
        passion.setStatus("active");
        hotels.add(passion);

        Hotel binhYen = new Hotel(
                "demo_binh_yen",
                "Khach San Binh Yen",
                "Vung Tau",
                650000,
                8.7f,
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200",
                "Resort",
                "Khach san yen tinh gan bien, co phong VIP huong bien va dich vu than thien.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200",
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200",
                        "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=1200"
                ),
                Arrays.asList("Wifi mien phi", "Ho boi", "Gan bien", "Bua sang", "Bai dau xe"),
                10.3460,
                107.0843
        );
        binhYen.setRatingStar(4);
        binhYen.setReviewCount(126);
        binhYen.setStatus("active");
        hotels.add(binhYen);

        Hotel ocean = new Hotel(
                "demo_ocean_breeze",
                "Ocean Breeze Hotel",
                "Tran Phu, Nha Trang",
                720000,
                8.9f,
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200",
                "Khach san",
                "Khach san ven bien voi phong rong, view dep va khu vuc trung tam.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200"
                ),
                Arrays.asList("Wifi", "Nha hang", "Ho boi", "Gym", "View bien"),
                12.2388,
                109.1967
        );
        ocean.setRatingStar(4);
        ocean.setReviewCount(98);
        ocean.setStatus("active");
        hotels.add(ocean);

        return hotels;
    }

    public static Hotel findHotel(String hotelId) {
        for (Hotel hotel : hotels()) {
            if (hotel.getId().equals(hotelId)) {
                return hotel;
            }
        }
        return null;
    }

    public static List<DemoRoom> rooms(String hotelId) {
        if ("demo_binh_yen".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_binh_yen_vip", "Phong VIP huong bien", "VIP", "1 giuong doi lon",
                            "2 nguoi lon, 1 tre em", 1500000, 5, "AVAILABLE", 42),
                    new DemoRoom("demo_binh_yen_standard", "Standard Room", "Standard", "1 giuong doi",
                            "2 nguoi lon", 650000, 3, "AVAILABLE", 28)
            );
        }

        if ("demo_ocean_breeze".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_ocean_deluxe", "Deluxe Sea View", "Deluxe", "1 giuong doi",
                            "2 nguoi lon", 980000, 4, "AVAILABLE", 35),
                    new DemoRoom("demo_ocean_family", "Family Room", "Family", "2 giuong doi",
                            "4 nguoi lon", 1350000, 0, "SUSPENDED", 45)
            );
        }

        return Arrays.asList(
                new DemoRoom("demo_passion_deluxe", "Deluxe Double Room", "Phong Deluxe", "1 giuong doi",
                        "2 nguoi lon, 1 tre em", 650000, 5, "AVAILABLE", 32),
                new DemoRoom("demo_passion_standard", "Standard Room", "Phong Standard", "1 giuong doi",
                        "2 nguoi lon", 450000, 2, "AVAILABLE", 24)
        );
    }

    public static List<DemoReview> reviews(String hotelId) {
        if ("demo_binh_yen".equals(hotelId)) {
            return Arrays.asList(
                    new DemoReview("Tran Minh", 5, "Phong VIP huong bien dep, nhan vien ho tro nhanh."),
                    new DemoReview("Le Phuong", 4, "Khach san sach se, vi tri thuan tien o Vung Tau.")
            );
        }

        return Arrays.asList(
                new DemoReview("Nguyen Van A", 5, "Khach san sach se, gan san bay, nhan vien than thien."),
                new DemoReview("Hoang My", 4, "Phong dep, gia hop ly, di chuyen ra san bay nhanh.")
        );
    }
}

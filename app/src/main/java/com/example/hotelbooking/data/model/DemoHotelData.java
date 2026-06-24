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

        Hotel passion = new Hotel();
        passion.setId("demo_passion_lux");
        passion.setName("The Passion Lux Airport");
        passion.setAddress("119 Duong Bach Dang, Phuong 2, Tan Binh, TP.HCM");
        passion.setLocation("TP.HCM");
        passion.setPrice(500000);
        passion.setRating(8.3);
        passion.setImageUrl("https://pix10.agoda.net/hotelImages/56801030/-1/96cc40d590a674ca39caebda5879fe97.jpg");
        passion.setDescription("Khách sạn gần sân bay, phòng hiện đại, phù hợp cho du lịch và công tác.");
        passion.setImageUrls(Arrays.asList(
                "https://pix10.agoda.net/hotelImages/56801030/-1/96cc40d590a674ca39caebda5879fe97.jpg",
                "https://pix10.agoda.net/hotelImages/56801030/-1/ea01218c2f62d0c80f2f22e54553d748.jpg",
                "https://pix10.agoda.net/hotelImages/56801030/-1/f8c527a2f31b0c2a141ddc884b7d7cd8.jpg"
        ));
        passion.setAmenities(Arrays.asList("Wifi miễn phí", "Máy lạnh", "Bãi đậu xe", "Lễ tân 24/7", "Đưa đón sân bay"));
        passion.setLatitude(10.8131);
        passion.setLongitude(106.6658);
        passion.setRatingStar(2);
        passion.setIsActive(true);
        passion.setApprovalStatus("approved");
        hotels.add(passion);

        Hotel binhYen = new Hotel();
        binhYen.setId("demo_binh_yen");
        binhYen.setName("Khách Sạn Bình Yên");
        binhYen.setAddress("Vũng Tàu");
        binhYen.setLocation("Vũng Tàu");
        binhYen.setPrice(650000);
        binhYen.setRating(8.7);
        binhYen.setImageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200");
        binhYen.setDescription("Khách sạn yên tĩnh gần biển, có phòng VIP hướng biển và dịch vụ thân thiện.");
        binhYen.setImageUrls(Arrays.asList(
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200",
                "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200",
                "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=1200"
        ));
        binhYen.setAmenities(Arrays.asList("Wifi miễn phí", "Hồ bơi", "Gần biển", "Bữa sáng", "Bãi đậu xe"));
        binhYen.setLatitude(10.3460);
        binhYen.setLongitude(107.0843);
        binhYen.setRatingStar(4);
        binhYen.setIsActive(true);
        binhYen.setApprovalStatus("approved");
        hotels.add(binhYen);

        Hotel ocean = new Hotel();
        ocean.setId("demo_ocean_breeze");
        ocean.setName("Ocean Breeze Hotel");
        ocean.setAddress("Trần Phú, Nha Trang");
        ocean.setLocation("Nha Trang");
        ocean.setPrice(720000);
        ocean.setRating(8.9);
        ocean.setImageUrl("https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200");
        ocean.setDescription("Khách sạn ven biển với phòng rộng, view đẹp và khu vực trung tâm.");
        ocean.setImageUrls(Arrays.asList(
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200",
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200"
        ));
        ocean.setAmenities(Arrays.asList("Wifi", "Nhà hàng", "Hồ bơi", "Gym", "View biển"));
        ocean.setLatitude(12.2388);
        ocean.setLongitude(109.1967);
        ocean.setRatingStar(4);
        ocean.setIsActive(true);
        ocean.setApprovalStatus("approved");
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
                    new DemoRoom("demo_binh_yen_vip", "Phòng VIP hướng biển", "VIP", "1 giường đôi lớn",
                            "2 người lớn, 1 trẻ em", 1500000, 5, "AVAILABLE", 42),
                    new DemoRoom("demo_binh_yen_standard", "Standard Room", "Standard", "1 giường đôi",
                            "2 người lớn", 650000, 3, "AVAILABLE", 28)
            );
        }

        if ("demo_ocean_breeze".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_ocean_deluxe", "Deluxe Sea View", "Deluxe", "1 giường đôi",
                            "2 người lớn", 980000, 4, "AVAILABLE", 35),
                    new DemoRoom("demo_ocean_family", "Family Room", "Family", "2 giường đôi",
                            "4 người lớn", 1350000, 0, "SUSPENDED", 45)
            );
        }

        return Arrays.asList(
                new DemoRoom("demo_passion_deluxe", "Deluxe Double Room", "Phòng Deluxe", "1 giường đôi",
                        "2 người lớn, 1 trẻ em", 650000, 5, "AVAILABLE", 32),
                new DemoRoom("demo_passion_standard", "Standard Room", "Phòng Standard", "1 giường đôi",
                        "2 người lớn", 450000, 2, "AVAILABLE", 24)
        );
    }

    public static List<DemoReview> reviews(String hotelId) {
        if ("demo_binh_yen".equals(hotelId)) {
            return Arrays.asList(
                    new DemoReview("Trần Minh", 5, "Phòng VIP hướng biển đẹp, nhân viên hỗ trợ nhanh."),
                    new DemoReview("Lê Phương", 4, "Khách sạn sạch sẽ, vị trí thuận tiện ở Vũng Tàu.")
            );
        }

        return Arrays.asList(
                new DemoReview("Nguyễn Văn A", 5, "Khách sạn sạch sẽ, gần sân bay, nhân viên thân thiện."),
                new DemoReview("Hoàng Mỹ", 4, "Phòng đẹp, giá hợp lý, di chuyển ra sân bay nhanh.")
        );
    }
}

package com.example.hotelbooking.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.hotelbooking.utils.AppConstants;

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
        public final String imageUrl;

        public DemoRoom(String id, String name, String type, String bedType, String capacity,
                        double price, int availableRooms, String status, double size) {
            this(id, name, type, bedType, capacity, price, availableRooms, status, size, "");
        }

        public DemoRoom(String id, String name, String type, String bedType, String capacity,
                        double price, int availableRooms, String status, double size, String imageUrl) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.bedType = bedType;
            this.capacity = capacity;
            this.price = price;
            this.availableRooms = availableRooms;
            this.status = status;
            this.size = size;
            this.imageUrl = imageUrl;
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
                "119 Đường Bạch Đằng, Phường 2, Tân Bình, TP.HCM",
                500000,
                8.3f,
                "https://kientructrangkim.com/wp-content/uploads/2019/09/tieu-chuan-khach-san-5-sao-c.jpg",
                "Khách sạn",
                "Khách sạn gần sân bay, phòng hiện đại, phù hợp cho du lịch và công tác.",
                Arrays.asList(
                        "https://pix10.agoda.net/hotelImages/56801030/-1/96cc40d590a674ca39caebda5879fe97.jpg",
                        "https://pix10.agoda.net/hotelImages/56801030/-1/ea01218c2f62d0c80f2f22e54553d748.jpg",
                        "https://pix10.agoda.net/hotelImages/56801030/-1/f8c527a2f31b0c2a141ddc884b7d7cd8.jpg"
                ),
                Arrays.asList("Wifi miễn phí", "Máy lạnh", "Bãi đậu xe", "Lễ tân 24/7", "Đưa đón sân bay"),
                10.8131,
                106.6658
        );
        passion.setRatingStar(2);
        passion.setReviewCount(84);
        markApproved(passion);
        hotels.add(passion);

        Hotel binhYen = new Hotel(
                "demo_binh_yen",
                "Khách Sạn Bình Yên",
                "Vung Tau",
                650000,
                8.7f,
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTsQEGBt--p2A_0oB2eHqQM0-gkCkPUcFBShC_D7pfgMy5vprgvhBs10kjf&s=10",
                "Resort",
                "Khách sạn yên tĩnh gần biển, có phòng VIP hướng biển và dịch vụ thân thiện.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200",
                        "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=1200",
                        "https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=1200"
                ),
                Arrays.asList("Wifi miễn phí", "Hồ bơi", "Gần biển", "Bữa sáng", "Bãi đậu xe"),
                10.3460,
                107.0843
        );
        binhYen.setRatingStar(4);
        binhYen.setReviewCount(126);
        markApproved(binhYen);
        hotels.add(binhYen);

        Hotel ocean = new Hotel(
                "demo_ocean_breeze",
                "Ocean Breeze Hotel",
                "Tran Phu, Nha Trang",
                720000,
                8.9f,
                "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200",
                "Khách sạn",
                "Khách sạn ven bien voi phòng rong, view dep va khu vuc trung tam.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=1200",
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200"
                ),
                Arrays.asList("Wifi", "Nhà hàng", "Hồ bơi", "Gym", "View biển"),
                12.2388,
                109.1967
        );
        ocean.setRatingStar(4);
        ocean.setReviewCount(98);
        markApproved(ocean);
        hotels.add(ocean);

        Hotel lotus = new Hotel(
                "demo_lotus_saigon",
                "Lotus Saigon Boutique",
                "Le Thanh Ton, Quan 1, TP.HCM",
                880000,
                9.0f,
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=1200",
                "Khách sạn",
                "Khách sạn boutique trung tâm thành phố, thuận tiện đi bộ đến khu ẩm thực và mua sắm.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=1200",
                        "https://images.unsplash.com/photo-1568084680786-a84f91d1153c?w=1200",
                        "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200"
                ),
                Arrays.asList("Wifi", "Bữa sáng", "Nhà hàng", "Gym", "Đưa đón sân bay"),
                10.7789,
                106.7042
        );
        lotus.setRatingStar(4);
        lotus.setReviewCount(212);
        markApproved(lotus);
        hotels.add(lotus);

        Hotel pine = new Hotel(
                "demo_pine_dalat",
                "Pine Hill Đà Lạt Retreat",
                "Duong Trieu Viet Vuong, Đà Lạt",
                780000,
                8.8f,
                "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=1200",
                "Villa",
                "Không gian nghỉ dưỡng yên tĩnh trên đồi thông, phù hợp gia đình và cặp đôi.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=1200",
                        "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=1200",
                        "https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=1200"
                ),
                Arrays.asList("Wifi", "Lò sưởi", "Sân vườn", "Bữa sáng", "Bãi đậu xe"),
                11.9251,
                108.4380
        );
        pine.setRatingStar(4);
        pine.setReviewCount(147);
        markApproved(pine);
        hotels.add(pine);

        Hotel river = new Hotel(
                "demo_river_hanoi",
                "River View Ha Noi",
                "Tran Quang Khai, Hoan Kiem, Ha Noi",
                920000,
                8.6f,
                "https://images.unsplash.com/photo-1563911302283-d2bc129e7570?w=1200",
                "Căn hộ",
                "Căn hộ dịch vụ gần phố cổ, có bếp nhỏ và tầm nhìn ra sông.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1563911302283-d2bc129e7570?w=1200",
                        "https://images.unsplash.com/photo-1560185893-a55cbc8c57e8?w=1200",
                        "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200"
                ),
                Arrays.asList("Wifi", "Bep nho", "May giat", "Le tan", "Gần phố cổ"),
                21.0328,
                105.8542
        );
        river.setRatingStar(4);
        river.setReviewCount(175);
        markApproved(river);
        hotels.add(river);

        Hotel sunbay = new Hotel(
                "demo_sunbay_danang",
                "Sunbay Da Nang Resort",
                "Vo Nguyen Giap, Da Nang",
                1100000,
                9.2f,
                "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=1200",
                "Resort",
                "Resort gần biển Mỹ Khê, có hồ bơi lớn và các hạng phòng view biển.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=1200",
                        "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=1200",
                        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200"
                ),
                Arrays.asList("Wifi", "Hồ bơi", "Spa", "View biển", "Bữa sáng buffet"),
                16.0678,
                108.2453
        );
        sunbay.setRatingStar(5);
        sunbay.setReviewCount(241);
        markApproved(sunbay);
        hotels.add(sunbay);

        Hotel garden = new Hotel(
                "demo_garden_hoian",
                "Hội An Garden Homestay",
                "Cam Chau, Hội An",
                520000,
                8.5f,
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200",
                "Homestay",
                "Homestay ấm cúng gần phố cổ Hội An, có sân vườn và xe đạp miễn phí.",
                Arrays.asList(
                        "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200",
                        "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=1200",
                        "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?w=1200"
                ),
                Arrays.asList("Wifi", "Xe đạp", "Sân vườn", "Bữa sáng", "Gần phố cổ"),
                15.8801,
                108.3380
        );
        garden.setRatingStar(3);
        garden.setReviewCount(93);
        markApproved(garden);
        hotels.add(garden);

        return hotels;
    }

    private static void markApproved(Hotel hotel) {
        hotel.setApprovalStatus(AppConstants.STATUS_APPROVED);
        hotel.setActive(true);
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
                            "2 người lớn, 1 trẻ em", 1500000, 5, "AVAILABLE", 42,
                            "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200"),
                    new DemoRoom("demo_binh_yen_standard", "Standard Room", "Standard", "1 giường đôi",
                            "2 người lớn", 650000, 3, "AVAILABLE", 28,
                            "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=1200"),
                    new DemoRoom("demo_binh_yen_family", "Family Garden Room", "Family", "2 giường đôi",
                            "4 người lớn", 1200000, 2, "AVAILABLE", 48,
                            "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200"),
                    new DemoRoom("demo_binh_yen_suite", "Suite Balcony", "Suite", "1 giường king",
                            "2 người lớn", 1850000, 1, "AVAILABLE", 55,
                            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200")
            );
        }

        if ("demo_ocean_breeze".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_ocean_deluxe", "Deluxe Sea View", "Deluxe", "1 giường đôi",
                            "2 người lớn", 980000, 4, "AVAILABLE", 35,
                            "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200"),
                    new DemoRoom("demo_ocean_family", "Family Room", "Family", "2 giường đôi",
                            "4 người lớn", 1350000, 0, "SUSPENDED", 45,
                            "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=1200"),
                    new DemoRoom("demo_ocean_superior", "Superior City View", "Superior", "1 giường queen",
                            "2 người lớn", 820000, 6, "AVAILABLE", 30,
                            "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200"),
                    new DemoRoom("demo_ocean_suite", "Ocean Suite", "Suite", "1 giường king",
                            "2 người lớn, 1 trẻ em", 1800000, 2, "AVAILABLE", 58,
                            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200")
            );
        }

        if ("demo_lotus_saigon".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_lotus_classic", "Classic Double", "Classic", "1 giường queen",
                            "2 người lớn", 880000, 7, "AVAILABLE", 28,
                            "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=1200"),
                    new DemoRoom("demo_lotus_executive", "Executive City View", "Executive", "1 giường king",
                            "2 người lớn", 1250000, 4, "AVAILABLE", 36,
                            "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200"),
                    new DemoRoom("demo_lotus_twin", "Twin Business", "Business", "2 giường đơn",
                            "2 người lớn", 980000, 5, "AVAILABLE", 32,
                            "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200")
            );
        }

        if ("demo_pine_dalat".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_pine_standard", "Pine Standard", "Standard", "1 giường queen",
                            "2 người lớn", 780000, 3, "AVAILABLE", 30,
                            "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200"),
                    new DemoRoom("demo_pine_family", "Family Attic", "Family", "2 giường đôi",
                            "4 người lớn", 1350000, 2, "AVAILABLE", 52,
                            "https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=1200"),
                    new DemoRoom("demo_pine_villa", "Private Villa", "Villa", "2 phòng ngủ",
                            "4 người lớn, 2 trẻ em", 2400000, 1, "AVAILABLE", 85,
                            "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=1200")
            );
        }

        if ("demo_river_hanoi".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_river_studio", "Studio Apartment", "Studio", "1 giường queen",
                            "2 người lớn", 920000, 6, "AVAILABLE", 34,
                            "https://images.unsplash.com/photo-1560185893-a55cbc8c57e8?w=1200"),
                    new DemoRoom("demo_river_onebed", "One Bedroom River View", "Apartment", "1 giường king",
                            "2 người lớn, 1 trẻ em", 1280000, 3, "AVAILABLE", 45,
                            "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200"),
                    new DemoRoom("demo_river_twobed", "Two Bedroom Suite", "Suite", "2 phòng ngủ",
                            "4 người lớn", 1950000, 2, "AVAILABLE", 70,
                            "https://images.unsplash.com/photo-1563911302283-d2bc129e7570?w=1200")
            );
        }

        if ("demo_sunbay_danang".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_sunbay_deluxe", "Deluxe Ocean", "Deluxe", "1 giường king",
                            "2 người lớn", 1100000, 5, "AVAILABLE", 38,
                            "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200"),
                    new DemoRoom("demo_sunbay_pool", "Pool Access Room", "Pool Access", "1 giường king",
                            "2 người lớn, 1 trẻ em", 1650000, 3, "AVAILABLE", 45,
                            "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=1200"),
                    new DemoRoom("demo_sunbay_family", "Family Ocean Suite", "Family Suite", "2 giường đôi",
                            "4 người lớn", 2200000, 2, "AVAILABLE", 68,
                            "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=1200")
            );
        }

        if ("demo_garden_hoian".equals(hotelId)) {
            return Arrays.asList(
                    new DemoRoom("demo_garden_double", "Garden Double", "Double", "1 giường queen",
                            "2 người lớn", 520000, 4, "AVAILABLE", 26,
                            "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=1200"),
                    new DemoRoom("demo_garden_family", "Family Garden", "Family", "2 giường đôi",
                            "4 người lớn", 880000, 2, "AVAILABLE", 40,
                            "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?w=1200"),
                    new DemoRoom("demo_garden_balcony", "Balcony Room", "Balcony", "1 giường queen",
                            "2 người lớn", 680000, 3, "AVAILABLE", 30,
                            "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1200")
            );
        }

        return Arrays.asList(
                new DemoRoom("demo_passion_deluxe", "Deluxe Double Room", "Phòng Deluxe", "1 giường đôi",
                        "2 người lớn, 1 trẻ em", 650000, 5, "AVAILABLE", 32,
                        "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200"),
                new DemoRoom("demo_passion_standard", "Standard Room", "Phòng Standard", "1 giường đôi",
                        "2 người lớn", 450000, 2, "AVAILABLE", 24,
                        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=1200"),
                new DemoRoom("demo_passion_twin", "Twin Airport Room", "Twin", "2 giường đơn",
                        "2 người lớn", 580000, 4, "AVAILABLE", 30,
                        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200"),
                new DemoRoom("demo_passion_family", "Family Airport Suite", "Family", "2 giường đôi",
                        "4 người lớn", 980000, 1, "AVAILABLE", 46,
                        "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200")
            );
        }

    public static List<DemoReview> reviews(String hotelId) {
        if ("demo_binh_yen".equals(hotelId)) {
            return Arrays.asList(
                    new DemoReview("Tran Minh", 5, "Phòng VIP hướng biển đẹp, nhân viên hỗ trợ nhanh."),
                    new DemoReview("Le Phuong", 4, "Khách sạn sạch sẽ, vị trí thuận tiện ở Vũng Tàu.")
            );
        }

        return Arrays.asList(
                new DemoReview("Nguyen Van A", 5, "Khách sạn sạch sẽ, gần sân bay, nhân viên thân thiện."),
                new DemoReview("Hoang My", 4, "Phòng đẹp, giá hợp lý, di chuyển ra sân bay nhanh.")
        );
    }

    public static List<String> getLocations() {
        return Arrays.asList(
                "TP.HCM", "Ha Noi", "Da Nang", "Nha Trang", "Vung Tau",
                "Đà Lạt", "Hội An", "Phú Quốc", "Huế", "Cần Thơ", "Sapa", "Hạ Long"
        );
    }
}

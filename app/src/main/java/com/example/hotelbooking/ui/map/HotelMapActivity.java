package com.example.hotelbooking.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.hotelbooking.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class HotelMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView osmMap;
    private TextView tvMapDistance;
    private FusedLocationProviderClient fusedLocationClient;

    private String hotelName;
    private String address;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình User Agent bắt buộc cho Osmdroid để tránh bị chặn tải bản đồ
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_hotel_map);

        // Nhận dữ liệu từ Intent gửi qua
        hotelName = getIntent().getStringExtra("hotel_name");
        address = getIntent().getStringExtra("address");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        TextView tvMapHotelName = findViewById(R.id.tvMapHotelName);
        TextView tvMapAddress = findViewById(R.id.tvMapAddress);
        tvMapDistance = findViewById(R.id.tvMapDistance);

        tvMapHotelName.setText("Vị trí khách sạn: " + valueOrDefault(hotelName, "Khách sạn"));
        tvMapAddress.setText("Địa chỉ: " + valueOrDefault(address, "Đang cập nhật"));
        tvMapDistance.setText("Khoảng cách từ bạn: Đang tính...");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo cấu hình bản đồ
        osmMap = findViewById(R.id.osmMap);
        osmMap.setTileSource(TileSourceFactory.MAPNIK);
        osmMap.setMultiTouchControls(true);

        showHotelLocation();
        checkAndRequestLocationPermission();
    }

    private void showHotelLocation() {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "Khách sạn chưa có tọa độ bản đồ", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint hotelPoint = new GeoPoint(latitude, longitude);
        osmMap.getController().setZoom(16.0);
        osmMap.getController().setCenter(hotelPoint);

        // Tạo ghim (Marker) vị trí khách sạn
        Marker marker = new Marker(osmMap);
        marker.setPosition(hotelPoint);
        marker.setTitle(hotelName);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        osmMap.getOverlays().add(marker);
        osmMap.invalidate(); // Vẽ lại bản đồ
    }

    // Hàm kiểm tra và chủ động xin quyền người dùng nếu chưa có
    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Chưa có quyền -> Chủ động hiện hộp thoại hệ thống xin quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Đã có quyền rồi -> Tiến hành tính khoảng cách ngay
            calculateDistance();
        }
    }

    // Tách phần tính toán khoảng cách thành hàm riêng cho sạch sẽ
    private void calculateDistance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null || latitude == 0 || longitude == 0) {
                tvMapDistance.setText("Khoảng cách từ bạn: Chưa xác định được vị trí của bạn");
                return;
            }

            float[] results = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), latitude, longitude, results);

            // kết quả results[0] trả về là mét (m), chia cho 1000 để đổi sang Kilômét (km)
            tvMapDistance.setText(String.format(Locale.getDefault(), "Khoảng cách từ bạn: %.1f km", results[0] / 1000));
        });
    }

    // Lắng nghe xem người dùng bấm "Cho phép" hay "Từ chối" trên hộp thoại xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Người dùng chọn CHO PHÉP -> Tính khoảng cách luôn
                calculateDistance();
            } else {
                // Người dùng chọn TỪ CHỐI
                tvMapDistance.setText("Khoảng cách từ bạn: Bị từ chối truy cập vị trí");
            }
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (osmMap != null) {
            osmMap.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (osmMap != null) {
            osmMap.onPause();
        }
    }
}
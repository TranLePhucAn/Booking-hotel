package com.example.hotelbooking.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_hotel_map);

        hotelName = getIntent().getStringExtra("hotel_name");
        address = getIntent().getStringExtra("address");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        TextView tvMapHotelName = findViewById(R.id.tvMapHotelName);
        TextView tvMapAddress = findViewById(R.id.tvMapAddress);
        TextView btnBackMap = findViewById(R.id.btnBackMap);
        tvMapDistance = findViewById(R.id.tvMapDistance);

        tvMapHotelName.setText("Vị trí khách sạn: " + valueOrDefault(hotelName, "Khách sạn"));
        tvMapAddress.setText("Địa chỉ: " + valueOrDefault(address, "Đang cập nhật"));
        tvMapDistance.setText("Khoảng cách từ bạn: đang tính...");
        btnBackMap.setOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        osmMap = findViewById(R.id.osmMap);
        osmMap.setTileSource(TileSourceFactory.MAPNIK);
        osmMap.setMultiTouchControls(true);
        osmMap.setBuiltInZoomControls(true);
        osmMap.setTilesScaledToDpi(true);

        showHotelLocation();
        checkAndRequestLocationPermission();
    }

    private void showHotelLocation() {
        if (latitude == 0 && longitude == 0) {
            useAddressCoordinateFallback();
        }

        if (latitude == 0 && longitude == 0) {
            latitude = 10.762622;
            longitude = 106.660172;
            tvMapDistance.setText("Khoảng cách từ bạn: khách sạn chưa có tọa độ chính xác");
            Toast.makeText(this, "Đang hiển thị vị trí trung tâm TP.HCM", Toast.LENGTH_SHORT).show();
        }

        GeoPoint hotelPoint = new GeoPoint(latitude, longitude);
        osmMap.getController().setZoom(16.0);
        osmMap.getController().setCenter(hotelPoint);

        Marker marker = new Marker(osmMap);
        marker.setPosition(hotelPoint);
        marker.setTitle(valueOrDefault(hotelName, "Khách sạn"));
        marker.setSubDescription(valueOrDefault(address, ""));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        osmMap.getOverlays().add(marker);
        osmMap.invalidate();
    }

    private void useAddressCoordinateFallback() {
        String normalized = valueOrDefault(address, "").toLowerCase(Locale.ROOT);
        if (normalized.contains("da lat") || normalized.contains("dalat")) {
            latitude = 11.9404;
            longitude = 108.4583;
        } else if (normalized.contains("vung tau")) {
            latitude = 10.4114;
            longitude = 107.1362;
        } else if (normalized.contains("da nang")) {
            latitude = 16.0678;
            longitude = 108.2453;
        } else if (normalized.contains("hoi an")) {
            latitude = 15.8801;
            longitude = 108.3380;
        } else if (normalized.contains("ha noi") || normalized.contains("hanoi")) {
            latitude = 21.0285;
            longitude = 105.8542;
        } else if (normalized.contains("nha trang")) {
            latitude = 12.2388;
            longitude = 109.1967;
        } else if (normalized.contains("tp.hcm")
                || normalized.contains("ho chi minh")
                || normalized.contains("hồ chí minh")
                || normalized.contains("sai gon")
                || normalized.contains("saigon")) {
            latitude = 10.762622;
            longitude = 106.660172;
        }
    }

    private void checkAndRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        calculateDistance();
    }

    private void calculateDistance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null || latitude == 0 || longitude == 0) {
                tvMapDistance.setText("Khoảng cách từ bạn: chưa xác định được vị trí của bạn");
                return;
            }

            double distanceKm = calculateDistanceKm(location.getLatitude(), location.getLongitude(), latitude, longitude);
            tvMapDistance.setText(String.format(Locale.getDefault(), "Khoảng cách từ bạn: %.1f km", distanceKm));
        });
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                calculateDistance();
            } else {
                tvMapDistance.setText("Khoảng cách từ bạn: bị từ chối truy cập vị trí");
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

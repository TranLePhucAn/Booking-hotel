package com.example.hotelbooking.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_hotel_map);

        hotelName = getIntent().getStringExtra("hotel_name");
        address = getIntent().getStringExtra("address");
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);

        TextView tvMapHotelName = findViewById(R.id.tvMapHotelName);
        TextView tvMapAddress = findViewById(R.id.tvMapAddress);
        tvMapDistance = findViewById(R.id.tvMapDistance);

        tvMapHotelName.setText("Vi tri khach san: " + valueOrDefault(hotelName, "Khach san"));
        tvMapAddress.setText("Dia chi: " + valueOrDefault(address, "Dang cap nhat"));
        tvMapDistance.setText("Khoang cach tu ban: dang tinh...");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        osmMap = findViewById(R.id.osmMap);
        osmMap.setTileSource(TileSourceFactory.MAPNIK);
        osmMap.setMultiTouchControls(true);

        showHotelLocation();
        calculateDistanceIfAllowed();
    }

    private void showHotelLocation() {
        if (latitude == 0 && longitude == 0) {
            Toast.makeText(this, "Khach san chua co toa do ban do", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint hotelPoint = new GeoPoint(latitude, longitude);
        osmMap.getController().setZoom(16.0);
        osmMap.getController().setCenter(hotelPoint);

        Marker marker = new Marker(osmMap);
        marker.setPosition(hotelPoint);
        marker.setTitle(hotelName);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        osmMap.getOverlays().add(marker);
        osmMap.invalidate();
    }

    private void calculateDistanceIfAllowed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvMapDistance.setText("Khoang cach tu ban: can cap quyen vi tri");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null || latitude == 0 || longitude == 0) {
                tvMapDistance.setText("Khoang cach tu ban: chua xac dinh");
                return;
            }

            float[] results = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), latitude, longitude, results);
            tvMapDistance.setText(String.format(Locale.getDefault(), "Khoang cach tu ban: %.1f km", results[0] / 1000));
        });
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

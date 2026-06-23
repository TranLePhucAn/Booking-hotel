package com.example.hotelbooking.ui.hotel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.databinding.ActivityMapBinding;
import com.example.hotelbooking.data.model.Hotel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private Hotel hotel;
    private double latitude;
    private double longitude;
    private String hotelName = "";
    private String address = "";
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiveMapData();
        hotel = (Hotel) getIntent().getSerializableExtra("hotel");
        if (hotel != null) {
            if (latitude == 0 && longitude == 0) {
                latitude = hotel.getLatitude();
                longitude = hotel.getLongitude();
            }
            if (hotelName.isEmpty()) {
                hotelName = valueOrDefault(hotel.getHotelName(), "");
            }
        }
        binding.tvMapHotelName.setText(valueOrDefault(hotelName, "Khach san"));
        if (!address.isEmpty()) {
            binding.tvDistance.setText(address);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.fabBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (latitude != 0 && longitude != 0) {
            LatLng hotelLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(hotelLocation).title(valueOrDefault(hotelName, "Khach san")));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotelLocation, 15f));
        } else {
            Toast.makeText(this, "Khach san chua co toa do", Toast.LENGTH_SHORT).show();
        }

        enableUserLocation();
    }

    private void receiveMapData() {
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        hotelName = valueOrDefault(getIntent().getStringExtra("hotel_name"), "");
        address = valueOrDefault(getIntent().getStringExtra("address"), "");
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        calculateDistance();
    }

    private void calculateDistance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && latitude != 0 && longitude != 0) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        latitude, longitude, results);
                float distanceInKm = results[0] / 1000;
                binding.tvDistance.setText(String.format(Locale.getDefault(), "Khoảng cách: %.2f km", distanceInKm));
            }
        });
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

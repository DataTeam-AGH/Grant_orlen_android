package com.example.mobile_app_orlen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private MapView map = null;
    private Marker userMarker = null;

    private TextView tvLatitude, tvLongitude, tvGpsStatus;
    private Button btnRefreshLocation, btnBackToMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguracja osmdroid - ważne przed setContentView
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        // Inicjalizacja widoków
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // Inicjalizacja osmdroid MapView
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        
        // Dodanie paska skali
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(20, 20);
        map.getOverlays().add(scaleBarOverlay);

        // Inicjalizacja klienta lokalizacji Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obsługa przycisków
        btnRefreshLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermissionAndGetLocation();
            }
        });

        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Ustawienie domyślne na Kraków (skoro tam jesteś)
        GeoPoint krakow = new GeoPoint(50.0647, 19.9450);
        map.getController().setCenter(krakow);

        checkLocationPermissionAndGetLocation();
    }

    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            requestCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvGpsStatus.setText("● Brak uprawnień");
            return;
        }

        tvGpsStatus.setText("● GPS: Pobieranie lokalizacji...");

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateUiAndMap(location);
                        Toast.makeText(MapActivity.this, "Lokalizacja zaktualizowana", Toast.LENGTH_SHORT).show();
                    } else {
                        tvGpsStatus.setText("● GPS: Brak danych");
                        Toast.makeText(MapActivity.this, "Upewnij się, że GPS jest włączony.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    tvGpsStatus.setText("● GPS: Błąd");
                });
    }

    private void updateUiAndMap(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        // Współrzędne w stopniach
        tvLatitude.setText(convertToDms(lat, true));
        tvLongitude.setText(convertToDms(lng, false));
        tvGpsStatus.setText("● GPS: Sygnał aktywny");

        // Aktualizacja mapy
        GeoPoint userPoint = new GeoPoint(lat, lng);
        map.getController().animateTo(userPoint);
        
        if (userMarker != null) {
            map.getOverlays().remove(userMarker);
        }
        
        userMarker = new Marker(map);
        userMarker.setPosition(userPoint);
        
        // Prosta czerwona ikonka (kropka)
        android.graphics.drawable.GradientDrawable dot = new android.graphics.drawable.GradientDrawable();
        dot.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        dot.setColor(android.graphics.Color.RED);
        dot.setSize(45, 45);
        dot.setStroke(3, android.graphics.Color.WHITE); // Biała obwódka dla lepszej widoczności
        
        userMarker.setIcon(dot);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        userMarker.setInfoWindow(null); // Wyłączenie dymka z tekstem

        map.getOverlays().add(userMarker);
        map.invalidate(); // Odśwież mapę
    }

    private String convertToDms(double coordinate, boolean isLatitude) {
        String direction = isLatitude ? (coordinate >= 0 ? "N" : "S") : (coordinate >= 0 ? "E" : "W");
        coordinate = Math.abs(coordinate);
        int degrees = (int) coordinate;
        double minutesPart = (coordinate - degrees) * 60;
        int minutes = (int) minutesPart;
        double seconds = (minutesPart - minutes) * 60;
        return String.format(Locale.US, "%d° %d' %.2f\" %s", degrees, minutes, seconds, direction);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestCurrentLocation();
            } else {
                tvGpsStatus.setText("● Odmówiono dostępu");
            }
        }
    }
}

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
import android.location.LocationManager;
import android.content.Context;

import com.google.android.gms.location.CurrentLocationRequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.File;
import java.util.Locale;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private MapView map = null;
    private Marker userMarker = null; 
    private boolean isFirstFix = true;

    private TextView tvLatitude, tvLongitude, tvGpsStatus;
    private Button btnRefreshLocation, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. CAŁKOWITY RESET I NAPRAWA 403
        // UNIKAMY "com.example" - serwery OSM to blokują!
        String myUserAgent = "OrlenGasMonitorSystem/1.2 (cyprian.orlen.project@gmail.com)";
        Configuration.getInstance().setUserAgentValue(myUserAgent);
        
        super.onCreate(savedInstanceState);

        // 2. Ładowanie i czyszczenie błędnych ścieżek
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
        // Wymuszamy User-Agent ponownie po load()
        Configuration.getInstance().setUserAgentValue(myUserAgent);
        
        // 3. Ustawienie ZUPEŁNIE NOWEJ ścieżki dla plików map (całkowity reset)
        File mapRoot = new File(getFilesDir(), "osmdroid_v6_final");
        if (!mapRoot.exists()) mapRoot.mkdirs();
        Configuration.getInstance().setOsmdroidBasePath(mapRoot);
        
        File mapCache = new File(mapRoot, "tiles");
        if (!mapCache.exists()) mapCache.mkdirs();
        Configuration.getInstance().setOsmdroidTileCache(mapCache);

        setContentView(R.layout.activity_map);

        // Inicjalizacja widoków
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
        btnBack = findViewById(R.id.btnBack);

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

        // Definicja ciągłego śledzenia pozycji
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateUiAndMap(location);
                    }
                }
            }
        };

        // Obsługa przycisków
        btnRefreshLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstFix = true;
                checkLocationPermissionAndGetLocation();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Ustawienie domyślne na Kraków
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
            tvGpsStatus.setText(getString(R.string.gps_no_permission));
            return;
        }

        // Sprawdź czy GPS jest w ogóle włączony w systemie
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Włącz GPS w ustawieniach emulatora/telefonu!", Toast.LENGTH_LONG).show();
        }

        tvGpsStatus.setText(getString(R.string.gps_loading));

        // 1. Spróbuj pobrać ostatnią znaną pozycję (szybki start)
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateUiAndMap(location);
            }
        });

        // 2. Wymuś pobranie świeżej lokalizacji (CurrentLocation)
        CancellationTokenSource cts = new CancellationTokenSource();
        CurrentLocationRequest clr = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        fusedLocationClient.getCurrentLocation(clr, cts.getToken()).addOnSuccessListener(this, location -> {
            if (location != null) {
                updateUiAndMap(location);
            } else {
                Toast.makeText(this, "GPS: Ustaw lokalizację w Extended Controls emulatora!", Toast.LENGTH_LONG).show();
            }
        });

        // 3. Rozpocznij ciągłe śledzenie
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void updateUiAndMap(Location location) {
        if (location == null) return;
        
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        GeoPoint userPoint = new GeoPoint(lat, lng);

        // SYNCHRONIZACJA: Ten sam sygnał dla tekstu i markera
        tvLatitude.setText(convertToDms(lat, true));
        tvLongitude.setText(convertToDms(lng, false));
        tvGpsStatus.setText(getString(R.string.gps_active));

        // Ręczny marker - 100% synchronizacji
        if (userMarker == null) {
            userMarker = new Marker(map);
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            
            // Profesjonalna kropka Orlen (Niebieska)
            android.graphics.drawable.GradientDrawable dot = new android.graphics.drawable.GradientDrawable();
            dot.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            dot.setColor(android.graphics.Color.parseColor("#0D47A1")); 
            dot.setSize(60, 60);
            dot.setStroke(5, android.graphics.Color.WHITE);
            
            userMarker.setIcon(dot);
            userMarker.setInfoWindow(null);
            map.getOverlays().add(userMarker);
            
            // Tylko przy pierwszym złapaniu sygnału centrujemy mapę
            map.getController().setCenter(userPoint);
            map.getController().setZoom(18.0);
        }
        
        userMarker.setPosition(userPoint);

        if (isFirstFix) {
            map.getController().animateTo(userPoint);
            isFirstFix = false;
        }
        
        map.invalidate(); 
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
        checkLocationPermissionAndGetLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        // Zatrzymaj śledzenie, gdy apka jest w tle
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
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

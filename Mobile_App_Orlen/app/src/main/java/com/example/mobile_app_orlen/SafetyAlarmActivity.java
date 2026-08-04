package com.example.mobile_app_orlen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SafetyAlarmActivity extends Activity {

    private Vibrator vibrator;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView tvCurrentLocation;
    private TextView tvWeatherAlarm;
    private WeatherService weatherService;
    private static final String WEATHER_API_KEY = "c6ebc42aeaf95f82074837ad9ea223e9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 1.0f;
        getWindow().setAttributes(params);

        setContentView(R.layout.activity_safety_alarm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);
        tvWeatherAlarm = findViewById(R.id.tvWeatherAlarm);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initWeatherService();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationUi(location);
                        getWeatherForLocation(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };

        loadAlarmDetails();
        startVibration();

        Button btnCall112 = findViewById(R.id.btnCall112);
        Button btnAcknowledge = findViewById(R.id.btnAcknowledge);

        btnCall112.setOnClickListener(view -> {
            Intent intent = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:112")
            );

            startActivity(intent);
        });

        btnAcknowledge.setOnClickListener(view -> {
            stopVibration();

            Intent intent = new Intent(
                    SafetyAlarmActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            finish();
        });
    }

    private void loadAlarmDetails() {
        TextView tvAlarmDetails = findViewById(R.id.tvAlarmDetails);

        String gasName = getIntent().getStringExtra("gasName");
        String stationName = getIntent().getStringExtra("stationName");

        double concentration = getIntent().getDoubleExtra(
                "concentration",
                12.4
        );

        if (gasName == null) {
            gasName = "Metan";
        }

        if (stationName == null) {
            stationName = "Kraków Północ";
        }

        String details =
                "Gaz: " + gasName +
                        "\nStężenie: " + concentration + "%" +
                        "\nStacja: " + stationName +
                        "\nPoziom zagrożenia: WYSOKI";

        tvAlarmDetails.setText(details);
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(
                Context.VIBRATOR_SERVICE
        );

        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        long[] pattern = {
                0,
                700,
                250,
                700,
                250,
                1200,
                700
        };

        vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, 0)
        );
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                    1001);
            tvCurrentLocation.setText(getString(R.string.gps_no_permission));
            return;
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Włącz GPS w ustawieniach emulatora!", Toast.LENGTH_LONG).show();
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        try {
            // Próba pobrania ostatniej pozycji na start
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) updateLocationUi(location);
            });

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            
        } catch (SecurityException e) {
            tvCurrentLocation.setText(getString(R.string.alarm_location_no_permission));
        }
    }

    private void updateLocationUi(android.location.Location location) {
        String latDms = convertToDms(location.getLatitude(), true);
        String lngDms = convertToDms(location.getLongitude(), false);
        tvCurrentLocation.setText(getString(R.string.alarm_location_format, latDms, lngDms));
    }

    private String convertToDms(double coordinate, boolean isLatitude) {
        String direction = isLatitude ? (coordinate >= 0 ? "N" : "S") : (coordinate >= 0 ? "E" : "W");
        coordinate = Math.abs(coordinate);
        int degrees = (int) coordinate;
        double minutesPart = (coordinate - degrees) * 60;
        int minutes = (int) minutesPart;
        double seconds = (minutesPart - minutes) * 60;
        return String.format(Locale.US, "%d°%d'%.1f\"%s", degrees, minutes, seconds, direction);
    }

    private void initWeatherService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherService = retrofit.create(WeatherService.class);
    }

    private void getWeatherForLocation(double lat, double lon) {
        android.content.SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
        long lastFetch = prefs.getLong("last_fetch", 0);
        long now = System.currentTimeMillis();


        if (now - lastFetch < 600000) {
            String cachedWind = prefs.getString("last_wind_text", "");
            if (tvWeatherAlarm != null && !cachedWind.isEmpty()) {
                tvWeatherAlarm.setText(cachedWind);
            }
            if (now - lastFetch < 300000) return; // Jeśli mniej niż 5 min, nie próbuj nawet pytać API
        }

        weatherService.getCurrentWeather(lat, lon, WEATHER_API_KEY, "metric", "pl")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateWeatherUi(response.body());
                        } else {
                            // Jeśli błąd to 401 (zły klucz) lub 429 (limit), używamy cache'u
                            if (response.code() == 401 || response.code() == 429) {
                                String cachedWind = getSharedPreferences("weather_prefs", MODE_PRIVATE)
                                        .getString("last_wind_text", "");
                                if (tvWeatherAlarm != null && !cachedWind.isEmpty()) {
                                    tvWeatherAlarm.setText(cachedWind);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        // W razie błędu nie czyścimy ekranu, zostawiamy co było
                    }
                });
    }

    private void updateWeatherUi(WeatherResponse weather) {
        if (tvWeatherAlarm != null) {
            String dir = getWindDirection(weather.wind.deg);
            String windText = String.format(Locale.US, "WIATR: %.1f m/s (%s)", weather.wind.speed, dir);
            tvWeatherAlarm.setText(windText);

            // Przygotuj pełny tekst dla MainActivity, aby dane były spójne
            String fullDir = getWindDirectionFull(weather.wind.deg);
            String weatherText = String.format(
                    Locale.US,
                    "Temp: %.1f°C | Wilgotność: %d%% | Wiatr: %.1f m/s (%s)",
                    weather.main.temp,
                    weather.main.humidity,
                    weather.wind.speed,
                    fullDir
            );

            // Współdzielimy cache z MainActivity - aktualizujemy oba pola
            getSharedPreferences("weather_prefs", MODE_PRIVATE).edit()
                    .putLong("last_fetch", System.currentTimeMillis())
                    .putString("last_wind_text", windText)
                    .putString("last_weather_text", weatherText)
                    .apply();
        }
    }

    private String getWindDirectionFull(int deg) {
        if (deg >= 337.5 || deg < 22.5) return "Północny (N)";
        if (deg >= 22.5 && deg < 67.5) return "Północno-Wschodni (NE)";
        if (deg >= 67.5 && deg < 112.5) return "Wschodni (E)";
        if (deg >= 112.5 && deg < 157.5) return "Południowo-Wschodni (SE)";
        if (deg >= 157.5 && deg < 202.5) return "Południowy (S)";
        if (deg >= 202.5 && deg < 247.5) return "Południowo-Zachodni (SW)";
        if (deg >= 247.5 && deg < 292.5) return "Zachodni (W)";
        if (deg >= 292.5 && deg < 337.5) return "Północno-Zachodni (NW)";
        return "N/A";
    }

    private String getWindDirection(int deg) {
        if (deg >= 337.5 || deg < 22.5) return "N";
        if (deg >= 22.5 && deg < 67.5) return "NE";
        if (deg >= 67.5 && deg < 112.5) return "E";
        if (deg >= 112.5 && deg < 157.5) return "SE";
        if (deg >= 157.5 && deg < 202.5) return "S";
        if (deg >= 202.5 && deg < 247.5) return "SW";
        if (deg >= 247.5 && deg < 292.5) return "W";
        if (deg >= 292.5 && deg < 337.5) return "NW";
        return "?";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        stopVibration();
        super.onDestroy();
    }
}
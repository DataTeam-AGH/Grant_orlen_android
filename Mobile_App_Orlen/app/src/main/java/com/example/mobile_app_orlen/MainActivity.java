package com.example.mobile_app_orlen;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.mobile_app_orlen.data.AppDatabase;
import com.example.mobile_app_orlen.data.Measurement;
import com.example.mobile_app_orlen.data.model;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvMethaneValue;
    private ProgressBar progressMethane;

    private TextView tvSystemStatus;
    private TextView tvMeasurementsCount;
    private TextView tvAnomalyCount;
    private TextView tvWeather;

    private Button btnNewMeasurement;
    private Button btnHistory;
    private Button btnMap;
    private Button btnAlerts;

    private FusedLocationProviderClient fusedLocationClient;
    private WeatherService weatherService;
    private static final String WEATHER_API_KEY = "c6ebc42aeaf95f82074837ad9ea223e9";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private long lastWeatherFetchTime = 0;

    private double currentMethaneConcentration = 0.0;
    private boolean userIsNearStation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initWeatherService();

        connectViews();
        // loadExampleData(); // Usunięte na rzecz dynamicznego ładowania w onResume
        setupButtons();
        fetchLocationAndWeather();
    }

    private void initWeatherService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = retrofit.create(WeatherService.class);
    }

    private void fetchLocationAndWeather() {
        android.content.SharedPreferences prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
        long lastFetch = prefs.getLong("last_fetch", 0);
        long lastAttempt = prefs.getLong("last_attempt", 0);
        String lastWeather = prefs.getString("last_weather_text", "");
        long now = System.currentTimeMillis();

        // 1. Jeśli mamy świeże dane (sprzed mniej niż 10 min), używamy ich i nie pytamy API
        if (now - lastFetch < 600000 && !lastWeather.isEmpty()) {
            if (tvWeather != null) tvWeather.setText(lastWeather);
            return;
        }

        // 2. Jeśli ostatnia PRÓBA (nawet nieudana) była mniej niż 5 minut temu, 
        // używamy starego cache'u, żeby nie blokować klucza (unikanie 429)
        if (now - lastAttempt < 300000 && !lastWeather.isEmpty()) {
            if (tvWeather != null) tvWeather.setText(lastWeather);
            return;
        }

        if (tvWeather != null) {
            tvWeather.setText("Odświeżanie pogody...");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Zapisujemy czas próby, żeby nie spamować API przy błędach 429
        prefs.edit().putLong("last_attempt", now).apply();

        CancellationTokenSource cts = new CancellationTokenSource();
        CurrentLocationRequest clr = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(60000)
                .build();

        fusedLocationClient.getCurrentLocation(clr, cts.getToken())
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    getWeatherForLocation(location.getLatitude(), location.getLongitude());
                } else {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                        if (lastLoc != null) {
                            getWeatherForLocation(lastLoc.getLatitude(), lastLoc.getLongitude());
                        } else {
                            if (tvWeather != null) tvWeather.setText("Oczekiwanie na lokalizację GPS...");
                        }
                    });
                }
            })
            .addOnFailureListener(e -> {
                if (tvWeather != null) tvWeather.setText("Błąd lokalizacji: " + e.getMessage());
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndWeather();
            } else {
                if (tvWeather != null) tvWeather.setText("Brak uprawnień do GPS");
            }
        }
    }

    private void getWeatherForLocation(double lat, double lon) {
        weatherService.getCurrentWeather(lat, lon, WEATHER_API_KEY, "metric", "pl")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateWeatherUi(response.body());
                        } else {
                            if (tvWeather != null) {
                                String cached = getSharedPreferences("weather_prefs", MODE_PRIVATE).getString("last_weather_text", "");
                                // Jeśli błąd to 401 (zły klucz) lub 429 (limit), pokaż ostatnie znane dane
                                if ((response.code() == 401 || response.code() == 429) && !cached.isEmpty()) {
                                    tvWeather.setText(cached);
                                } else {
                                    tvWeather.setText("Błąd API: " + response.code());
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        if (tvWeather != null) {
                            tvWeather.setText("Błąd sieci: " + t.getMessage());
                        }
                    }
                });
    }

    private void updateWeatherUi(WeatherResponse weather) {
        if (tvWeather != null) {
            String direction = getWindDirection(weather.wind.deg);
            String weatherText = String.format(
                    Locale.US,
                    "Temp: %.1f°C | Wilgotność: %d%% | Wiatr: %.1f m/s (%s)",
                    weather.main.temp,
                    weather.main.humidity,
                    weather.wind.speed,
                    direction
            );
            tvWeather.setText(weatherText);

            // Wyciągamy sam symbol kierunku (np. "N") do cache'u alarmu
            String shortDir = "N/A";
            int start = direction.indexOf("(");
            int end = direction.indexOf(")");
            if (start != -1 && end != -1) {
                shortDir = direction.substring(start + 1, end);
            }
            String windTextAlarm = String.format(Locale.US, "WIATR: %.1f m/s (%s)", weather.wind.speed, shortDir);

            // Zapisz do cache, aby obie aktywności miały to samo
            getSharedPreferences("weather_prefs", MODE_PRIVATE).edit()
                    .putLong("last_fetch", System.currentTimeMillis())
                    .putString("last_weather_text", weatherText)
                    .putString("last_wind_text", windTextAlarm)
                    .apply();
        }
    }

    private String getWindDirection(int deg) {
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

    private void connectViews() {
        tvMethaneValue = findViewById(R.id.tvMethaneValue);
        progressMethane = findViewById(R.id.progressMethane);
        tvSystemStatus = findViewById(R.id.tvSystemStatus);
        tvMeasurementsCount = findViewById(R.id.tvMeasurementsCount);
        tvAnomalyCount = findViewById(R.id.tvAnomalyCount);
        tvWeather = findViewById(R.id.tvWeather);
        btnNewMeasurement = findViewById(R.id.btnNewMeasurement);
        btnHistory = findViewById(R.id.btnHistory);
        btnMap = findViewById(R.id.btnMap);
        btnAlerts = findViewById(R.id.btnAlerts);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLastMeasurement();
        fetchLocationAndWeather();
    }

    private void loadLastMeasurement() {
        AppDatabase db = AppDatabase.getDatabase(this);
        db.measurementDao().getAllMeasurements().observe(this, measurements -> {
            if (measurements != null && !measurements.isEmpty()) {
                Measurement last = measurements.get(0);
                currentMethaneConcentration = last.ch4Value;
                updateGasDisplay(tvMethaneValue, progressMethane, currentMethaneConcentration);
                updateSystemStatus(currentMethaneConcentration);
                
                if (tvMeasurementsCount != null) tvMeasurementsCount.setText(String.valueOf(measurements.size()));
                
                long anomalyCount = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    anomalyCount = measurements.stream().filter(m -> m.isAnomaly).count();
                }
                if (tvAnomalyCount != null) tvAnomalyCount.setText(String.valueOf(anomalyCount));
            } else {
                // Domyślnie SAFE przy braku danych
                currentMethaneConcentration = 0.0;
                updateGasDisplay(tvMethaneValue, progressMethane, 0.0);
                updateSystemStatus(0.0);
                if (tvMeasurementsCount != null) tvMeasurementsCount.setText("0");
                if (tvAnomalyCount != null) tvAnomalyCount.setText("0");
            }
        });
    }

    private void updateSystemStatus(double value) {
        if (tvSystemStatus != null) {
            model.SafetyLevel level = model.getMethaneSafetyLevel(value);
            tvSystemStatus.setText(model.getStatusMessage(level));
            
            int color;
            switch (level) {
                case ALERT: color = Color.parseColor("#F44336"); break;
                case WARNING: color = Color.parseColor("#FFC107"); break;
                default: color = Color.parseColor("#4CAF50"); break;
            }
            tvSystemStatus.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    private void updateGasDisplay(TextView textView, ProgressBar progressBar, double value) {
        if (textView != null) {
            textView.setText(String.format(Locale.getDefault(), "%.1f %%", value));
        }

        if (progressBar != null) {
            progressBar.setMax(100);
            progressBar.setProgress((int) value);

            model.SafetyLevel level = model.getMethaneSafetyLevel(value);
            int color;
            switch (level) {
                case ALERT: color = Color.parseColor("#F44336"); break;
                case WARNING: color = Color.parseColor("#FFC107"); break;
                default: color = Color.parseColor("#4CAF50"); break;
            }
            progressBar.setProgressTintList(ColorStateList.valueOf(color));
        }
    }

    private void setupButtons() {
        if (btnNewMeasurement != null) {
            btnNewMeasurement.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AddMeasurementActivity.class));
            });
        }
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            });
        }
        if (btnMap != null) {
            btnMap.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, MapActivity.class));
            });
        }
        if (btnAlerts != null) {
            btnAlerts.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SafetyAlarmActivity.class));
            });
        }
    }

    private void checkSafetyAlarm() {
        model.SafetyLevel level = model.getMethaneSafetyLevel(currentMethaneConcentration);
        if (userIsNearStation && level == model.SafetyLevel.ALERT) {
            Intent intent = new Intent(MainActivity.this, SafetyAlarmActivity.class);
            intent.putExtra("gasName", "Metan");
            intent.putExtra("concentration", currentMethaneConcentration);
            intent.putExtra("stationName", "Kraków Północ");
            startActivity(intent);
        }
    }
}

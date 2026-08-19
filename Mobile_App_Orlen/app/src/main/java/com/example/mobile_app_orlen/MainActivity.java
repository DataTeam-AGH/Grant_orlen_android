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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvMethaneValue;
    private ProgressBar progressMethane;

    private TextView tvSystemStatus;
    private TextView tvMeasurementsCount;
    private TextView tvAnomalyCount;
    private TextView tvLastMeasurements;
    private TextView tvWeather;

    private Button btnNewMeasurement;
    private Button btnHistory;
    private Button btnMap;
    private Button btnAlerts;

    private FusedLocationProviderClient fusedLocationClient;
    private WeatherService weatherService;

    private static final String WEATHER_API_KEY = "c6ebc42aeaf95f82074837ad9ea223e9";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private double currentMethaneConcentration = 0.0;
    private boolean userIsNearStation = true;

    // Zmienne do przechowywania aktualnych warunków pogodowych dla modelu rozmytego
    private double currentTemp = 20.0;
    private double currentPress = 1013.0;
    private double currentHum = 50.0;
    private double currentWind = 1.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        initWeatherService();
        connectViews();
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
        android.content.SharedPreferences prefs =
                getSharedPreferences("weather_prefs", MODE_PRIVATE);

        long lastFetch = prefs.getLong("last_fetch", 0);
        long lastAttempt = prefs.getLong("last_attempt", 0);
        String lastWeather = prefs.getString("last_weather_text", "");
        long now = System.currentTimeMillis();

        if (now - lastFetch < 600000 && !lastWeather.isEmpty()) {
            if (tvWeather != null) {
                tvWeather.setText(lastWeather);
            }
            return;
        }

        if (now - lastAttempt < 300000 && !lastWeather.isEmpty()) {
            if (tvWeather != null) {
                tvWeather.setText(lastWeather);
            }
            return;
        }

        if (tvWeather != null) {
            tvWeather.setText("Odświeżanie pogody...");
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            return;
        }

        prefs.edit()
                .putLong("last_attempt", now)
                .apply();

        CancellationTokenSource cts =
                new CancellationTokenSource();

        CurrentLocationRequest clr =
                new CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setMaxUpdateAgeMillis(60000)
                        .build();

        fusedLocationClient
                .getCurrentLocation(clr, cts.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getWeatherForLocation(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                    } else {
                        fusedLocationClient
                                .getLastLocation()
                                .addOnSuccessListener(lastLoc -> {
                                    if (lastLoc != null) {
                                        getWeatherForLocation(
                                                lastLoc.getLatitude(),
                                                lastLoc.getLongitude()
                                        );
                                    } else {
                                        if (tvWeather != null) {
                                            tvWeather.setText(
                                                    "Oczekiwanie na lokalizację GPS..."
                                            );
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvWeather != null) {
                        tvWeather.setText(
                                "Błąd lokalizacji: " + e.getMessage()
                        );
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                fetchLocationAndWeather();
            } else {
                if (tvWeather != null) {
                    tvWeather.setText("Brak uprawnień do GPS");
                }
            }
        }
    }

    private void getWeatherForLocation(double lat, double lon) {
        weatherService
                .getCurrentWeather(
                        lat,
                        lon,
                        WEATHER_API_KEY,
                        "metric",
                        "pl"
                )
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(
                            Call<WeatherResponse> call,
                            Response<WeatherResponse> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            updateWeatherUi(response.body());

                        } else if (tvWeather != null) {

                            String cached =
                                    getSharedPreferences(
                                            "weather_prefs",
                                            MODE_PRIVATE
                                    ).getString(
                                            "last_weather_text",
                                            ""
                                    );

                            if ((response.code() == 401
                                    || response.code() == 429)
                                    && !cached.isEmpty()) {

                                tvWeather.setText(cached);

                            } else {
                                tvWeather.setText(
                                        "Błąd API: " + response.code()
                                );
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<WeatherResponse> call,
                            Throwable t
                    ) {
                        if (tvWeather != null) {
                            tvWeather.setText(
                                    "Błąd sieci: " + t.getMessage()
                            );
                        }
                    }
                });
    }

    private void updateWeatherUi(WeatherResponse weather) {
        if (tvWeather != null) {
            currentTemp = weather.main.temp;
            currentPress = weather.main.pressure;
            currentHum = weather.main.humidity;
            currentWind = weather.wind.speed;

            String direction = getWindDirection(
                    weather.wind.deg
            );

            String weatherText = String.format(
                    Locale.US,
                    "Temp: %.1f°C | Wilgotność: %d%% | Ciśnienie: %d hPa | Wiatr: %.1f m/s (%s)",
                    weather.main.temp,
                    weather.main.humidity,
                    weather.main.pressure,
                    weather.wind.speed,
                    direction
            );

            tvWeather.setText(weatherText);

            String windTextAlarm = String.format(
                    Locale.US,
                    "WIATR: %.1f m/s (%s)",
                    weather.wind.speed,
                    direction
            );

            getSharedPreferences(
                    "weather_prefs",
                    MODE_PRIVATE
            ).edit()
                    .putLong(
                            "last_fetch",
                            System.currentTimeMillis()
                    )
                    .putString(
                            "last_weather_text",
                            weatherText
                    )
                    .putString(
                            "last_wind_text",
                            windTextAlarm
                    )
                    .apply();

            // Odśwież status, bo zmieniły się warunki dla modelu fuzzy
            updateSystemStatus(currentMethaneConcentration);
        }
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
        return "N/A";
    }

    private void connectViews() {
        tvMethaneValue = findViewById(R.id.tvMethaneValue);
        progressMethane = findViewById(R.id.progressMethane);
        tvSystemStatus = findViewById(R.id.tvSystemStatus);
        tvMeasurementsCount = findViewById(R.id.tvMeasurementsCount);
        tvAnomalyCount = findViewById(R.id.tvAnomalyCount);
        tvLastMeasurements = findViewById(R.id.tvLastMeasurements);
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

        db.measurementDao()
                .getAllMeasurements()
                .observe(this, measurements -> {

                    if (measurements != null
                            && !measurements.isEmpty()) {

                        Measurement last = measurements.get(0);

                        currentMethaneConcentration =
                                last.ch4Value;

                        updateGasDisplay(
                                tvMethaneValue,
                                progressMethane,
                                currentMethaneConcentration
                        );

                        updateSystemStatus(
                                currentMethaneConcentration
                        );

                    } else {

                        currentMethaneConcentration = 0.0;

                        updateGasDisplay(
                                tvMethaneValue,
                                progressMethane,
                                0.0
                        );

                        updateSystemStatus(0.0);
                    }
                });

        db.measurementDao()
                .getMeasurementsCount()
                .observe(this, count -> {

                    if (tvMeasurementsCount != null) {
                        tvMeasurementsCount.setText(
                                String.valueOf(
                                        count != null ? count : 0
                                )
                        );
                    }
                });

        db.measurementDao()
                .getAnomalyCount()
                .observe(this, count -> {

                    if (tvAnomalyCount != null) {
                        tvAnomalyCount.setText(
                                String.valueOf(
                                        count != null ? count : 0
                                )
                        );
                    }
                });

        db.measurementDao()
                .getLatestMeasurements()
                .observe(this, measurements -> {
                    updateLastMeasurements(measurements);
                });
    }

    private void updateLastMeasurements(
            List<Measurement> measurements
    ) {
        if (tvLastMeasurements == null) {
            return;
        }

        if (measurements == null || measurements.isEmpty()) {
            tvLastMeasurements.setText(
                    "Brak zapisanych pomiarów"
            );
            return;
        }

        StringBuilder text = new StringBuilder();

        for (Measurement measurement : measurements) {

            String time = new SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
            ).format(
                    new Date(measurement.timestamp)
            );

            String location;

            if (measurement.locationName != null
                    && !measurement.locationName.trim().isEmpty()) {

                location = measurement.locationName;

            } else {

                location = String.format(
                        Locale.getDefault(),
                        "%.5f, %.5f",
                        measurement.latitude,
                        measurement.longitude
                );
            }

            model.SafetyLevel level =
                    model.getMethaneSafetyLevel(
                            measurement.ch4Value
                    );

            String status;

            switch (level) {
                case ALERT:
                    status = "ALERT";
                    break;

                case WARNING:
                    status = "WARNING";
                    break;

                default:
                    status = "SAFE";
                    break;
            }

            text.append(time)
                    .append(" | ")
                    .append(
                            String.format(
                                    Locale.getDefault(),
                                    "%.1f %%",
                                    measurement.ch4Value
                            )
                    )
                    .append(" | ")
                    .append(location)
                    .append(" | ")
                    .append(status)
                    .append("\n");
        }

        tvLastMeasurements.setText(
                text.toString().trim()
        );
    }

    private void updateSystemStatus(double value) {
        if (tvSystemStatus != null) {
            // Obliczanie poziomu bezpieczeństwa za pomocą logiki rozmytej
            model.FuzzyResult fuzzy = model.calculateFuzzySafety(
                    value,
                    currentTemp,
                    currentPress,
                    currentHum,
                    currentWind
            );

            tvSystemStatus.setText(fuzzy.message);

            int color = getSafetyColor(fuzzy.level);

            tvSystemStatus.setBackgroundTintList(
                    ColorStateList.valueOf(color)
            );
        }
    }

    private void updateGasDisplay(
            TextView textView,
            ProgressBar progressBar,
            double value
    ) {
        if (textView != null) {
            textView.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.1f %% LEL",
                            value
                    )
            );
        }

        if (progressBar != null) {
            progressBar.setMax(100);
            progressBar.setProgress((int) value);

            // Wykorzystujemy model rozmyty do koloru paska postępu
            model.FuzzyResult fuzzy = model.calculateFuzzySafety(
                    value,
                    currentTemp,
                    currentPress,
                    currentHum,
                    currentWind
            );

            int color = getSafetyColor(fuzzy.level);

            progressBar.setProgressTintList(
                    ColorStateList.valueOf(color)
            );
        }
    }

    private int getSafetyColor(model.SafetyLevel level) {
        switch (level) {
            case ALERT:
                return Color.parseColor("#F44336");
            case WARNING:
                return Color.parseColor("#FFC107");
            case SAFE:
            default:
                return Color.parseColor("#4CAF50");
        }
    }

    private void setupButtons() {
        if (btnNewMeasurement != null) {
            btnNewMeasurement.setOnClickListener(v -> {
                startActivity(
                        new Intent(
                                MainActivity.this,
                                AddMeasurementActivity.class
                        )
                );
            });
        }

        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                startActivity(
                        new Intent(
                                MainActivity.this,
                                HistoryActivity.class
                        )
                );
            });
        }

        if (btnMap != null) {
            btnMap.setOnClickListener(v -> {
                startActivity(
                        new Intent(
                                MainActivity.this,
                                MapActivity.class
                        )
                );
            });
        }

        if (btnAlerts != null) {
            btnAlerts.setOnClickListener(v -> {
                startActivity(
                        new Intent(
                                MainActivity.this,
                                SafetyAlarmActivity.class
                        )
                );
            });
        }
    }

    private void checkSafetyAlarm() {
        model.SafetyLevel level =
                model.getMethaneSafetyLevel(
                        currentMethaneConcentration
                );

        if (userIsNearStation
                && level == model.SafetyLevel.ALERT) {

            Intent intent = new Intent(
                    MainActivity.this,
                    SafetyAlarmActivity.class
            );

            intent.putExtra("gasName", "Metan");
            intent.putExtra(
                    "concentration",
                    currentMethaneConcentration
            );
            intent.putExtra(
                    "stationName",
                    "Pomiar bieżący"
            );

            startActivity(intent);
        }
    }
}

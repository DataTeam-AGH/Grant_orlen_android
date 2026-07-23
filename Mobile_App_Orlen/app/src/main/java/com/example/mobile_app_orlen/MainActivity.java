package com.example.mobile_app_orlen;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import java.util.Locale;

public class MainActivity extends Activity {

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
    private static final String WEATHER_API_KEY = "bd5e378503939ddaee76f12ad7a97608"; // Placeholder API Key
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static final double METHANE_DANGER_THRESHOLD = 5.0;

    private double currentMethaneConcentration = 12.4;
    private boolean userIsNearStation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initWeatherService();

        connectViews();
        loadExampleData();
        setupButtons();
        checkSafetyAlarm();
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
        if (tvWeather != null) {
            tvWeather.setText(getString(R.string.weather_loading));
        }

        // Prośba o OBA uprawnienia jednocześnie (wymagane na nowszych Androidach)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();
        CurrentLocationRequest clr = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(60000) // Może być z ostatniej minuty
                .build();

        fusedLocationClient.getCurrentLocation(clr, cts.getToken())
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    getWeatherForLocation(location.getLatitude(), location.getLongitude());
                } else {
                    // Jeśli brak fixa, spróbuj ostatnią znaną
                    fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                        if (lastLoc != null) {
                            getWeatherForLocation(lastLoc.getLatitude(), lastLoc.getLongitude());
                        } else {
                            Toast.makeText(this, "Ustaw lokalizację w emulatorze!", Toast.LENGTH_LONG).show();
                            getWeatherForLocation(50.0647, 19.9450);
                        }
                    });
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Błąd GPS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                getWeatherForLocation(50.0647, 19.9450);
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndWeather();
            } else {
                getWeatherForLocation(50.0647, 19.9450); // Brak uprawnień -> domyślnie Kraków
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
                                tvWeather.setText("Błąd API: " + response.code() + " (Sprawdź klucz)");
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

    private void loadExampleData() {
        updateGasDisplay(
                tvMethaneValue,
                progressMethane,
                currentMethaneConcentration + " %",
                85,
                100
        );

        if (tvSystemStatus != null) {
            tvSystemStatus.setText("● System aktywny");
        }

        if (tvMeasurementsCount != null) {
            tvMeasurementsCount.setText("128");
        }

        if (tvAnomalyCount != null) {
            tvAnomalyCount.setText("2");
        }

        if (tvWeather != null) {
            tvWeather.setText(
                    "Temperatura: 18°C  |  Wilgotność: 64%  |  Wiatr: 3.5 m/s"
            );
        }
    }

    private void updateGasDisplay(
            TextView textView,
            ProgressBar progressBar,
            String valueText,
            int progress,
            int max
    ) {
        if (textView != null) {
            textView.setText(valueText);
        }

        if (progressBar == null) {
            return;
        }

        progressBar.setMax(max);
        progressBar.setProgress(progress);

        float ratio = (float) progress / max;
        int color;

        if (ratio < 0.3f) {
            color = Color.parseColor("#4CAF50");
        } else if (ratio < 0.7f) {
            color = Color.parseColor("#FFC107");
        } else {
            color = Color.parseColor("#F44336");
        }

        progressBar.setProgressTintList(
                ColorStateList.valueOf(color)
        );
    }

    private void setupButtons() {
        if (btnNewMeasurement != null) {
            btnNewMeasurement.setOnClickListener(v -> {
                Intent intent = new Intent(
                        MainActivity.this,
                        AddMeasurementActivity.class
                );

                startActivity(intent);
            });
        }

        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                Intent intent = new Intent(
                        MainActivity.this,
                        HistoryActivity.class
                );

                startActivity(intent);
            });
        }

        if (btnMap != null) {
            btnMap.setOnClickListener(v -> {
                Intent intent = new Intent(
                        MainActivity.this,
                        MapActivity.class
                );

                startActivity(intent);
            });
        }

        if (btnAlerts != null) {
            btnAlerts.setOnClickListener(v -> {
                Intent intent = new Intent(
                        MainActivity.this,
                        SafetyAlarmActivity.class
                );

                startActivity(intent);
            });
        }
    }

    private void checkSafetyAlarm() {
        boolean dangerousConcentration =
                currentMethaneConcentration >= METHANE_DANGER_THRESHOLD;

        if (!userIsNearStation || !dangerousConcentration) {
            return;
        }

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
                "Kraków Północ"
        );

        startActivity(intent);
    }
}
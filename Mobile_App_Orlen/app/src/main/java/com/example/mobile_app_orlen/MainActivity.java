package com.example.mobile_app_orlen;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    // Widoki gazów
    private TextView tvMethaneValue;
    private ProgressBar progressMethane;
    
    // Status i KPI
    private TextView tvSystemStatus, tvMeasurementsCount, tvAnomalyCount, tvWeather;

    // Przyciski
    private Button btnNewMeasurement, btnHistory, btnMap, btnAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectViews();
        loadExampleData();
        setupButtons();
    }

    private void connectViews() {
        // Gazy
        tvMethaneValue = findViewById(R.id.tvMethaneValue);
        progressMethane = findViewById(R.id.progressMethane);

        // Status i KPI
        tvSystemStatus = findViewById(R.id.tvSystemStatus);
        tvMeasurementsCount = findViewById(R.id.tvMeasurementsCount);
        tvAnomalyCount = findViewById(R.id.tvAnomalyCount);
        tvWeather = findViewById(R.id.tvWeather);

        // Przyciski
        btnNewMeasurement = findViewById(R.id.btnNewMeasurement);
        btnHistory = findViewById(R.id.btnHistory);
        btnMap = findViewById(R.id.btnMap);
        btnAlerts = findViewById(R.id.btnAlerts);
    }

    private void loadExampleData() {
        // Metan (CH4) - Niski poziom (Zielony)
        updateGasDisplay(tvMethaneValue, progressMethane, "0.5 %", 10, 100);

        // Status i KPI
        if (tvSystemStatus != null) tvSystemStatus.setText("● System aktywny");
        if (tvMeasurementsCount != null) tvMeasurementsCount.setText("128");
        if (tvAnomalyCount != null) tvAnomalyCount.setText("2");
        if (tvWeather != null) tvWeather.setText("Temperatura: 18°C  |  Wilgotność: 64%  |  Wiatr: 3.5 m/s");
    }

    private void updateGasDisplay(TextView textView, ProgressBar progressBar, String valueText, int progress, int max) {
        if (textView != null) textView.setText(valueText);
        if (progressBar != null) {
            progressBar.setMax(max);
            progressBar.setProgress(progress);
            
            int color;
            float ratio = (float) progress / max;
            if (ratio < 0.3f) {
                color = Color.parseColor("#4CAF50"); // Zielony
            } else if (ratio < 0.7f) {
                color = Color.parseColor("#FFC107"); // Żółty
            } else {
                color = Color.parseColor("#F44336"); // Czerwony
            }
            progressBar.setProgressTintList(ColorStateList.valueOf(color));
        }
    }

    private void setupButtons() {
        if (btnNewMeasurement != null) {
            btnNewMeasurement.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddMeasurementActivity.class);
                startActivity(intent);
            });
        }

        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            });
        }

        if (btnMap != null) {
            btnMap.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            });
        }

        if (btnAlerts != null) {
            btnAlerts.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AlertsActivity.class);
                startActivity(intent);
            });
        }
    }
}

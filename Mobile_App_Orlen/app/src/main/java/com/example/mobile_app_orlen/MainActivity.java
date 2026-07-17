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

    private static final double METHANE_DANGER_THRESHOLD = 5.0;

    private double currentMethaneConcentration = 12.4;
    private boolean userIsNearStation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectViews();
        loadExampleData();
        setupButtons();
        checkSafetyAlarm();
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
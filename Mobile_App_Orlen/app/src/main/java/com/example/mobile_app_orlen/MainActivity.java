package com.example.mobile_app_orlen;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    // Widoki gazów
    private TextView tvMethaneValue, tvH2SValue, tvCO2Value;
    private ProgressBar progressMethane, progressH2S, progressCO2;
    
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
        
        tvH2SValue = findViewById(R.id.tvH2SValue);
        progressH2S = findViewById(R.id.progressH2S);
        
        tvCO2Value = findViewById(R.id.tvCO2Value);
        progressCO2 = findViewById(R.id.progressCO2);

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

        // Siarkowodór (H2S) - Średni poziom (Żółty)
        updateGasDisplay(tvH2SValue, progressH2S, "15.0 ppm", 50, 100);

        // Dwutlenek Węgla (CO2) - Wysoki poziom (Czerwony)
        updateGasDisplay(tvCO2Value, progressCO2, "850 ppm", 850, 1000);

        // Status i KPI
        if (tvSystemStatus != null) tvSystemStatus.setText("● System aktywny");
        if (tvMeasurementsCount != null) tvMeasurementsCount.setText("128");
        if (tvAnomalyCount != null) tvAnomalyCount.setText("2");
        if (tvWeather != null) tvWeather.setText("Temperatura: 18°C  |  Wilgotność: 64%  |  Wiatr: 3.5 m/s");
    }

    /**
     * Aktualizuje tekst i kolor paska postępu w zależności od poziomu (ratio).
     */
    private void updateGasDisplay(TextView textView, ProgressBar progressBar, String valueText, int progress, int max) {
        if (textView != null) textView.setText(valueText);
        if (progressBar != null) {
            progressBar.setMax(max);
            progressBar.setProgress(progress);
            
            // Logika kolorów: zielony < 30%, żółty < 70%, czerwony >= 70%
            int color;
            float ratio = (float) progress / max;
            if (ratio < 0.3f) {
                color = Color.parseColor("#4CAF50"); // Zielony
            } else if (ratio < 0.7f) {
                color = Color.parseColor("#FFC107"); // Żółty/Pomarańczowy
            } else {
                color = Color.parseColor("#F44336"); // Czerwony
            }
            progressBar.setProgressTintList(ColorStateList.valueOf(color));
        }
    }

    private void setupButtons() {
        if (btnNewMeasurement != null) {
            btnNewMeasurement.setOnClickListener(v ->
                    Toast.makeText(this, "Tu później zrobimy ekran dodawania pomiaru", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnHistory != null) {
            btnHistory.setOnClickListener(v ->
                    Toast.makeText(this, "Tu później zrobimy historię pomiarów", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnMap != null) {
            btnMap.setOnClickListener(v ->
                    Toast.makeText(this, "Tu później zrobimy mapę GPS / heatmapę", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnAlerts != null) {
            btnAlerts.setOnClickListener(v ->
                    Toast.makeText(this, "Tu później zrobimy alarmy i procedury", Toast.LENGTH_SHORT).show()
            );
        }
    }
}

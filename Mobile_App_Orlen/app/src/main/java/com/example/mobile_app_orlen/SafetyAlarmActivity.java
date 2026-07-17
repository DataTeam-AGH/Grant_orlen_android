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

public class SafetyAlarmActivity extends Activity {

    private Vibrator vibrator;

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

    @Override
    protected void onDestroy() {
        stopVibration();
        super.onDestroy();
    }
}
package com.example.mobile_app_orlen;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddMeasurementActivity extends Activity {

    private EditText etConcentration, etLocation;
    private Button btnSave, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);

        etConcentration = findViewById(R.id.etConcentration);
        etLocation = findViewById(R.id.etLocation);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnSave.setOnClickListener(v -> {
            String concentration = etConcentration.getText().toString();
            if (concentration.isEmpty()) {
                Toast.makeText(this, "Wpisz stężenie!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Pomiar zapisany pomyślnie", Toast.LENGTH_SHORT).show();
                finish(); // Powrót do menu po zapisie
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}

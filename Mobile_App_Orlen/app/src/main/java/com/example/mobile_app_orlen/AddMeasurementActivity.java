package com.example.mobile_app_orlen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_app_orlen.data.model;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;

public class AddMeasurementActivity extends AppCompatActivity {

    private ManualEntryViewModel viewModel;
    private TextInputEditText etCh4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ManualEntryViewModel.class);

        etCh4 = findViewById(R.id.et_ch4);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnBack = findViewById(R.id.btnBack);

        btnSave.setOnClickListener(v -> {
            String ch4 = etCh4.getText() != null ? etCh4.getText().toString() : "";
            viewModel.saveMeasurements(ch4);
        });

        btnBack.setOnClickListener(v -> finish());

        viewModel.saveStatus.observe(this, status -> {
            if (status == null) return;

            switch (status) {
                case "SUCCESS":
                    Toast.makeText(this, R.string.success_save, Toast.LENGTH_SHORT).show();
                    
                    String ch4Str = etCh4.getText() != null ? etCh4.getText().toString() : "0";
                    try {
                        double val = Double.parseDouble(ch4Str);
                        if (model.getMethaneSafetyLevel(val) == model.SafetyLevel.ALERT) {
                            Intent intent = new Intent(this, SafetyAlarmActivity.class);
                            intent.putExtra("gasName", "Metan");
                            intent.putExtra("concentration", val);
                            intent.putExtra("stationName", "Pomiar ręczny");
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        // ignore
                    }

                    finish();
                    break;
                case "ERROR_EMPTY":
                    Toast.makeText(this, R.string.error_at_least_one_field, Toast.LENGTH_SHORT).show();
                    break;
                case "ERROR_INVALID":
                    Toast.makeText(this, R.string.error_invalid_value, Toast.LENGTH_SHORT).show();
                    break;
            }
            viewModel.resetStatus();
        });
    }
}

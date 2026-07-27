package com.example.mobile_app_orlen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

public class AddMeasurementActivity extends AppCompatActivity {

    private ManualEntryViewModel viewModel;
    private TextInputEditText etCh4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);

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

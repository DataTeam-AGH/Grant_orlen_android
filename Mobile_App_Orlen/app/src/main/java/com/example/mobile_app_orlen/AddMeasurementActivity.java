package com.example.mobile_app_orlen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class AddMeasurementActivity extends AppCompatActivity {

    private ManualEntryViewModel viewModel;
    private TextInputEditText etCh4, etLat, etLon, etLocationName;
    private TextView tvGpsCoords;
    private RadioGroup rgLocationType;
    private LinearLayout layoutGps, layoutManual;
    
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0.0, currentLon = 0.0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_measurement);

        viewModel = new ViewModelProvider(this).get(ManualEntryViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etCh4 = findViewById(R.id.et_ch4);
        etLat = findViewById(R.id.et_lat);
        etLon = findViewById(R.id.et_lon);
        etLocationName = findViewById(R.id.et_location_name);
        tvGpsCoords = findViewById(R.id.tv_gps_coords);
        rgLocationType = findViewById(R.id.rg_location_type);
        layoutGps = findViewById(R.id.layout_gps);
        layoutManual = findViewById(R.id.layout_manual);

        rgLocationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_gps) {
                layoutGps.setVisibility(View.VISIBLE);
                layoutManual.setVisibility(View.GONE);
            } else {
                layoutGps.setVisibility(View.GONE);
                layoutManual.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btn_fetch_gps).setOnClickListener(v -> fetchGpsLocation());

        Button btnSave = findViewById(R.id.btnSave);
        Button btnBack = findViewById(R.id.btnBack);

        btnSave.setOnClickListener(v -> {
            String ch4 = etCh4.getText() != null ? etCh4.getText().toString() : "";
            String locationName = etLocationName.getText() != null ? etLocationName.getText().toString() : "";
            
            double lat, lon;
            if (rgLocationType.getCheckedRadioButtonId() == R.id.rb_gps) {
                lat = currentLat;
                lon = currentLon;
            } else {
                String latStr = etLat.getText() != null ? etLat.getText().toString() : "0";
                String lonStr = etLon.getText() != null ? etLon.getText().toString() : "0";
                try {
                    lat = Double.parseDouble(latStr);
                    lon = Double.parseDouble(lonStr);
                } catch (NumberFormatException e) {
                    lat = 0;
                    lon = 0;
                }
            }
            
            viewModel.saveMeasurements(ch4, lat, lon, locationName);
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

    private void fetchGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                tvGpsCoords.setText(String.format(Locale.US, "Lat: %.6f, Lon: %.6f", currentLat, currentLon));
            } else {
                Toast.makeText(this, "Nie można pobrać lokalizacji. Upewnij się, że GPS jest włączony.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchGpsLocation();
            } else {
                Toast.makeText(this, "Brak uprawnień do lokalizacji.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

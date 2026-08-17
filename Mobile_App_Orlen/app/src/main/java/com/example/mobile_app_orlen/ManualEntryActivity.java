package com.example.mobile_app_orlen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

public class ManualEntryActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1002;

    private ManualEntryViewModel viewModel;

    private TextInputEditText etCh4;
    private TextInputEditText etLatitude;
    private TextInputEditText etLongitude;
    private TextInputEditText etLocationName;

    private TextView tvLocationStatus;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        viewModel = new ViewModelProvider(this).get(ManualEntryViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etCh4 = findViewById(R.id.et_ch4);
        etLatitude = findViewById(R.id.et_latitude);
        etLongitude = findViewById(R.id.et_longitude);
        etLocationName = findViewById(R.id.et_location_name);
        tvLocationStatus = findViewById(R.id.tv_location_status);

        Button btnSave = findViewById(R.id.btn_save);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnGetLocation = findViewById(R.id.btn_get_location);

        btnGetLocation.setOnClickListener(v -> getCurrentLocation());

        btnSave.setOnClickListener(v -> {
            String ch4 = etCh4.getText() != null
                    ? etCh4.getText().toString().trim()
                    : "";

            String latitudeText = etLatitude.getText() != null
                    ? etLatitude.getText().toString().trim()
                    : "";

            String longitudeText = etLongitude.getText() != null
                    ? etLongitude.getText().toString().trim()
                    : "";

            String locationName = etLocationName.getText() != null
                    ? etLocationName.getText().toString().trim()
                    : "";

            double latitude = 0.0;
            double longitude = 0.0;

            if (!latitudeText.isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeText);
                } catch (NumberFormatException e) {
                    Toast.makeText(
                            this,
                            "Nieprawidłowa szerokość geograficzna",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
            }

            if (!longitudeText.isEmpty()) {
                try {
                    longitude = Double.parseDouble(longitudeText);
                } catch (NumberFormatException e) {
                    Toast.makeText(
                            this,
                            "Nieprawidłowa długość geograficzna",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
            }

            if (latitude < -90 || latitude > 90) {
                Toast.makeText(
                        this,
                        "Szerokość musi być w zakresie -90 do 90",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (longitude < -180 || longitude > 180) {
                Toast.makeText(
                        this,
                        "Długość musi być w zakresie -180 do 180",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            viewModel.saveMeasurements(
                    ch4,
                    latitude,
                    longitude,
                    locationName
            );
        });

        btnCancel.setOnClickListener(v -> finish());

        viewModel.saveStatus.observe(this, status -> {
            if (status == null) return;

            switch (status) {
                case "SUCCESS":
                    Toast.makeText(
                            this,
                            R.string.success_save,
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                    break;

                case "ERROR_EMPTY":
                    Toast.makeText(
                            this,
                            R.string.error_at_least_one_field,
                            Toast.LENGTH_SHORT
                    ).show();
                    break;

                case "ERROR_INVALID":
                    Toast.makeText(
                            this,
                            R.string.error_invalid_value,
                            Toast.LENGTH_SHORT
                    ).show();
                    break;
            }

            viewModel.resetStatus();
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(
                                this,
                                "Nie udało się pobrać lokalizacji",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    etLatitude.setText(String.valueOf(latitude));
                    etLongitude.setText(String.valueOf(longitude));

                    tvLocationStatus.setText(
                            "Lokalizacja GPS: "
                                    + latitude
                                    + ", "
                                    + longitude
                    );
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();
            } else {
                Toast.makeText(
                        this,
                        "Brak uprawnień do lokalizacji",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
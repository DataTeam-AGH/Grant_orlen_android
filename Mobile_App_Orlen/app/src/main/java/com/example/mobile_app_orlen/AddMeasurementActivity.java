package com.example.mobile_app_orlen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_app_orlen.data.model;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddMeasurementActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private ManualEntryViewModel viewModel;
    private TextInputEditText etCh4;
    private TextInputEditText etLatitude;
    private TextInputEditText etLongitude;
    private TextInputEditText etLocationName;

    private android.widget.TextView tvLocationStatus;

    private FusedLocationProviderClient fusedLocationClient;

    private final ExecutorService geocoderExecutor =
            Executors.newSingleThreadExecutor();

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etCh4 = findViewById(R.id.et_ch4);
        etLatitude = findViewById(R.id.et_latitude);
        etLongitude = findViewById(R.id.et_longitude);
        etLocationName = findViewById(R.id.et_location_name);
        tvLocationStatus = findViewById(R.id.tv_location_status);

        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnBack = findViewById(R.id.btnBack);

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

        btnBack.setOnClickListener(v -> finish());

        viewModel.saveStatus.observe(this, status -> {
            if (status == null) return;

            switch (status) {
                case "SUCCESS":
                    Toast.makeText(
                            this,
                            R.string.success_save,
                            Toast.LENGTH_SHORT
                    ).show();

                    String ch4Str = etCh4.getText() != null
                            ? etCh4.getText().toString()
                            : "0";

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
                    }

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

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
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
                    "Lokalizacja GPS: " +
                            latitude +
                            ", " +
                            longitude
            );

            getCityFromCoordinates(latitude, longitude);
        });
    }

    private void getCityFromCoordinates(double latitude, double longitude) {
        if (!Geocoder.isPresent()) {
            tvLocationStatus.setText(
                    "GPS pobrany, ale rozpoznawanie miasta jest niedostępne"
            );
            return;
        }

        tvLocationStatus.setText(
                "GPS pobrany. Rozpoznawanie miasta..."
        );

        geocoderExecutor.execute(() -> {
            Geocoder geocoder = new Geocoder(
                    this,
                    Locale.getDefault()
            );

            try {
                List<Address> addresses = geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1
                );

                runOnUiThread(() -> {
                    if (addresses == null || addresses.isEmpty()) {
                        tvLocationStatus.setText(
                                "GPS pobrany, ale nie znaleziono miasta"
                        );
                        return;
                    }

                    Address address = addresses.get(0);

                    String city = address.getLocality();

                    if (city == null || city.trim().isEmpty()) {
                        city = address.getSubAdminArea();
                    }

                    if (city == null || city.trim().isEmpty()) {
                        city = address.getAdminArea();
                    }

                    if (city == null || city.trim().isEmpty()) {
                        tvLocationStatus.setText(
                                "GPS pobrany, ale nie znaleziono miasta"
                        );
                        return;
                    }

                    city = city.trim();

                    etLocationName.setText(city);

                    tvLocationStatus.setText(
                            "Lokalizacja: " + city
                    );
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                        tvLocationStatus.setText(
                                "GPS pobrany, ale nie udało się rozpoznać miasta"
                        )
                );
            }
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
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geocoderExecutor.shutdown();
    }
}
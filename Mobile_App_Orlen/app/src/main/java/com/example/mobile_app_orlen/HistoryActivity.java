package com.example.mobile_app_orlen;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_app_orlen.data.Measurement;
import com.example.mobile_app_orlen.data.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private HistoryViewModel viewModel;
    private MeasurementAdapter adapter;

    private EditText etSearch;
    private EditText etMinConcentration;
    private EditText etMaxConcentration;
    private EditText etLocation;

    private Spinner spStatus;

    private Button btnDateFrom;
    private Button btnDateTo;
    private Button btnApplyFilters;
    private Button btnClearFilters;

    private Long dateFrom;
    private Long dateTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        viewModel = new ViewModelProvider(this)
                .get(HistoryViewModel.class);

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        TextView tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        Button btnBack = findViewById(R.id.btnBack);

        etSearch = findViewById(R.id.etSearch);
        etMinConcentration = findViewById(R.id.etMinConcentration);
        etMaxConcentration = findViewById(R.id.etMaxConcentration);
        etLocation = findViewById(R.id.etLocation);

        spStatus = findViewById(R.id.spStatus);

        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        btnClearFilters = findViewById(R.id.btnClearFilters);

        adapter = new MeasurementAdapter();

        String[] statuses = {
                "Wszystkie",
                "SAFE",
                "WARNING",
                "ALERT"
        };

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statuses
        );

        statusAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spStatus.setAdapter(statusAdapter);

        rvHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvHistory.setAdapter(adapter);

        List<Measurement>[] currentMeasurements =
                new List[]{new ArrayList<>()};

        viewModel.getUserMeasurements().observe(
                this,
                measurements -> {

                    if (measurements == null) {
                        currentMeasurements[0] =
                                new ArrayList<>();
                    } else {
                        currentMeasurements[0] =
                                measurements;
                    }

                    applyFilters(
                            currentMeasurements[0],
                            rvHistory,
                            tvEmptyHistory
                    );
                }
        );

        btnDateFrom.setOnClickListener(
                v -> showDatePicker(true)
        );

        btnDateTo.setOnClickListener(
                v -> showDatePicker(false)
        );

        btnApplyFilters.setOnClickListener(
                v -> applyFilters(
                        currentMeasurements[0],
                        rvHistory,
                        tvEmptyHistory
                )
        );

        btnClearFilters.setOnClickListener(v -> {

            etSearch.setText("");
            etMinConcentration.setText("");
            etMaxConcentration.setText("");
            etLocation.setText("");

            spStatus.setSelection(0);

            dateFrom = null;
            dateTo = null;

            btnDateFrom.setText("Data od");
            btnDateTo.setText("Data do");

            applyFilters(
                    currentMeasurements[0],
                    rvHistory,
                    tvEmptyHistory
            );
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePicker(boolean isFromDate) {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    Calendar selectedDate =
                            Calendar.getInstance();

                    selectedDate.set(
                            year,
                            month,
                            dayOfMonth,
                            0,
                            0,
                            0
                    );

                    selectedDate.set(
                            Calendar.MILLISECOND,
                            0
                    );

                    if (isFromDate) {

                        dateFrom =
                                selectedDate.getTimeInMillis();

                        btnDateFrom.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%02d.%02d.%04d",
                                        dayOfMonth,
                                        month + 1,
                                        year
                                )
                        );

                    } else {

                        selectedDate.set(
                                Calendar.HOUR_OF_DAY,
                                23
                        );

                        selectedDate.set(
                                Calendar.MINUTE,
                                59
                        );

                        selectedDate.set(
                                Calendar.SECOND,
                                59
                        );

                        selectedDate.set(
                                Calendar.MILLISECOND,
                                999
                        );

                        dateTo =
                                selectedDate.getTimeInMillis();

                        btnDateTo.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%02d.%02d.%04d",
                                        dayOfMonth,
                                        month + 1,
                                        year
                                )
                        );
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void applyFilters(
            List<Measurement> measurements,
            RecyclerView rvHistory,
            TextView tvEmptyHistory
    ) {
        String search = etSearch.getText()
                .toString()
                .trim()
                .toLowerCase();

        String location = etLocation.getText()
                .toString()
                .trim()
                .toLowerCase();

        String selectedStatus =
                spStatus.getSelectedItem().toString();

        String minText = etMinConcentration.getText()
                .toString()
                .trim();

        String maxText = etMaxConcentration.getText()
                .toString()
                .trim();

        Double min = null;
        Double max = null;

        if (!minText.isEmpty()) {
            try {
                min = Double.parseDouble(
                        minText.replace(",", ".")
                );
            } catch (NumberFormatException ignored) {
            }
        }

        if (!maxText.isEmpty()) {
            try {
                max = Double.parseDouble(
                        maxText.replace(",", ".")
                );
            } catch (NumberFormatException ignored) {
            }
        }

        List<Measurement> filtered =
                new ArrayList<>();

        for (Measurement measurement : measurements) {

            long timestamp = measurement.timestamp;

            if (dateFrom != null
                    && timestamp < dateFrom) {
                continue;
            }

            if (dateTo != null
                    && timestamp > dateTo) {
                continue;
            }

            if (min != null
                    && measurement.ch4Value < min) {
                continue;
            }

            if (max != null
                    && measurement.ch4Value > max) {
                continue;
            }

            model.SafetyLevel level =
                    model.getMethaneSafetyLevel(
                            measurement.ch4Value
                    );

            String status = level.name();

            if (!selectedStatus.equals("Wszystkie")
                    && !status.equals(selectedStatus)) {
                continue;
            }

            String notes = measurement.notes == null
                    ? ""
                    : measurement.notes;

            String searchable =
                    measurement.ch4Value + " " +
                            measurement.timestamp + " " +
                            notes;

            if (!search.isEmpty()
                    && !searchable
                    .toLowerCase()
                    .contains(search)) {
                continue;
            }

            if (!location.isEmpty()
                    && !notes
                    .toLowerCase()
                    .contains(location)) {
                continue;
            }

            filtered.add(measurement);
        }

        adapter.submitList(filtered);

        if (filtered.isEmpty()) {

            tvEmptyHistory.setVisibility(
                    View.VISIBLE
            );

            rvHistory.setVisibility(
                    View.GONE
            );

        } else {

            tvEmptyHistory.setVisibility(
                    View.GONE
            );

            rvHistory.setVisibility(
                    View.VISIBLE
            );
        }
    }
}
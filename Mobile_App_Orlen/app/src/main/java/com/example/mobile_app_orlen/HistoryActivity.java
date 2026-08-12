package com.example.mobile_app_orlen;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private HistoryViewModel viewModel;
    private MeasurementAdapter adapter;

    private EditText etSearch;
    private EditText etMinConcentration;
    private EditText etMaxConcentration;

    private Spinner spStatus;

    private Button btnApplyFilters;
    private Button btnClearFilters;
    private Button btnToggleFilters;
    private Button btnDateFrom;
    private Button btnDateTo;

    private LinearLayout filterPanel;

    private TextView tvResultsCount;
    private TextView tvEmptyHistory;

    private Long dateFrom;
    private Long dateTo;

    private List<Measurement> currentMeasurements = new ArrayList<>();

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final SimpleDateFormat searchDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        viewModel = new ViewModelProvider(this)
                .get(HistoryViewModel.class);

        RecyclerView rvHistory = findViewById(R.id.rvHistory);

        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        tvResultsCount = findViewById(R.id.tvResultsCount);

        Button btnBack = findViewById(R.id.btnBack);

        etSearch = findViewById(R.id.etSearch);
        etMinConcentration = findViewById(R.id.etMinConcentration);
        etMaxConcentration = findViewById(R.id.etMaxConcentration);

        spStatus = findViewById(R.id.spStatus);

        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        btnClearFilters = findViewById(R.id.btnClearFilters);

        btnToggleFilters = findViewById(R.id.btnToggleFilters);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);

        filterPanel = findViewById(R.id.filterPanel);

        adapter = new MeasurementAdapter();

        rvHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvHistory.setAdapter(adapter);

        setupStatusSpinner();
        setupFilterPanel();
        setupDateButtons();

        viewModel.getUserMeasurements().observe(this, measurements -> {

            if (measurements == null) {
                currentMeasurements = new ArrayList<>();
            } else {
                currentMeasurements = measurements;
            }

            applyFilters(
                    currentMeasurements,
                    rvHistory
            );
        });

        btnApplyFilters.setOnClickListener(v ->
                applyFilters(
                        currentMeasurements,
                        rvHistory
                )
        );

        btnClearFilters.setOnClickListener(v -> {
            etSearch.setText("");
            etMinConcentration.setText("");
            etMaxConcentration.setText("");

            spStatus.setSelection(0);

            dateFrom = null;
            dateTo = null;

            btnDateFrom.setText("Data od");
            btnDateTo.setText("Data do");

            applyFilters(
                    currentMeasurements,
                    rvHistory
            );
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupStatusSpinner() {

        String[] statuses = {
                "Wszystkie",
                "SAFE",
                "WARNING",
                "ALERT"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        statuses
                );

        statusAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spStatus.setAdapter(statusAdapter);
    }

    private void setupFilterPanel() {

        filterPanel.setVisibility(View.GONE);

        btnToggleFilters.setOnClickListener(v -> {

            if (filterPanel.getVisibility() == View.VISIBLE) {
                filterPanel.setVisibility(View.GONE);
                btnToggleFilters.setText("FILTRY");
            } else {
                filterPanel.setVisibility(View.VISIBLE);
                btnToggleFilters.setText("UKRYJ FILTRY");
            }
        });
    }

    private void setupDateButtons() {

        btnDateFrom.setOnClickListener(v ->
                showDatePicker(true)
        );

        btnDateTo.setOnClickListener(v ->
                showDatePicker(false)
        );
    }

    private void showDatePicker(boolean isFromDate) {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    year,
                                    month,
                                    dayOfMonth,
                                    0,
                                    0,
                                    0
                            );

                            selected.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            if (isFromDate) {

                                dateFrom =
                                        selected.getTimeInMillis();

                                btnDateFrom.setText(
                                        dateFormat.format(
                                                selected.getTime()
                                        )
                                );

                            } else {

                                selected.set(
                                        Calendar.HOUR_OF_DAY,
                                        23
                                );

                                selected.set(
                                        Calendar.MINUTE,
                                        59
                                );

                                selected.set(
                                        Calendar.SECOND,
                                        59
                                );

                                selected.set(
                                        Calendar.MILLISECOND,
                                        999
                                );

                                dateTo =
                                        selected.getTimeInMillis();

                                btnDateTo.setText(
                                        dateFormat.format(
                                                selected.getTime()
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
            RecyclerView rvHistory
    ) {

        String search =
                etSearch.getText()
                        .toString()
                        .trim()
                        .toLowerCase(Locale.getDefault());

        String minText =
                etMinConcentration.getText()
                        .toString()
                        .trim();

        String maxText =
                etMaxConcentration.getText()
                        .toString()
                        .trim();

        Double min = null;
        Double max = null;

        try {

            if (!minText.isEmpty()) {
                min = Double.parseDouble(
                        minText.replace(",", ".")
                );
            }

            if (!maxText.isEmpty()) {
                max = Double.parseDouble(
                        maxText.replace(",", ".")
                );

            }

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Nieprawidłowy zakres stężenia",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (min != null && max != null && min > max) {

            Toast.makeText(
                    this,
                    "Wartość „Od” nie może być większa niż „Do”",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String selectedStatus =
                spStatus.getSelectedItem().toString();

        List<Measurement> filtered =
                new ArrayList<>();

        for (Measurement measurement : measurements) {

            if (dateFrom != null &&
                    measurement.timestamp < dateFrom) {
                continue;
            }

            if (dateTo != null &&
                    measurement.timestamp > dateTo) {
                continue;
            }

            if (min != null &&
                    measurement.ch4Value < min) {
                continue;
            }

            if (max != null &&
                    measurement.ch4Value > max) {
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

            String searchable =
                    measurement.ch4Value
                            + " "
                            + measurement.timestamp
                            + " "
                            + measurement.notes
                            + " "
                            + searchDateFormat.format(
                            new Date(measurement.timestamp)
                    );

            if (!search.isEmpty()
                    && !searchable
                    .toLowerCase(Locale.getDefault())
                    .contains(search)) {
                continue;
            }

            filtered.add(measurement);
        }

        adapter.submitList(filtered);

        tvResultsCount.setText(
                "Znalezione pomiary: " + filtered.size()
        );

        if (filtered.isEmpty()) {

            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);

        } else {

            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
        }
    }
}
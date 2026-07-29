package com.example.mobile_app_orlen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryActivity extends AppCompatActivity {

    private HistoryViewModel viewModel;
    private MeasurementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        TextView tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        Button btnBack = findViewById(R.id.btnBack);

        adapter = new MeasurementAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        viewModel.getUserMeasurements().observe(this, measurements -> {
            if (measurements == null || measurements.isEmpty()) {
                tvEmptyHistory.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            } else {
                tvEmptyHistory.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
                adapter.submitList(measurements);
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}

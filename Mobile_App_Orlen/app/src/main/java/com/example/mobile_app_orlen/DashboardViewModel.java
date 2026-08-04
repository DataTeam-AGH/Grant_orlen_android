package com.example.mobile_app_orlen;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mobile_app_orlen.data.Measurement;
import com.example.mobile_app_orlen.data.MeasurementRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final MeasurementRepository repository;
    private final LiveData<List<Measurement>> latestMeasurements;
    private final LiveData<Integer> totalMeasurements;
    private final LiveData<Integer> anomalyCount;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new MeasurementRepository(application);
        String userId = "default_user";
        latestMeasurements = repository.getLatestMeasurements(userId, 3);
        totalMeasurements = repository.getMeasurementsCount(userId);
        anomalyCount = repository.getAnomalyCount(userId);
    }

    public LiveData<List<Measurement>> getLatestMeasurements() {
        return latestMeasurements;
    }

    public LiveData<Integer> getTotalMeasurements() {
        return totalMeasurements;
    }

    public LiveData<Integer> getAnomalyCount() {
        return anomalyCount;
    }
}

package com.example.mobile_app_orlen;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mobile_app_orlen.data.Measurement;
import com.example.mobile_app_orlen.data.MeasurementRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final MeasurementRepository repository;
    private final LiveData<List<Measurement>> userMeasurements;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new MeasurementRepository(application);
        // For now, using hardcoded userId "default_user"
        userMeasurements = repository.getMeasurementsForUser("default_user");
    }

    public LiveData<List<Measurement>> getUserMeasurements() {
        return userMeasurements;
    }
}

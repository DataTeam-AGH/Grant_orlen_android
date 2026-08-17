package com.example.mobile_app_orlen.data;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;

public class MeasurementRepository {

    private final MeasurementDao measurementDao;

    public MeasurementRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        measurementDao = db.measurementDao();
    }

    public LiveData<List<Measurement>> getMeasurementsForUser(String userId) {
        return measurementDao.getMeasurementsForUser(userId);
    }

    public LiveData<List<Measurement>> getAllMeasurements() {
        return measurementDao.getAllMeasurements();
    }

    public LiveData<List<Measurement>> getLatestMeasurements() {
        return measurementDao.getLatestMeasurements();
    }

    public LiveData<Integer> getMeasurementsCount() {
        return measurementDao.getMeasurementsCount();
    }

    public LiveData<Integer> getAnomalyCount() {
        return measurementDao.getAnomalyCount();
    }

    public void insert(Measurement measurement, Runnable onSuccess) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            measurementDao.insert(measurement);

            if (onSuccess != null) {
                new Handler(Looper.getMainLooper()).post(onSuccess);
            }
        });
    }
}
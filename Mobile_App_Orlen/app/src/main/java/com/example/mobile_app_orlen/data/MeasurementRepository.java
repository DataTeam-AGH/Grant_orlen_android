package com.example.mobile_app_orlen.data;

import android.app.Application;

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

    public LiveData<List<Measurement>> getLatestMeasurements(String userId, int limit) {
        return measurementDao.getLatestMeasurements(userId, limit);
    }

    public LiveData<Integer> getMeasurementsCount(String userId) {
        return measurementDao.getMeasurementsCount(userId);
    }

    public LiveData<Integer> getAnomalyCount(String userId) {
        return measurementDao.getAnomalyCount(userId);
    }

    public void insert(Measurement measurement) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            measurementDao.insert(measurement);
        });
    }
}

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

    public void insert(Measurement measurement) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            measurementDao.insert(measurement);
        });
    }
}

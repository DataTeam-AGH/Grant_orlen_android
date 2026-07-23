package com.example.mobile_app_orlen.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MeasurementDao {
    @Insert
    void insert(Measurement measurement);

    @Query("SELECT * FROM measurements WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<Measurement>> getMeasurementsForUser(String userId);

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    LiveData<List<Measurement>> getAllMeasurements();

    @Query("SELECT * FROM measurements WHERE userId = :userId ORDER BY timestamp DESC")
    List<Measurement> getMeasurementsForUserSync(String userId);
}

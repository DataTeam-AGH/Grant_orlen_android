package com.example.mobile_app_orlen.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measurements")
public class Measurement {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;
    public double ch4Value;
    public long timestamp;
    public boolean isAnomaly;
    public String notes;
    public double latitude;
    public double longitude;
    public String locationName;

    public Measurement(
            String userId,
            double ch4Value,
            long timestamp,
            boolean isAnomaly,
            String notes,
            double latitude,
            double longitude,
            String locationName
    ) {
        this.userId = userId;
        this.ch4Value = ch4Value;
        this.timestamp = timestamp;
        this.isAnomaly = isAnomaly;
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
    }
}
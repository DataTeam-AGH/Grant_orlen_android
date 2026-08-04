package com.example.mobile_app_orlen;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_app_orlen.data.Measurement;
import com.example.mobile_app_orlen.data.MeasurementRepository;

public class ManualEntryViewModel extends AndroidViewModel {

    private final MeasurementRepository repository;
    private final MutableLiveData<String> _saveStatus = new MutableLiveData<>();
    public LiveData<String> saveStatus = _saveStatus;
    
    private static final double METHANE_THRESHOLD = 5.0;

    public ManualEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new MeasurementRepository(application);
    }

    public void saveMeasurements(String ch4, double lat, double lon, String locationName) {
        if (ch4.isEmpty()) {
            _saveStatus.setValue("ERROR_EMPTY");
            return;
        }

        if (!isValid(ch4)) {
            _saveStatus.setValue("ERROR_INVALID");
            return;
        }

        double ch4Val = Double.parseDouble(ch4);
        boolean isAnomaly = ch4Val > METHANE_THRESHOLD;
        
        Measurement measurement = new Measurement(
                "default_user",
                0.0,
                0.0,
                ch4Val,
                System.currentTimeMillis(),
                isAnomaly,
                "",
                lat,
                lon,
                locationName
        );

        repository.insert(measurement);
        _saveStatus.setValue("SUCCESS");
    }

    private boolean isValid(String value) {
        if (value.isEmpty()) return true;
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void resetStatus() {
        _saveStatus.setValue(null);
    }
}

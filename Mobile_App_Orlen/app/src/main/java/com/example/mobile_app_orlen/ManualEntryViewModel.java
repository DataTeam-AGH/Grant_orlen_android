package com.example.mobile_app_orlen;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_app_orlen.data.Measurement;
import com.example.mobile_app_orlen.data.MeasurementRepository;
import com.example.mobile_app_orlen.data.model;

public class ManualEntryViewModel extends AndroidViewModel {

    private final MeasurementRepository repository;
    private final MutableLiveData<String> _saveStatus = new MutableLiveData<>();
    public LiveData<String> saveStatus = _saveStatus;

    public ManualEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new MeasurementRepository(application);
    }

    public void saveMeasurements(
            String ch4,
            double latitude,
            double longitude,
            String locationName
    ) {
        if (ch4.isEmpty()) {
            _saveStatus.setValue("ERROR_EMPTY");
            return;
        }

        if (!isValid(ch4)) {
            _saveStatus.setValue("ERROR_INVALID");
            return;
        }

        double ch4Val = Double.parseDouble(ch4);

        model.SafetyLevel level =
                model.getMethaneSafetyLevel(ch4Val);

        boolean isAnomaly =
                level == model.SafetyLevel.ALERT;

        Measurement measurement = new Measurement(
                "default_user",
                ch4Val,
                System.currentTimeMillis(),
                isAnomaly,
                "",
                latitude,
                longitude,
                locationName
        );

        repository.insert(
                measurement,
                () -> _saveStatus.setValue("SUCCESS")
        );
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
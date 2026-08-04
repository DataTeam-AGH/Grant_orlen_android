# Implementation Plan: Location Support and Dynamic Dashboard

This plan outlines the addition of location support (GPS and manual) to measurements and making the main dashboard dynamic.

## Proposed Changes

### Data Layer

#### [Measurement.java](file:///C:/Users/dell/Desktop/grant_orlen/Grant_orlen_android/Mobile_App_Orlen/app/src/main/java/com/example/mobile_app_orlen/data/Measurement.java)
- Add `latitude` and `longitude` fields (double).
- Add `locationName` field (String).

#### [MeasurementDao.java](file:///C:/Users/dell/Desktop/grant_orlen/Grant_orlen_android/Mobile_App_Orlen/app/src/main/java/com/example/mobile_app_orlen/data/MeasurementDao.java)
- Add `getLatestMeasurements(int limit)` to fetch last N measurements.
- Add `getAnomalyCount()` to count entries where `isAnomaly` is true.
- Add `getMeasurementsCount()` to count all entries.

#### [MeasurementRepository.java](file:///C:/Users/dell/Desktop/grant_orlen/Grant_orlen_android/Mobile_App_Orlen/app/src/main/java/com/example/mobile_app_orlen/data/MeasurementRepository.java)
- Add methods to expose the new DAO functions.

### UI and Logic

#### [activity_add_measurement.xml](file:///C:/Users/dell/Desktop/grant_orlen/Grant_orlen_android/Mobile_App_Orlen/app/src/main/res/layout/activity_add_measurement.xml)
- Add a toggle or buttons to choose between "Current GPS" and "Manual Location".
- Add input fields for Latitude and Longitude (visible when Manual is selected).
- Add a "Fetch GPS" button.

#### [AddMeasurementActivity.java](file:///C:/Users/dell/Desktop/grant_orlen/Grant_orlen_android/Mobile_App_Orlen/app/src/main/java/com/example/mobile_app_orlen/AddMeasurementActivity.java)
- Implement `FusedLocationProviderClient` to get current GPS coordinates.
- Handle location permissions.
- Update ViewModel call to include location data.

#### [ManualEntryViewModel.java](file:///C:/Users/dell/Desktop/grant_orlen/Grant_orlen_android/Mobile_App_Orlen/app/src/main/java/com/example/mobile_app_orlen/ManualEntryViewModel.java)
- Update `saveMeasurements` signature to include location.
- Determine `isAnomaly` based on threshold (e.g., > 5.0%).

#### [MainActivity.java](file:///C:/Users/dell/Desktop/grant_orlen/Grant_orlen_android/Mobile_App_Orlen/app/src/main/java/com/example/mobile_app_orlen/MainActivity.java)
- Create and observe a `DashboardViewModel`.
- Update KPIs (Measurements, Anomalies) dynamically.
- Update "Ostatnie pomiary" with formatted data from the last 3 measurements.

## Verification Plan

### Manual Verification
1.  Open "DODAJ POMIAR RĘCZNIE".
2.  Test "Użyj GPS" - verify coordinates are fetched.
3.  Test "Wpisz ręcznie" - enter custom coordinates.
4.  Save measurement and return to dashboard.
5.  Verify "Pomiarów" and "Anomalie" counters increased if applicable.
6.  Verify the new measurement appears in "Ostatnie pomiary" with correct info.

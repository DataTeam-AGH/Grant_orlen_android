package com.example.mobile_app_orlen.data;

public class model {
    public enum SafetyLevel {
        SAFE,
        WARNING,
        ALERT
    }

    public static SafetyLevel getMethaneSafetyLevel(double methanePercentage) {
        if (methanePercentage > 50.0) {
            return SafetyLevel.ALERT;
        } else if (methanePercentage > 30.0) {
            return SafetyLevel.WARNING;
        } else {
            return SafetyLevel.SAFE;
        }
    }

    public static String getStatusMessage(SafetyLevel level) {
        switch (level) {
            case ALERT:
                return "ALERT: Niebezpieczne stężenie metanu!";
            case WARNING:
                return "WARNING: Podwyższone stężenie metanu.";
            case SAFE:
            default:
                return "SAFE: Stężenie w normie.";
        }
    }
}

package com.example.mobile_app_orlen.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class model {
    public enum SafetyLevel {
        SAFE,
        WARNING,
        ALERT
    }

    // ==============================================================================
    // 1. FUNKCJE PRZYNALEŻNOŚCI (FUZZY MEMBERSHIP FUNCTIONS)
    // ==============================================================================

    private static double trimf(double x, double a, double b, double c) {
        if (x <= a || x >= c) return 0.0;
        if (a < x && x <= b) return (x - a) / (b - a);
        return (c - x) / (c - b);
    }

    private static double trapmf(double x, double a, double b, double c, double d) {
        if (x <= a || x >= d) return 0.0;
        if (a < x && x < b) return (x - a) / (b - a);
        if (b <= x && x <= c) return 1.0;
        return (d - x) / (d - c);
    }

    private static Map<String, Map<String, Double>> fuzzifyInputs(double temp, double press, double hum, double wind) {
        Map<String, Map<String, Double>> fz = new HashMap<>();

        Map<String, Double> tempMap = new HashMap<>();
        tempMap.put("zimno", trapmf(temp, -30, -30, 10, 18));
        tempMap.put("optimum", trimf(temp, 12, 22, 28));
        tempMap.put("goraco", trapmf(temp, 25, 35, 60, 60));
        fz.put("temp", tempMap);

        Map<String, Double> pressMap = new HashMap<>();
        pressMap.put("niskie", trapmf(press, 800, 800, 970, 995));
        pressMap.put("normalne", trimf(press, 985, 1013, 1030));
        pressMap.put("wysokie", trapmf(press, 1020, 1045, 1200, 1200));
        fz.put("press", pressMap);

        Map<String, Double> humMap = new HashMap<>();
        humMap.put("sucho", trapmf(hum, 0, 0, 30, 45));
        humMap.put("umiarkowane", trimf(hum, 35, 55, 75));
        humMap.put("wilgotno", trapmf(hum, 65, 80, 100, 100));
        fz.put("hum", humMap);

        Map<String, Double> windMap = new HashMap<>();
        windMap.put("zastoj", trapmf(wind, 0, 0, 0.2, 0.5));
        windMap.put("umiarkowany", trimf(wind, 0.3, 1.0, 2.0));
        windMap.put("przewietrzanie", trapmf(wind, 1.5, 2.5, 20.0, 20.0));
        fz.put("wind", windMap);

        return fz;
    }

    // ==============================================================================
    // 2. SILNIK WNIOSKOWANIA (MAMDANI INFERENCE ENGINE)
    // ==============================================================================

    private static Map<String, Double> evaluateRules(Map<String, Map<String, Double>> fz) {
        double vLow = 0.0;
        double low = 0.0;
        double nominal = 0.0;

        // REGUŁY KRYTYCZNE
        vLow = Math.max(vLow, Math.min(fz.get("temp").get("goraco"), fz.get("wind").get("zastoj")));
        vLow = Math.max(vLow, Math.min(fz.get("temp").get("goraco"), fz.get("press").get("niskie")));
        vLow = Math.max(vLow, Math.min(Math.min(fz.get("temp").get("goraco"), fz.get("hum").get("sucho")), fz.get("wind").get("zastoj")));

        // REGUŁY ŚREDNIE
        low = Math.max(low, Math.min(fz.get("temp").get("goraco"), fz.get("wind").get("umiarkowany")));
        low = Math.max(low, Math.min(Math.min(fz.get("temp").get("optimum"), fz.get("press").get("niskie")), fz.get("wind").get("zastoj")));
        low = Math.max(low, Math.min(fz.get("temp").get("optimum"), fz.get("hum").get("sucho")));

        // REGUŁY STANDARDOWE
        nominal = Math.max(nominal, fz.get("wind").get("przewietrzanie"));
        nominal = Math.max(nominal, Math.min(fz.get("temp").get("zimno"), fz.get("press").get("normalne")));
        nominal = Math.max(nominal, Math.min(Math.min(fz.get("temp").get("optimum"), fz.get("wind").get("umiarkowany")), fz.get("press").get("normalne")));
        nominal = Math.max(nominal, fz.get("hum").get("wilgotno"));

        Map<String, Double> activations = new HashMap<>();
        activations.put("bardzo_niskie", vLow);
        activations.put("niskie", low);
        activations.put("nominalne", nominal);
        return activations;
    }

    private static double defuzzifyCentroid(Map<String, Double> activations) {
        int steps = 100;
        double xMin = 4.0;
        double xMax = 5.0;
        double dx = (xMax - xMin) / steps;

        double num = 0.0;
        double den = 0.0;

        for (int i = 0; i <= steps; i++) {
            double x = xMin + i * dx;

            double muVeryLow = trapmf(x, 4.0, 4.0, 4.2, 4.4);
            double muLow = trimf(x, 4.3, 4.6, 4.85);
            double muNominal = trapmf(x, 4.75, 4.95, 5.0, 5.0);

            double muAgg = Math.max(
                Math.max(
                    Math.min(activations.get("bardzo_niskie"), muVeryLow),
                    Math.min(activations.get("niskie"), muLow)
                ),
                Math.min(activations.get("nominalne"), muNominal)
            );

            num += x * muAgg;
            den += muAgg;
        }

        return den > 0 ? num / den : 5.0;
    }

    // ==============================================================================
    // 3. PUBLIC API
    // ==============================================================================

    /**
     * Główna metoda obliczająca poziom bezpieczeństwa na podstawie logiki rozmytej.
     */
    public static FuzzyResult calculateFuzzySafety(double rawLel, double temp, double press, double hum, double wind) {
        if (rawLel >= 100.0) {
            return new FuzzyResult(SafetyLevel.ALERT, 5.0, 5.0, "STAN KRYTYCZNY: Miernik przekroczył 100% LEL!");
        }

        Map<String, Map<String, Double>> fz = fuzzifyInputs(temp, press, hum, wind);
        Map<String, Double> activations = evaluateRules(fz);
        double dpwCorr = defuzzifyCentroid(activations);

        double ch4Measured = (rawLel / 100.0) * 5.0;
        double margin = dpwCorr - ch4Measured;

        SafetyLevel level;
        String reason;

        if (margin <= 0.5) {
            level = SafetyLevel.ALERT;
            reason = String.format(Locale.US, "ZAGROŻENIE! Metan (%.2f%%) jest w odległości zaledwie %.2f p.p. od skorygowanego DPW (%.2f%%).", ch4Measured, margin, dpwCorr);
        } else if (margin <= 1.0) {
            level = SafetyLevel.WARNING;
            reason = String.format(Locale.US, "OSTRZEŻENIE! Metan (%.2f%%) zbliża się do strefy ryzyka (margines %.2f p.p. do DPW %.2f%%).", ch4Measured, margin, dpwCorr);
        } else {
            level = SafetyLevel.SAFE;
            reason = String.format(Locale.US, "BEZPIECZNIE. Margines do skorygowanego DPW wynosi %.2f p.p.", margin);
        }

        return new FuzzyResult(level, ch4Measured, dpwCorr, reason);
    }

    public static class FuzzyResult {
        public final SafetyLevel level;
        public final double ch4Measured;
        public final double dpwCorrected;
        public final String message;

        public FuzzyResult(SafetyLevel level, double ch4Measured, double dpwCorrected, String message) {
            this.level = level;
            this.ch4Measured = ch4Measured;
            this.dpwCorrected = dpwCorrected;
            this.message = message;
        }
    }

    // Zachowanie kompatybilności wstecznej dla starego wywołania (uproszczone)
    public static SafetyLevel getMethaneSafetyLevel(double methanePercentage) {
        // Aplikacja podaje wartość w % LEL
        FuzzyResult result = calculateFuzzySafety(methanePercentage, 20.0, 1013.0, 50.0, 1.0);
        return result.level;
    }

    public static String getStatusMessage(SafetyLevel level) {
        switch (level) {
            case ALERT: return "ALERT: Niebezpieczne stężenie metanu!";
            case WARNING: return "WARNING: Podwyższone stężenie metanu.";
            case SAFE:
            default: return "SAFE: Stężenie w normie.";
        }
    }
}

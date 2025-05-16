package com.example.collector.util;

import java.util.List;

public class RegressionUtil {
    public static double simpleOLS(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) return 0;

        double xMean = x.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double yMean = y.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double numerator = 0;
        double denominator = 0;

        for (int i = 0; i < x.size(); i++) {
            numerator += (x.get(i) - xMean) * (y.get(i) - yMean);
            denominator += Math.pow(x.get(i) - xMean, 2);
        }

        return denominator == 0 ? 0 : numerator / denominator;
    }
}
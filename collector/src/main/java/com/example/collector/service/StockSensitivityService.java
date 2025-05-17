package com.example.collector.service;

import com.example.collector.domain.*;
import com.example.collector.repository.IndicatorRepository;
import com.example.collector.repository.SectorRepository;
import com.example.collector.repository.SectorSensitivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockSensitivityService {

    private final IndicatorRepository indicatorRepository;
    private final SectorRepository sectorRepository;
    private final SectorSensitivityRepository sectorSensitivityRepository;

    /**
     * 섹터-지표 상관계수 계산 및 저장
     */
    public void calculateHeatmap(List<Indicator> events,
                                 Map<String, Map<LocalDate, Double>> sectorPriceMap,
                                 String window) {

        // 1. 지표 수익률 계산
        Map<String, List<Double>> indicatorReturns = new HashMap<>();
        for (Indicator event : events) {
            String indicatorCode = event.getCode();
            LocalDate date = event.getDate();
            Map<LocalDate, Double> basePrices = sectorPriceMap.values().iterator().next(); // 기준 섹터
            Double ret = calcReturn(basePrices, date, window);
            if (ret != null) {
                indicatorReturns.computeIfAbsent(indicatorCode, k -> new ArrayList<>()).add(ret);
            }
        }

        // 2. 섹터별 수익률 계산
        Map<String, List<Double>> sectorReturns = new HashMap<>();
        for (String sectorName : sectorPriceMap.keySet()) {
            Map<LocalDate, Double> prices = sectorPriceMap.get(sectorName);
            List<Double> returns = events.stream()
                    .map(e -> calcReturn(prices, e.getDate(), window))
                    .toList();
            sectorReturns.put(sectorName, returns);
        }

        // 3. 상관계수 계산 + enum 매핑 + 저장
        for (Map.Entry<String, List<Double>> indEntry : indicatorReturns.entrySet()) {
            String indicatorCode = indEntry.getKey();
            List<Double> x = indEntry.getValue();
            Indicator indicatorEntity = indicatorRepository.findTopByCodeOrderByDateDesc(indicatorCode);

            for (Map.Entry<String, List<Double>> secEntry : sectorReturns.entrySet()) {
                String sectorName = secEntry.getKey();
                List<Double> y = secEntry.getValue();
                Sector sectorEntity = sectorRepository.findByName(sectorName);

                double score = pearson(x, y);
                Correlation correlation = toScore(score); // ← enum

                SectorSensitivity sensitivity = SectorSensitivity.builder()
                        .indicator(indicatorEntity)
                        .sector(sectorEntity)
                        .window(window)
                        .correlation(correlation)
                        .score(score)              // ← enum 해석값
                        .updatedAt(LocalDateTime.now())
                        .build();

                sectorSensitivityRepository.save(sensitivity);
            }
        }
    }

    /**
     * 수익률 계산: ±1일, ±3일, 당일
     */
    private Double calcReturn(Map<LocalDate, Double> prices, LocalDate baseDate, String window) {
        try {
            Double before, after;
            return switch (window) {
                case "1d" -> {
                    before = prices.get(baseDate.minusDays(1));
                    after = prices.get(baseDate.plusDays(1));
                    yield (before != null && after != null) ? (after - before) / before : null;
                }
                case "3d" -> {
                    before = prices.get(baseDate.minusDays(3));
                    after = prices.get(baseDate.plusDays(3));
                    yield (before != null && after != null) ? (after - before) / before : null;
                }
                case "day" -> {
                    before = prices.get(baseDate.minusDays(1));
                    Double current = prices.get(baseDate);
                    yield (before != null && current != null) ? (current - before) / before : null;
                }
                default -> null;
            };
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * 피어슨 상관계수 계산
     */
    private double pearson(List<Double> x, List<Double> y) {
        int n = Math.min(x.size(), y.size());
        if (n == 0) return 0;

        double xMean = x.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double yMean = y.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double num = 0, denomX = 0, denomY = 0;
        for (int i = 0; i < n; i++) {
            double dx = x.get(i) - xMean;
            double dy = y.get(i) - yMean;
            num += dx * dy;
            denomX += dx * dx;
            denomY += dy * dy;
        }

        return (denomX == 0 || denomY == 0) ? 0 : num / Math.sqrt(denomX * denomY);
    }

    /**
     * 상관계수 → 해석용 enum 매핑
     */
    public Correlation toScore(double r) {
        if (r >= 0.7) return Correlation.strong_positive;
        else if (r >= 0.3) return Correlation.positive;
        else if (r > -0.3) return Correlation.neutral;
        else if (r > -0.7) return Correlation.negative;
        else return Correlation.strong_negative;
    }
}

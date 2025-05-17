package com.example.collector.service;

import com.example.collector.domain.Indicator;
import com.example.collector.domain.Sector;
import com.example.collector.domain.SectorSensitivity;
import com.example.collector.repository.IndicatorRepository;
import com.example.collector.repository.SectorRepository;
import com.example.collector.repository.SectorSensitivityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SectorSensitivityService {
    private final IndicatorRepository indicatorRepository;
    private final SectorRepository sectorRepository;
    private final SectorService sectorService;
    private final SectorSensitivityRepository sectorSensitivityRepository;

    @Transactional
    public void calculateHeatmap(String window) {
        List<String> indicatorCodes = indicatorRepository.findDistinctIndicatorCodes();
        List<Sector> sectors = sectorRepository.findAll();
        List<SectorSensitivity> saveList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Double> sectorReturnCache = new HashMap<>();
        sectorSensitivityRepository.deleteByWindow(window);

        for (String indicatorCode : indicatorCodes) {
            List<Indicator> list = indicatorRepository.findByCodeOrderByDate(indicatorCode);
            if (list.size() < 2) continue;

            List<Double> deltas = new ArrayList<>();
            List<LocalDate> dates = new ArrayList<>();

            for (int i = 1; i < list.size(); i++) {
                double delta = list.get(i).getValue() - list.get(i - 1).getValue();
                deltas.add(delta);
                dates.add(list.get(i).getDate()); // ΔI 기준일
            }

            // 마지막 Indicator 객체 (최근 날짜 기준)
            Indicator indicator = list.get(list.size() - 1);

            for (Sector sector : sectors) {
                List<Double> sectorReturns = new ArrayList<>();

                for (LocalDate date : dates) {
                    String key = sector.getId() + "_" + date + "_" + window;
                    Double r = sectorReturnCache.get(key);

                    if (r == null) {
                        r = sectorService.calculateSectorReturn(sector.getId(), date, window);
                        sectorReturnCache.put(key, r);
                    }

                    sectorReturns.add(r);
                }

                double correlation = pearson(normalize(deltas), normalize(sectorReturns));

                SectorSensitivity sensitivity = SectorSensitivity.builder()
                        .indicator(indicator)
                        .sector(sector)
                        .score(correlation)
                        .window(window)
                        .updatedAt(now)
                        .build();

                saveList.add(sensitivity);
            }
        }

        sectorSensitivityRepository.saveAll(saveList);
    }

    private List<Double> normalize(List<Double> values) {
        double mean = values.stream().mapToDouble(d -> d).average().orElse(0);
        double std = Math.sqrt(values.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0));
        return std == 0 ? values.stream().map(d -> 0.0).toList() : values.stream().map(d -> (d - mean) / std).toList();
    }

    private double pearson(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) return 0;

        double meanX = x.stream().mapToDouble(d -> d).average().orElse(0);
        double meanY = y.stream().mapToDouble(d -> d).average().orElse(0);

        double num = 0, denX = 0, denY = 0;
        for (int i = 0; i < x.size(); i++) {
            double dx = x.get(i) - meanX;
            double dy = y.get(i) - meanY;
            num += dx * dy;
            denX += dx * dx;
            denY += dy * dy;
        }

        return (denX == 0 || denY == 0) ? 0 : num / Math.sqrt(denX * denY);
    }

}

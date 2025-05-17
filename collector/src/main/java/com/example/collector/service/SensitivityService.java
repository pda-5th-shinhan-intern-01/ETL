package com.example.collector.service;

import com.example.collector.domain.EconomicEvent;
import com.example.collector.domain.Indicator;
import com.example.collector.domain.Stock;
import com.example.collector.domain.StockSensitivity;
import com.example.collector.repository.EconomicEventRepository;
import com.example.collector.repository.IndicatorRepository;
import com.example.collector.repository.StockRepository;
import com.example.collector.repository.StockSensitivityRepository;
import com.example.collector.util.IndicatorMapper;
import com.example.collector.util.RegressionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensitivityService {
    private final StockRepository stockRepository;
    private final IndicatorRepository indicatorRepository;
    private final StockSensitivityRepository sensitivityRepository;
    private final EconomicEventRepository economicEventRepository;

    public void calculateBeta(String ticker, String indicatorCode) {
        LocalDate threeYearsAgo = LocalDate.now().minusYears(3);

        List<Stock> stockList = stockRepository.findByTickerAndDateAfterOrderByDate(ticker, threeYearsAgo);
        List<Indicator> indicatorList = indicatorRepository.findByCodeAndDateAfterOrderByDate(indicatorCode, threeYearsAgo);
        Map<LocalDate, Double> rawReturnMap = calculateLogReturnMap(stockList);
        Map<LocalDate, Double> indicatorChangeMap = calculatePctChangeMap(indicatorList);
        Set<LocalDate> indicatorDates = indicatorChangeMap.keySet();

        Map<LocalDate, Double> filteredReturnMap = filterReturnsByIndicatorDates(rawReturnMap, indicatorDates);

        List<LocalDate> alignedDates = getAlignedDates(filteredReturnMap, indicatorChangeMap);
        if (alignedDates.size() < 3) return; // 회귀 최소 요건

        List<Double> alignedReturns = alignedDates.stream()
                .map(filteredReturnMap::get)
                .collect(Collectors.toList());

        List<Double> alignedChanges = alignedDates.stream()
                .map(indicatorChangeMap::get)
                .collect(Collectors.toList());

        double beta = RegressionUtil.simpleOLS(alignedChanges, alignedReturns);

        Stock stock = stockRepository.findTopByTickerOrderByDateDesc(ticker);
        Indicator indicator = indicatorRepository.findTopByCodeOrderByDateDesc(indicatorCode);

        StockSensitivity sensitivity = StockSensitivity.builder()
                .stock(stock)
                .indicator(indicator)
                .score(beta)
                .createdAt(LocalDate.now())
                .build();

        sensitivityRepository.save(sensitivity);
    }

    private Map<LocalDate, Double> calculateLogReturnMap(List<Stock> stockList) {
        Map<LocalDate, Double> result = new HashMap<>();
        for (int i = 1; i < stockList.size(); i++) {
            LocalDate date = stockList.get(i).getDate();
            double today = stockList.get(i).getClosePrice();
            double yesterday = stockList.get(i - 1).getClosePrice();
            result.put(date, Math.log(today / yesterday));
        }
        return result;
    }

    private Map<LocalDate, Double> calculatePctChangeMap(List<Indicator> indicatorList) {
        Map<LocalDate, Double> result = new HashMap<>();
        for (int i = 1; i < indicatorList.size(); i++) {
            LocalDate date = indicatorList.get(i).getDate();
            double today = indicatorList.get(i).getValue();
            double yesterday = indicatorList.get(i - 1).getValue();
            result.put(date, (today - yesterday) / yesterday);
        }
        return result;
    }

    private Map<LocalDate, Double> filterReturnsByIndicatorDates(
            Map<LocalDate, Double> stockReturnMap,
            Set<LocalDate> indicatorDates
    ) {
        Set<LocalDate> targetDates = new HashSet<>();
        for (LocalDate indicatorDate : indicatorDates) {
            for (int i = -3; i <= 3; i++) {
                LocalDate check = indicatorDate.plusDays(i);
                if (stockReturnMap.containsKey(check)) {
                    targetDates.add(check);
                }
            }
        }
        return stockReturnMap.entrySet().stream()
                .filter(entry -> targetDates.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<LocalDate> getAlignedDates(Map<LocalDate, Double> stockMap, Map<LocalDate, Double> indicatorMap) {
        Set<LocalDate> common = new HashSet<>(stockMap.keySet());
        common.retainAll(indicatorMap.keySet());
        List<LocalDate> sorted = new ArrayList<>(common);
        sorted.sort(Comparator.naturalOrder());
        return sorted;
    }


    public void calculatePerformance(){
        // 예상치 있는 이벤트 가져오기
        List<EconomicEvent> eventsWithForecast = economicEventRepository.findByPreviousIsNotNullAndForecastIsNotNull();

        // 퍼포먼스 계산하기
        for(EconomicEvent event : eventsWithForecast){
            String name = event.getName();
            String code = IndicatorMapper.getCode(name);
            if(code == null){
                continue;
            }
            String unit = IndicatorMapper.getUnit(code);

            Double prior = parseDouble(event.getPrevious());
            Double forecast = parseDouble(event.getForecast());
            if (prior==null || forecast==null) {
                System.out.println("⚠️ 수치 파싱 실패: previous&forecast"+ event.getPrevious()+"," +event.getForecast());
                continue; // 스킵
            }

            double deltaI = "percent".equals(unit)
                    ? (forecast - prior) /prior
                    : (forecast - prior);

            // indicator
            Indicator indicator = indicatorRepository.findTopByCodeOrderByDateDesc(code);

            List<StockSensitivity> sensitivities = sensitivityRepository.findByIndicatorCode(indicator.getCode());
            for (StockSensitivity sensitivity : sensitivities) {
                double expected = sensitivity.getScore()*deltaI*100;
                sensitivity.setPerformance(expected);
                sensitivity.setUnit(unit);
                System.out.printf("▶ [%s] β=%.4f, ΔI=%.4f, 예측=%.2f%%\n",
                        code, sensitivity.getScore(), deltaI, expected);
            }
            sensitivityRepository.saveAll(sensitivities);

        }
    }
    public static Double parseDouble(String raw){
        if(raw == null ) return null;
        raw = raw.trim().toUpperCase(); // 공백 제거 + 대문자 통일
        if (raw.isEmpty() || raw.equals("N/A")) return null;

        try {
            // 단위 변환
            if (raw.endsWith("K")) {
                return Double.parseDouble(raw.replace("K", "").replace(",", "").trim()) * 1_000;
            } else if (raw.endsWith("M")) {
                return Double.parseDouble(raw.replace("M", "").replace(",", "").trim()) * 1_000_000;
            } else if (raw.endsWith("%")) {
                return Double.parseDouble(raw.replace("%", "").replace(",", "").trim());
            } else {
                return Double.parseDouble(raw.replace(",", "").trim());
            }
        } catch (NumberFormatException e) {
            System.err.printf("❗ 숫자 파싱 실패: '%s'\n", raw);
            return null;
        }
    }
}

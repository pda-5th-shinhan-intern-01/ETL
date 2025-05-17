package com.example.collector.scheduler;

import com.example.collector.domain.Indicator;
import com.example.collector.repository.IndicatorRepository;
import com.example.collector.repository.StockRepository;
import com.example.collector.service.SensitivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensitivityScheduler {
    private final IndicatorRepository indicatorRepository;
    private final StockRepository stockRepository;
    private final SensitivityService sensitivityService;

//    @Scheduled(cron="0  10 * * ?")
//    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void runInitialBatch() {
        List<String> indicatorCodes = indicatorRepository.findDistinctIndicatorCodes();
        List<String> tickers = stockRepository.findAllTickers();

        for (String indicatorCode : indicatorCodes) {
            for (String ticker : tickers) {
                try {
                    sensitivityService.calculateBeta(ticker, indicatorCode);
                    System.out.println("✅ 초기 계산 완료: " + ticker + " - " + indicatorCode);
                } catch (Exception e) {
                    System.err.println("❌ 실패: " + ticker + " - " + indicatorCode + " / " + e.getMessage());
                }
            }
        }
    }

    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void runCalculate() {
        sensitivityService.calculatePerformance();
    }


//    @Scheduled(cron = "0 0 3 * * ?")
//    @Scheduled(cron = "0 10 10 * * *")
    public void runBatch() {
        LocalDate targetDate = LocalDate.now().minusDays(3);
        log.info("📌 [민감도 스케줄러 시작] 기준일: {}", targetDate);

        List<Indicator> indicators = indicatorRepository.findByDate(targetDate);
        List<String> tickers = stockRepository.findAllTickers();

        for (Indicator indicator : indicators) {
            String indicatorCode = indicator.getCode();

            for (String ticker : tickers) {
                try {
                    sensitivityService.calculateBeta(ticker, indicatorCode);
                    log.info("✅ 민감도 계산 완료: {} - {}", ticker, indicatorCode);
                } catch (Exception e) {
                    log.warn("❌ 민감도 계산 실패: {} - {} / {}", ticker, indicatorCode, e.getMessage());
                }
            }
        }

        log.info("✅ [민감도 스케줄러 종료]");
    }

}

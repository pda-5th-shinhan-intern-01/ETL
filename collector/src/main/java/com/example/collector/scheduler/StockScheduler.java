package com.example.collector.scheduler;

import com.example.collector.repository.StockRepository;
import com.example.collector.service.StockService;
import com.example.collector.util.TickerNameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockScheduler {
    private final StockService stockHistoryService;
    private final TickerNameMapper tickerNameMapper;
    private final StockRepository stockRepository;

//    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE) // 앱 시작 10초 후 1회 실행
    public void initAllStocks() {
        log.info("📈 종목 초기화 시작");

        for (String ticker : tickerNameMapper.getAllTickers()) {
            if (stockRepository.existsByTicker(ticker)) {
                log.info("⏩ 이미 초기화된 종목: {}", ticker);
                continue;
            }
            try {
                String t = tickerNameMapper.toYahooSymbol(ticker);
                stockHistoryService.fetchAndStore(t);
            } catch (Exception e) {
                log.error("❌ [{}] 초기화 실패: {}", ticker, e.getMessage());
            }
        }

        log.info("✅ 모든 종목 초기화 완료");
    }
}
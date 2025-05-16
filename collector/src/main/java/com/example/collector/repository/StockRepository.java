package com.example.collector.repository;

import com.example.collector.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock,Long> {

    boolean existsByTickerAndDate(String ticker, LocalDate date);

    List<Stock> findByTickerOrderByDate(String ticker);

    Stock findTopByTickerOrderByDateDesc(String ticker);

    @Query("SELECT DISTINCT s.ticker FROM Stock s")
    List<String> findAllTickers();

    boolean existsByTicker(String ticker);

    List<Stock> findByTickerAndDateAfterOrderByDate(String ticker, LocalDate threeYearsAgo);
}

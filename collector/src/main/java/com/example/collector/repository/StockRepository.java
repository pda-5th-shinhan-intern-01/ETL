package com.example.collector.repository;

import com.example.collector.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Stock> findByTickerAndDateAfter(String ticker, LocalDate fromDate);

    List<Stock> findBySectorId(Long sectorId);

    @Query(value = """
        SELECT * FROM stock 
        WHERE ticker = :ticker 
          AND date <= :baseDate 
        ORDER BY date DESC 
        LIMIT 1 OFFSET :offset
    """, nativeQuery = true)
    Stock findStockBeforeDateWithOffset(@Param("ticker") String ticker,
                                        @Param("baseDate") LocalDate baseDate,
                                        @Param("offset") int offset);
    @Query(value = """
    SELECT * FROM stock 
    WHERE ticker = :ticker 
      AND date > :baseDate 
    ORDER BY date ASC 
    LIMIT 1 OFFSET :offset
""", nativeQuery = true)
    Stock findStockAfterDateWithOffset(@Param("ticker") String ticker,
                                       @Param("baseDate") LocalDate baseDate,
                                       @Param("offset") int offset);

    @Query("SELECT DISTINCT s.ticker FROM Stock s WHERE s.sector.id = :sectorId")
    List<String> findDistinctTickersBySectorId(@Param("sectorId") Long sectorId);


}

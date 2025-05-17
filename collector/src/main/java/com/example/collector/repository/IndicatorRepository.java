package com.example.collector.repository;

import com.example.collector.domain.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface IndicatorRepository extends JpaRepository<Indicator,Long> {

    boolean existsByCodeAndDate(String code, LocalDate date);

    List<Indicator> findByCodeOrderByDate(String indicatorCode);

    Indicator findTopByCodeOrderByDateDesc(String indicatorCode);
    @Query("SELECT DISTINCT i.code FROM Indicator i")
    List<String> findDistinctIndicatorCodes();
    List<Indicator> findByDate(LocalDate targetDate);

    List<Indicator> findByCodeAndDateAfterOrderByDate(String indicatorCode, LocalDate threeYearsAgo);

    Indicator findByCode(String indicator);
}

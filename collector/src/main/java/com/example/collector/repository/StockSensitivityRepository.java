package com.example.collector.repository;

import com.example.collector.domain.StockSensitivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockSensitivityRepository extends JpaRepository<StockSensitivity,Long> {
    List<StockSensitivity> findByIndicatorCode(String code);
}

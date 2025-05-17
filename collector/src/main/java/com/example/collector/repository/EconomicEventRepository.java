package com.example.collector.repository;

import com.example.collector.domain.EconomicEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EconomicEventRepository extends JpaRepository<EconomicEvent, Long> {
    List<EconomicEvent> findByPreviousIsNotNullAndForecastIsNotNull();
}

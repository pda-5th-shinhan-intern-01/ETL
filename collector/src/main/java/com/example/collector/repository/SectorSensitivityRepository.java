package com.example.collector.repository;

import com.example.collector.domain.SectorSensitivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectorSensitivityRepository extends JpaRepository<SectorSensitivity,Long> {
    void deleteByWindow(String window);
}

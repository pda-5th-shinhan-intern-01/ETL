package com.example.collector.repository;

import com.example.collector.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector,Long> {
    Sector findByName(String sector);
}

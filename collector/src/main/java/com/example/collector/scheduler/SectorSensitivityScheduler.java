package com.example.collector.scheduler;

import com.example.collector.service.SectorSensitivityService;
import com.example.collector.service.SectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SectorSensitivityScheduler {
    private final SectorSensitivityService sectorSensitivityService;
    private final SectorService sectorService;

//    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void CalculateHeatMap(){
        // 섹터 종가 평균도 계산
        log.info("종가 평균 계산");
        sectorService.updateSectorReturn(LocalDate.now().minusDays(3));
        log.info("heatmap 계산 시작");
        sectorSensitivityService.calculateHeatmap("당일");
        sectorSensitivityService.calculateHeatmap("1일");
        sectorSensitivityService.calculateHeatmap("3일");
    }
}


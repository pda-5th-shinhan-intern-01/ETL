package com.example.collector.controller;

import com.example.collector.service.IndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/etl/economics")
public class IndicatorController {
    private final IndicatorService indicatorService;

    @PostMapping("/history/{indicatorCode}")
    public ResponseEntity<?> fetch(@PathVariable String indicatorCode) {
        try {
            indicatorService.fetchAndStore(indicatorCode);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid indicator code: " + indicatorCode);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch/store: " + e.getMessage());
        }
    }
}

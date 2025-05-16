package com.example.collector.controller;

import com.example.collector.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/etl/stocks")
public class StockController {
    private final StockService stockService;

    // 초기 데이터 세팅 용
    @PostMapping("/history/{ticker}")
    public ResponseEntity<?> getOhlcv(@PathVariable String ticker) {
        try{
            stockService.fetchAndStore(ticker);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid ticker" + ticker);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch/store: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    //일간 종목 데이터 조회 용
//    @GetMapping("/{ticker}")
//    public ResponseEntity<> getStockData(@PathVariable String ticker) {
//
//    }

}

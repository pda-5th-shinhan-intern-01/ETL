package com.example.collector.service;

import com.example.collector.domain.Sector;
import com.example.collector.domain.Stock;
import com.example.collector.repository.SectorRepository;
import com.example.collector.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SectorService {

    private final StockRepository stockRepository;
    private final SectorRepository sectorRepository;

    public void updateSectorReturn(LocalDate baseDate){
        List<Sector> sectors = sectorRepository.findAll();
        for(Sector sector : sectors){
            double returnRate = calculateSectorReturn(sector.getId(),baseDate,"당일");
            System.out.println("returnRate: " + returnRate);
            sector.setChangeRate(returnRate);
            sectorRepository.save(sector);
        }
    }
    public double calculateSectorReturn(Long sectorId, LocalDate baseDate, String window){
        List<String> tickers = stockRepository.findDistinctTickersBySectorId(sectorId);

        double totalCap = 0;
        double weightSum = 0;

        for(String ticker : tickers){
            Double r = getReturn(ticker,baseDate,window);
            Long cap = getMarketCap(ticker,baseDate,window);
            if(r==null || cap==null) continue;

            weightSum+=cap*r;
            totalCap+=cap;
        }
        System.out.println("sectorId: "+sectorId+", baseDate : "+baseDate   +" totalCap : "+totalCap+", weightSum : "+weightSum+", window :"+window);

        Double rate= totalCap == 0 ? 0 : weightSum/totalCap;
        return rate;
    }


    public Double getPriceBefore(String ticker, LocalDate baseDate, int n) {
        Stock stock = stockRepository.findStockBeforeDateWithOffset(ticker, baseDate, n);
        return stock != null ? stock.getClosePrice() : null;
    }

    public Double getPriceAfter(String ticker, LocalDate baseDate, int n) {
        Stock stock = stockRepository.findStockAfterDateWithOffset(ticker, baseDate, n);
        return stock != null ? stock.getClosePrice() : null;
    }

    public Long getMarketCapBefore(String ticker, LocalDate baseDate, int n) {
        Stock stock = stockRepository.findStockBeforeDateWithOffset(ticker, baseDate, n);
        return stock != null ? stock.getMarketCap() : null;
    }

    public Stock getRawStockBefore(String ticker, LocalDate baseDate, int n) {
        return stockRepository.findStockBeforeDateWithOffset(ticker, baseDate, n);
    }
    private Double getReturn(String ticker, LocalDate baseDate, String window) {
        Double before = null;
        Double after = null;

        switch (window) {
            case "당일":
                before = getPriceBefore(ticker, baseDate,1);
                after = getPriceAfter(ticker, baseDate,0);
                break;
            case "1일":
                before = getPriceBefore(ticker, baseDate,1);
                after = getPriceAfter(ticker, baseDate,1);
                break;
            case "3일":
                before = getPriceBefore(ticker, baseDate,3);
                after = getPriceAfter(ticker, baseDate,3);
                break;
            default:
                return null;
        }

        if (before == null || after == null || before == 0) return null;
        return (after - before) / before;
    }

    private Long getMarketCap(String ticker, LocalDate baseDate, String window) {
        int offset = switch (window) {
            case "당일", "1일" -> 1;
            case "3일" -> 3;
            default -> 0;
        };
        return getMarketCapBefore(ticker, baseDate, offset);
    }
}

package com.example.collector.service;

import com.example.collector.domain.Stock;
import com.example.collector.repository.StockRepository;
import com.example.collector.util.TickerNameMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final StockRepository stockRepository;
    private final TickerNameMapper nameMapper;
    @Value("${api.fred.key}")
    private String apiKey;

    public void fetchAndStore(String ticker) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker +
                "?interval=1d&range=" + "3y";

        try {
            // User-Agent 헤더 추가
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 요청 전송
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class
            );
            String response = responseEntity.getBody();

            if (response == null) return;

            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.path("chart").path("result").get(0);
            JsonNode timestamps = result.path("timestamp");
            JsonNode quote = result.path("indicators").path("quote").get(0);

            ArrayNode refinedArray = objectMapper.createArrayNode();
            List<Stock> stockList = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                long epoch = timestamps.get(i).asLong();
                LocalDate date = Instant.ofEpochSecond(epoch)
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toLocalDate();
                String dateStr = date.toString();

                JsonNode open = quote.path("open").get(i);
                JsonNode high = quote.path("high").get(i);
                JsonNode low = quote.path("low").get(i);
                JsonNode close = quote.path("close").get(i);
                JsonNode volume = quote.path("volume").get(i);

                // 필터링: null 또는 NaN 데이터 skip
                if (open.isNull() || close.isNull() || volume.isNull()) continue;

                ObjectNode ohlcv = objectMapper.createObjectNode();
                ohlcv.put("date", dateStr);
                ohlcv.put("open", open.asDouble());
                ohlcv.put("high", high.asDouble());
                ohlcv.put("low", low.asDouble());
                ohlcv.put("close", close.asDouble());
                ohlcv.put("volume", volume.asLong());

                refinedArray.add(ohlcv);
                if (stockRepository.existsByTickerAndDate(ticker, date)) continue;

                Stock stock = Stock.builder()
                        .ticker(ticker)
                        .name(nameMapper.getKoreanName(ticker))
                        .date(date)
                        .openPrice(open.asDouble())
                        .closePrice(close.asDouble())
                        .highPrice(high.asDouble())
                        .lowPrice(low.asDouble())
                        .volume(volume.asLong())
                        .build();

                stockList.add(stock);
            }

            //redis에 저장
            String redisKey = "stock:" + ticker.toUpperCase();
            String json = objectMapper.writeValueAsString(refinedArray);
            redisTemplate.opsForValue().set(redisKey, json);

            //db에 저장
            if (!stockList.isEmpty()) {
                stockRepository.saveAll(stockList);
            }
        } catch (Exception e) {
            e.printStackTrace(); // 필요시 로그로
        }
    }

    public void updateMarketCaps(LocalDate fromDate) {
        for (String ticker : nameMapper.getAllTickers()) {
            String yahooTicker = TickerNameMapper.toYahooSymbol(ticker);
            Long shares = getFloatShares(yahooTicker);

            if (shares == null) {
                System.err.println("❌ [" + ticker + "] sharesOutstanding null");
                continue;
            }

            List<Stock> prices = stockRepository.findByTickerAndDateAfter(ticker, fromDate);

            for (Stock price : prices) {
                if (price.getClosePrice() != null) {
                    long marketCap = (long) (price.getClosePrice() * shares);
                    price.setMarketCap(marketCap);
                }
            }

            stockRepository.saveAll(prices);
            stockRepository.flush();
            System.out.println("✅ [" + ticker + "] 시가총액 저장 완료 (" + shares + " 주)");

            try {
                Thread.sleep(500); // rate limit 대응
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final String API_URL = "https://financialmodelingprep.com/stable/shares-float?symbol=%s&apikey=%s";

    public Long getFloatShares(String ticker) {
        try {
            String url = String.format(API_URL, ticker, apiKey);
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            if (root.isArray() && root.size() > 0) {
                JsonNode data = root.get(0);
                return data.get("floatShares").asLong();
            }
        } catch (Exception e) {
            System.err.printf("❌ [%s] 유통주식수 조회 실패: %s%n", ticker, e.getMessage());
        }

        return null;
    }
}

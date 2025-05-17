package com.example.collector.service;

import com.example.collector.domain.Indicator;
import com.example.collector.repository.IndicatorRepository;
import com.example.collector.util.IndicatorMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicatorService {
    private final RedisTemplate<String, String> redisTemplate;
    private final IndicatorMapper indicatorMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IndicatorRepository indicatorRepository;

    public void fetchAndStore(String indicatorCode) {
        String url = indicatorMapper.getApiUrl(indicatorCode);
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) return;
        List<Indicator> indicatorList = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode observations = root.path("observations");

            if (!observations.isMissingNode() && observations.isArray()) {
                ArrayNode refinedArray = objectMapper.createArrayNode();

                for (JsonNode item : observations) {
                    String dateStr = item.path("date").asText();
                    LocalDate date = LocalDate.parse(dateStr);
                    String valueStr = item.path("value").asText();
                    Double value = Double.parseDouble(valueStr);

                    // 필터링: value가 "." 또는 빈 문자열이면 스킵
                    if (valueStr.equals(".") || valueStr.isEmpty()) continue;

                    ObjectNode obj = objectMapper.createObjectNode();
                    obj.put("date", dateStr);
                    obj.put("value", valueStr);
                    refinedArray.add(obj);

                    if(indicatorRepository.existsByCodeAndDate(indicatorCode,date)) continue;

                    Indicator indicator = Indicator.builder()
                            .code(indicatorCode)
                            .name(IndicatorMapper.getName(indicatorCode))
                            .date(date)
                            .value(value)
                            .build();
                    indicatorList.add(indicator);
                }

                String compactJson = objectMapper.writeValueAsString(refinedArray);
                String redisKey = "economic:history:" + indicatorCode;
                redisTemplate.opsForValue().set(redisKey, compactJson);

                if(!indicatorList.isEmpty()){
                    indicatorRepository.saveAll(indicatorList);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.example.collector.service;

import com.example.collector.util.IndicatorMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class EconomicService {
    private final RedisTemplate<String, String> redisTemplate;
    private final IndicatorMapper indicatorMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void fetchAndStore(String indicatorCode) {
        String url = indicatorMapper.getApiUrl(indicatorCode);
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) return;

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode observations = root.path("observations");

            if (!observations.isMissingNode() && observations.isArray()) {
                ArrayNode refinedArray = objectMapper.createArrayNode();

                for (JsonNode item : observations) {
                    String date = item.path("date").asText();
                    String value = item.path("value").asText();

                    // 필터링: value가 "." 또는 빈 문자열이면 스킵
                    if (value.equals(".") || value.isEmpty()) continue;

                    ObjectNode obj = objectMapper.createObjectNode();
                    obj.put("date", date);
                    obj.put("value", value);
                    refinedArray.add(obj);
                }

                String compactJson = objectMapper.writeValueAsString(refinedArray);
                String redisKey = "economic:history:" + indicatorCode;
                redisTemplate.opsForValue().set(redisKey, compactJson);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

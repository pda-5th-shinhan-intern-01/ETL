package com.example.collector.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Generated;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndicatorMapper {
    @Value("${api.fred.key}")
    private String apiKey;
    private static final String BASE_URL = "https://api.stlouisfed.org/fred/series/observations";
    private static final Map<String, String> SERIES_ID_MAP = Map.of("CORE_CPI", "CPILFESL", "CORE_PPI", "WPSFD49207", "CORE_PCE", "PCEPILFE", "GDP", "A191RL1Q225SBEA", "ISM_PMI", "NAPM", "NFP", "PAYEMS", "UNEMPLOYMENT", "UNRATE", "RETAIL_SALES", "RSXFS", "INDUSTRIAL_PRODUCTION", "INDPRO");

    public String getApiUrl(String indicatorCode) {
        String seriesId = (String)SERIES_ID_MAP.get(indicatorCode);
        if (seriesId == null) {
            throw new IllegalArgumentException("Unknown indicator code: " + indicatorCode);
        } else {
            String observationStart = LocalDate.now().minusYears(3L).format(DateTimeFormatter.ISO_DATE);
            return "https://api.stlouisfed.org/fred/series/observations?series_id=" + seriesId + "&api_key=" + this.apiKey + "&file_type=json&observation_start=" + observationStart;
        }
    }

    private static final Map<String, String> INDICATOR_KR_MAP = Map.ofEntries(
            Map.entry("CORE_CPI", "근원 소비자물가지수"),
            Map.entry("CORE_PPI", "근원 생산자물가지수"),
            Map.entry("CORE_PCE", "근원 개인소비지출물가지수"),
            Map.entry("GDP", "국내총생산 분기 성장률"),
            Map.entry("RETAIL_SALES", "근원 소매판매"),
            Map.entry("INDUSTRIAL_PRODUCTION", "산업생산"),
            Map.entry("UNEMPLOYMENT", "실업률"),
            Map.entry("NFP", "비농업부문 신규 고용자 수")
    );
    private static final Map<String, String> KR_TO_CODE_MAP = INDICATOR_KR_MAP.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    public static String getCode(String nameKr) {
        return KR_TO_CODE_MAP.get(nameKr);
    }

    public static String getName(String code){
        return INDICATOR_KR_MAP.get(code);
    }

    private static final Map<String, String> UNIT_MAP = Map.ofEntries(
            Map.entry("CORE_CPI", "percent"),              // 근원 소비자물가지수 (%)
            Map.entry("CORE_PPI", "percent"),              // 근원 생산자물가지수 (%)
            Map.entry("CORE_PCE", "percent"),              // 근원 개인소비지출물가지수 (%)
            Map.entry("GDP", "percent"),                   // 국내총생산 분기 성장률 (%)
            Map.entry("RETAIL_SALES", "percent"),          // 근원 소매판매 (%)
            Map.entry("INDUSTRIAL_PRODUCTION", "percent"), // 산업생산 (%)
            Map.entry("UNEMPLOYMENT", "percent"),          // 실업률 (%)
            Map.entry("NFP", "absolute")                   // 비농업부문 신규 고용자 수 (수치)
    );

    public static String getUnit(String code){
        return UNIT_MAP.get(code);
    }
}

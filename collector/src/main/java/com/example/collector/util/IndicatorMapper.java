package com.example.collector.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IndicatorMapper {

    @Value("${api.fred.key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.stlouisfed.org/fred/series/observations";

    private static final Map<String, String> SERIES_ID_MAP = Map.of(
            "CORE_CPI", "CPILFESL",
            "CORE_PPI", "WPSFD49207",
            "CORE_PCE", "PCEPILFE",
            "GDP", "A191RL1Q225SBEA",
            "ISM_PMI", "NAPM",
            "NFP", "PAYEMS",
            "UNEMPLOYMENT", "UNRATE",
            "RETAIL_SALES", "RSXFS",
            "INDUSTRIAL_PRODUCTION", "INDPRO"
    );

    public String getApiUrl(String indicatorCode) {
        String seriesId = SERIES_ID_MAP.get(indicatorCode);
        if (seriesId == null) {
            throw new IllegalArgumentException("Unknown indicator code: " + indicatorCode);
        }

        // observation_start: 오늘 기준 5년 전
        String observationStart = LocalDate.now().minusYears(3).format(DateTimeFormatter.ISO_DATE);

        return BASE_URL + "?series_id=" + seriesId
                + "&api_key=" + apiKey
                + "&file_type=json"
                + "&observation_start=" + observationStart;
    }
}

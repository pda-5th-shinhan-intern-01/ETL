package com.example.collector.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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

    @Generated
    public IndicatorMapper() {
    }
}

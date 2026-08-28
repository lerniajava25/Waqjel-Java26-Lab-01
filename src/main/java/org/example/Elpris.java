package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Elpris(
        @JsonProperty("SEK_per_kWh") Double sekPerKwh,
        @JsonProperty("EUR_per_kWh") Double eurPerKwh,
        @JsonProperty("EXR") Double exr,
        @JsonProperty("time_start") String timeStart,
        @JsonProperty("time_end") String timeEnd
) {
}

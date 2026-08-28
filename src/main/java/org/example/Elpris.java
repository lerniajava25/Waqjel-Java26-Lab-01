package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record representing electricity price data for a specific hour.
 * Contains prices in both SEK and EUR per kWh, exchange rate, and time range information.
 *
 * @param sekPerKwh price in Swedish Kronor per kilowatt-hour
 * @param eurPerKwh price in Euro per kilowatt-hour
 * @param exr exchange rate used for currency conversion
 * @param timeStart start time of the pricing period in ISO 8601 format
 * @param timeEnd end time of the pricing period in ISO 8601 format
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Elpris(
        @JsonProperty("SEK_per_kWh") Double sekPerKwh,
        @JsonProperty("EUR_per_kWh") Double eurPerKwh,
        @JsonProperty("EXR") Double exr,
        @JsonProperty("time_start") String timeStart,
        @JsonProperty("time_end") String timeEnd
) {
}

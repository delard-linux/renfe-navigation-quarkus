package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Station information
 */
public record StationDTO(
    @JsonProperty("station_code") String stationCode,
    @JsonProperty("administration_code") String administrationCode,
    Integer priority,
    String description,
    @JsonProperty("station_name") String stationName,
    @JsonProperty("uic_code") String uicCode,
    String key,
    @JsonProperty("station_name_plano") String stationNamePlano
) {
}


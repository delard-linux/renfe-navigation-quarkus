package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO for Trains Response
 */
public record TrainsResponseDTO(
    String origin,
    String destination,
    @JsonProperty("date_out") String dateOut,
    @JsonProperty("date_return") String dateReturn,
    String adults,
    @JsonProperty("trains_out") List<TrainDTO> trainsOut,
    @JsonProperty("trains_return") List<TrainDTO> trainsReturn
) {
    /**
     * Compact constructor for defensive copying
     */
    public TrainsResponseDTO {
        // Defensive copy of trainsOut list
        if (trainsOut == null) {
            trainsOut = Collections.emptyList();
        } else {
            trainsOut = new ArrayList<>(trainsOut);
        }
        // Defensive copy of trainsReturn list (can be null)
        if (trainsReturn != null) {
            trainsReturn = new ArrayList<>(trainsReturn);
        }
    }

}

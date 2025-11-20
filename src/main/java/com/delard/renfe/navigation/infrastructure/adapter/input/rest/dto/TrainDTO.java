package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Train information
 */
public record TrainDTO(
    @JsonProperty("train_id") String trainId,
    @JsonProperty("service_type") String serviceType,
    @JsonProperty("departure_time") String departureTime,
    @JsonProperty("arrival_time") String arrivalTime,
    String duration,
    @JsonProperty("price_from") double priceFrom,
    String currency,
    List<FareOptionDTO> fares,
    List<String> badges,
    boolean accessible,
    @JsonProperty("eco_friendly") boolean ecoFriendly,
    TrainConnectionDTO connection
) {
    /**
     * Compact constructor for default values and defensive copying
     */
    public TrainDTO {
        // Default currency if null
        if (currency == null) {
            currency = "EUR";
        }
        // Defensive copy of fares list
        if (fares == null) {
            fares = new ArrayList<>();
        } else {
            fares = new ArrayList<>(fares);
        }
        // Defensive copy of badges list
        if (badges == null) {
            badges = new ArrayList<>();
        } else {
            badges = new ArrayList<>(badges);
        }
    }

}

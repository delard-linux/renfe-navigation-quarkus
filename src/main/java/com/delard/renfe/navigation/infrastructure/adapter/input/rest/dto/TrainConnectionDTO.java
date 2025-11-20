package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Train Connection information
 */
public record TrainConnectionDTO(
    String duration,
    @JsonProperty("first_train_type") String firstTrainType,
    @JsonProperty("second_train_type") String secondTrainType
) {
}


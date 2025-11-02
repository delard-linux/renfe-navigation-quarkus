package com.renfe.navigation.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DTO for Trains Response
 */
public class TrainsResponseDTO {

    private String origin;
    private String destination;

    @JsonProperty("date_out")
    private String dateOut;

    @JsonProperty("date_return")
    private String dateReturn;

    private int adults;

    @JsonProperty("trains_out")
    private List<TrainDTO> trainsOut;

    @JsonProperty("trains_return")
    private List<TrainDTO> trainsReturn;

    public TrainsResponseDTO() {
    }

    // Getters and Setters
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDateOut() {
        return dateOut;
    }

    public void setDateOut(String dateOut) {
        this.dateOut = dateOut;
    }

    public String getDateReturn() {
        return dateReturn;
    }

    public void setDateReturn(String dateReturn) {
        this.dateReturn = dateReturn;
    }

    public int getAdults() {
        return adults;
    }

    public void setAdults(int adults) {
        this.adults = adults;
    }

    public List<TrainDTO> getTrainsOut() {
        return trainsOut != null ? new ArrayList<>(trainsOut) : Collections.emptyList();
    }

    public void setTrainsOut(List<TrainDTO> trainsOut) {
        this.trainsOut = trainsOut != null ? new ArrayList<>(trainsOut) : null;
    }

    public List<TrainDTO> getTrainsReturn() {
        return trainsReturn != null ? new ArrayList<>(trainsReturn) : null;
    }

    public void setTrainsReturn(List<TrainDTO> trainsReturn) {
        this.trainsReturn = trainsReturn != null ? new ArrayList<>(trainsReturn) : null;
    }
}


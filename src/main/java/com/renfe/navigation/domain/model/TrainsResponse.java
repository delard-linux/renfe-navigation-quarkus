package com.renfe.navigation.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result containing trains for outbound and optional return journey
 */
public class TrainsResponse {
    private String origin;
    private String destination;
    private String dateOut;
    private String dateReturn;
    private int adults;
    private List<Train> trainsOut;
    private List<Train> trainsReturn;

    public TrainsResponse() {
    }

    public TrainsResponse(String origin, String destination, String dateOut, String dateReturn,
                          int adults, List<Train> trainsOut, List<Train> trainsReturn) {
        this.origin = origin;
        this.destination = destination;
        this.dateOut = dateOut;
        this.dateReturn = dateReturn;
        this.adults = adults;
        this.trainsOut = trainsOut != null ? new ArrayList<>(trainsOut) : null;
        this.trainsReturn = trainsReturn != null ? new ArrayList<>(trainsReturn) : null;
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

    public List<Train> getTrainsOut() {
        return trainsOut != null ? new ArrayList<>(trainsOut) : null;
    }

    public void setTrainsOut(List<Train> trainsOut) {
        this.trainsOut = trainsOut != null ? new ArrayList<>(trainsOut) : null;
    }

    public List<Train> getTrainsReturn() {
        return trainsReturn != null ? new ArrayList<>(trainsReturn) : null;
    }

    public void setTrainsReturn(List<Train> trainsReturn) {
        this.trainsReturn = trainsReturn != null ? new ArrayList<>(trainsReturn) : null;
    }
}


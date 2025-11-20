package com.delard.renfe.navigation.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Train with all fares and details
 */
public class Train {
    private String trainId;
    private String serviceType;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private double priceFrom;
    private String currency;
    private List<FareOption> fares;
    private List<String> badges;
    private boolean accessible;
    private boolean ecoFriendly;
    private TrainConnection connection;

    public Train() {
        this.currency = "EUR";
        this.fares = new ArrayList<>();
        this.badges = new ArrayList<>();
        this.accessible = false;
        this.ecoFriendly = false;
    }

    public Train(String trainId, String serviceType, String departureTime, String arrivalTime,
                 String duration, double priceFrom) {
        this();
        this.trainId = trainId;
        this.serviceType = serviceType;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.priceFrom = priceFrom;
    }

    // Getters and Setters
    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(double priceFrom) {
        this.priceFrom = priceFrom;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<FareOption> getFares() {
        return new ArrayList<>(fares);
    }

    public void setFares(List<FareOption> fares) {
        this.fares = fares != null ? new ArrayList<>(fares) : new ArrayList<>();
    }

    public List<String> getBadges() {
        return new ArrayList<>(badges);
    }

    public void setBadges(List<String> badges) {
        this.badges = badges != null ? new ArrayList<>(badges) : new ArrayList<>();
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public boolean isEcoFriendly() {
        return ecoFriendly;
    }

    public void setEcoFriendly(boolean ecoFriendly) {
        this.ecoFriendly = ecoFriendly;
    }

    public TrainConnection getConnection() {
        return connection;
    }

    public void setConnection(TrainConnection connection) {
        this.connection = connection;
    }
}


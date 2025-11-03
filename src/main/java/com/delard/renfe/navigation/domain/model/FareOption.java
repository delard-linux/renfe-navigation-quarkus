package com.delard.renfe.navigation.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Individual train fare option
 */
public class FareOption {
    private String name;
    private double price;
    private String currency;
    private String code;
    private String tpEnlace;
    private List<String> features;

    public FareOption() {
        this.currency = "EUR";
        this.features = new ArrayList<>();
    }

    public FareOption(String name, double price, String currency, String code, String tpEnlace, List<String> features) {
        this.name = name;
        this.price = price;
        this.currency = currency != null ? currency : "EUR";
        this.code = code;
        this.tpEnlace = tpEnlace;
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTpEnlace() {
        return tpEnlace;
    }

    public void setTpEnlace(String tpEnlace) {
        this.tpEnlace = tpEnlace;
    }

    public List<String> getFeatures() {
        return new ArrayList<>(features);
    }

    public void setFeatures(List<String> features) {
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }
}


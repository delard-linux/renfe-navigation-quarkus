package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for individual train fare
 */
public class FareOptionDTO {

    private String name;
    private Double price;
    private String currency = "EUR";
    private String code;

    @JsonProperty("tp_enlace")
    private String tpEnlace;

    private String plan;

    private List<String> features = new ArrayList<>();

    public FareOptionDTO() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
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

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public List<String> getFeatures() {
        return new ArrayList<>(features);
    }

    public void setFeatures(List<String> features) {
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }
}


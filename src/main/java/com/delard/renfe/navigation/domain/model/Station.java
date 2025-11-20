/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.model;


import java.util.Objects;


/**
 * Domain model representing a Renfe station
 */
public class Station
{
    private String stationCode;
    private String administrationCode;
    private Integer priority;
    private String description;
    private String stationName;
    private String uicCode;
    private String key;
    private String stationNamePlano;

    public Station()
    {
    }

    public Station(String stationCode, String administrationCode, Integer priority,
            String description, String stationName, String uicCode,
            String key, String stationNamePlano)
    {
        this.stationCode = stationCode;
        this.administrationCode = administrationCode;
        this.priority = priority;
        this.description = description;
        this.stationName = stationName;
        this.uicCode = uicCode;
        this.key = key;
        this.stationNamePlano = stationNamePlano;
    }

    public String getStationCode()
    {
        return stationCode;
    }

    public void setStationCode(String stationCode)
    {
        this.stationCode = stationCode;
    }

    public String getAdministrationCode()
    {
        return administrationCode;
    }

    public void setAdministrationCode(String administrationCode)
    {
        this.administrationCode = administrationCode;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getStationName()
    {
        return stationName;
    }

    public void setStationName(String stationName)
    {
        this.stationName = stationName;
    }

    public String getUicCode()
    {
        return uicCode;
    }

    public void setUicCode(String uicCode)
    {
        this.uicCode = uicCode;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getStationNamePlano()
    {
        return stationNamePlano;
    }

    public void setStationNamePlano(String stationNamePlano)
    {
        this.stationNamePlano = stationNamePlano;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Station station = (Station)o;
        return Objects.equals(stationCode, station.stationCode) &&
                Objects.equals(administrationCode, station.administrationCode) &&
                Objects.equals(key, station.key);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(stationCode, administrationCode, key);
    }

    @Override
    public String toString()
    {
        return "Station{" +
                "stationCode='" + stationCode + '\'' +
                ", stationName='" + stationName + '\'' +
                ", stationNamePlano='" + stationNamePlano + '\'' +
                ", uicCode='" + uicCode + '\'' +
                '}';
    }
}

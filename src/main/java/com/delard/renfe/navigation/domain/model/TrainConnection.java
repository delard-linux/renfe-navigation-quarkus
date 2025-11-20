/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.domain.model;

/**
 * Represents a connection/link between two trains in a journey
 */
public class TrainConnection
{
    private String duration;
    private String firstTrainType;
    private String secondTrainType;

    public TrainConnection()
    {
    }

    public TrainConnection(String duration, String firstTrainType, String secondTrainType)
    {
        this.duration = duration;
        this.firstTrainType = firstTrainType;
        this.secondTrainType = secondTrainType;
    }

    public String getDuration()
    {
        return duration;
    }

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public String getFirstTrainType()
    {
        return firstTrainType;
    }

    public void setFirstTrainType(String firstTrainType)
    {
        this.firstTrainType = firstTrainType;
    }

    public String getSecondTrainType()
    {
        return secondTrainType;
    }

    public void setSecondTrainType(String secondTrainType)
    {
        this.secondTrainType = secondTrainType;
    }
}

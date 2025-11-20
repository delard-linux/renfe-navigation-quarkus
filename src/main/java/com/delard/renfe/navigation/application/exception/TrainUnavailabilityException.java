/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.application.exception;

/**
 * Exception thrown when train tickets are not available for the requested route and date.
 * This exception includes detailed information about which direction (outbound/return) has no availability.
 */
public class TrainUnavailabilityException extends RuntimeException
{

    private final String direction;
    private final String detailMessage;

    public TrainUnavailabilityException(String direction, String detailMessage)
    {
        super(String.format("Error searching trains for %s: %s", direction, detailMessage));
        this.direction = direction;
        this.detailMessage = detailMessage;
    }

    public TrainUnavailabilityException(String direction, String detailMessage, Throwable cause)
    {
        super(String.format("Error searching trains for %s: %s", direction, detailMessage), cause);
        this.direction = direction;
        this.detailMessage = detailMessage;
    }

    public String getDirection()
    {
        return direction;
    }

    public String getDetailMessage()
    {
        return detailMessage;
    }
}

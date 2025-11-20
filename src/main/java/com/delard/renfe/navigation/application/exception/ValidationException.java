/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.application.exception;

/**
 * Exception thrown when validation of input parameters fails
 */
public class ValidationException extends RuntimeException
{

    public ValidationException(String message)
    {
        super(message);
    }

    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

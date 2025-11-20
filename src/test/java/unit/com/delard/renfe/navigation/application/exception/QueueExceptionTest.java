/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.application.exception;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for QueueException
 */
class QueueExceptionTest
{

    @Test
    void testConstructorWithMessage()
    {
        String message = "Ticket purchase is queued";
        QueueException exception = new QueueException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause()
    {
        String message = "Ticket purchase is queued";
        Throwable cause = new RuntimeException("Queue system error");
        QueueException exception = new QueueException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithNullMessage()
    {
        QueueException exception = new QueueException((String)null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithEmptyMessage()
    {
        QueueException exception = new QueueException("");

        assertNotNull(exception);
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithNullCause()
    {
        String message = "Ticket purchase is queued";
        QueueException exception = new QueueException(message, null);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException()
    {
        QueueException exception = new QueueException("Test message");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown()
    {
        QueueException exception = new QueueException("Test message");

        assertThrows(QueueException.class, () -> {
            throw exception;
        });
    }
}

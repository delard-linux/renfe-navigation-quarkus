package com.delard.renfe.navigation.application.exception;

/**
 * Exception thrown when ticket purchase is queued and the system redirects to a queue management page
 */
public class QueueException extends RuntimeException {

    public QueueException(String message) {
        super(message);
    }

    public QueueException(String message, Throwable cause) {
        super(message, cause);
    }
}


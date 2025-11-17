package com.delard.renfe.navigation.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationException
 */
class ValidationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Validation error message";
        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Validation error message";
        Throwable cause = new IllegalArgumentException("Root cause");
        ValidationException exception = new ValidationException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithNullMessage() {
        ValidationException exception = new ValidationException((String) null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        ValidationException exception = new ValidationException("");

        assertNotNull(exception);
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithNullCause() {
        String message = "Validation error message";
        ValidationException exception = new ValidationException(message, null);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ValidationException exception = new ValidationException("Test message");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown() {
        ValidationException exception = new ValidationException("Test message");

        assertThrows(ValidationException.class, () -> {
            throw exception;
        });
    }
}


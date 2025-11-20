package com.delard.renfe.navigation.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainUnavailabilityException
 */
class TrainUnavailabilityExceptionTest {

    @Test
    void testConstructorWithDirectionAndDetailMessage() {
        String direction = "outbound";
        String detailMessage = "No hay trenes disponibles para la fecha seleccionada";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        assertNotNull(exception);
        assertEquals(direction, exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertNull(exception.getCause());
        assertTrue(exception.getMessage().contains("Error searching trains for outbound"));
        assertTrue(exception.getMessage().contains(detailMessage));
    }

    @Test
    void testConstructorWithDirectionDetailMessageAndCause() {
        String direction = "return";
        String detailMessage = "No hay billetes de vuelta disponibles";
        Throwable cause = new RuntimeException("Timeout error");
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage, cause);

        assertNotNull(exception);
        assertEquals(direction, exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception.getMessage().contains("Error searching trains for return"));
        assertTrue(exception.getMessage().contains(detailMessage));
    }

    @Test
    void testConstructorWithNullDirection() {
        String detailMessage = "No trains available";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(null, detailMessage);

        assertNotNull(exception);
        assertNull(exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertTrue(exception.getMessage().contains("null"));
        assertTrue(exception.getMessage().contains(detailMessage));
    }

    @Test
    void testConstructorWithEmptyDirection() {
        String direction = "";
        String detailMessage = "No trains available";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        assertNotNull(exception);
        assertEquals("", exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertTrue(exception.getMessage().contains(detailMessage));
    }

    @Test
    void testConstructorWithNullDetailMessage() {
        String direction = "outbound";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, null);

        assertNotNull(exception);
        assertEquals(direction, exception.getDirection());
        assertNull(exception.getDetailMessage());
        assertTrue(exception.getMessage().contains("Error searching trains for outbound"));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void testConstructorWithEmptyDetailMessage() {
        String direction = "outbound";
        String detailMessage = "";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        assertNotNull(exception);
        assertEquals(direction, exception.getDirection());
        assertEquals("", exception.getDetailMessage());
        assertTrue(exception.getMessage().contains("Error searching trains for outbound"));
    }

    @Test
    void testConstructorWithNullCause() {
        String direction = "return";
        String detailMessage = "No trains available";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage, null);

        assertNotNull(exception);
        assertEquals(direction, exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageFormatForOutbound() {
        String direction = "outbound";
        String detailMessage = "No hay trenes disponibles para la fecha seleccionada";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        String expectedMessage = String.format("Error searching trains for %s: %s", direction, detailMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testMessageFormatForReturn() {
        String direction = "return";
        String detailMessage = "No hay billetes de vuelta disponibles";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        String expectedMessage = String.format("Error searching trains for %s: %s", direction, detailMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testGetDirectionReturnsCorrectValue() {
        String direction = "outbound";
        String detailMessage = "Error message";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        assertEquals(direction, exception.getDirection());
    }

    @Test
    void testGetDetailMessageReturnsCorrectValue() {
        String direction = "return";
        String detailMessage = "Detailed error message from website";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        assertEquals(detailMessage, exception.getDetailMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        TrainUnavailabilityException exception = new TrainUnavailabilityException("outbound", "Test message");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown() {
        TrainUnavailabilityException exception = new TrainUnavailabilityException("outbound", "Test message");

        assertThrows(TrainUnavailabilityException.class, () -> {
            throw exception;
        });
    }

    @Test
    void testExceptionWithLongDetailMessage() {
        String direction = "outbound";
        String detailMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        assertNotNull(exception);
        assertEquals(direction, exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertTrue(exception.getMessage().contains(detailMessage));
    }

    @Test
    void testExceptionWithSpecialCharactersInDetailMessage() {
        String direction = "return";
        String detailMessage = "Error: ¡No hay billetes! 日本語 @#$%^&*()";
        TrainUnavailabilityException exception = new TrainUnavailabilityException(direction, detailMessage);

        assertNotNull(exception);
        assertEquals(direction, exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertTrue(exception.getMessage().contains(detailMessage));
    }
}


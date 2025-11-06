package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FlowResponseDTO
 */
class FlowResponseDTOTest {

    @Test
    void testDefaultConstructor() {
        FlowResponseDTO dto = new FlowResponseDTO();
        assertNotNull(dto);
        assertNull(dto.getMessage());
        assertNull(dto.getFilepath());
    }

    @Test
    void testParameterizedConstructor() {
        FlowResponseDTO dto = new FlowResponseDTO("Success", "/path/to/file");
        assertEquals("Success", dto.getMessage());
        assertEquals("/path/to/file", dto.getFilepath());
    }

    @Test
    void testGettersAndSetters() {
        FlowResponseDTO dto = new FlowResponseDTO();

        // Test message
        dto.setMessage("Flow completed successfully");
        assertEquals("Flow completed successfully", dto.getMessage());

        // Test filepath
        dto.setFilepath("/tmp/flow-result.html");
        assertEquals("/tmp/flow-result.html", dto.getFilepath());
    }

    @Test
    void testSetNullValues() {
        FlowResponseDTO dto = new FlowResponseDTO("Initial", "/initial/path");
        dto.setMessage(null);
        dto.setFilepath(null);
        assertNull(dto.getMessage());
        assertNull(dto.getFilepath());
    }

    @Test
    void testAllFields() {
        FlowResponseDTO dto = new FlowResponseDTO("Test message", "/test/path");
        assertEquals("Test message", dto.getMessage());
        assertEquals("/test/path", dto.getFilepath());

        dto.setMessage("Updated message");
        dto.setFilepath("/updated/path");
        assertEquals("Updated message", dto.getMessage());
        assertEquals("/updated/path", dto.getFilepath());
    }
}


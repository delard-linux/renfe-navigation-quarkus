package com.renfe.navigation.infrastructure.adapter.input.rest.dto;

/**
 * DTO for Flow Response
 */
public class FlowResponseDTO {

    private String message;
    private String filepath;

    public FlowResponseDTO() {
    }

    public FlowResponseDTO(String message, String filepath) {
        this.message = message;
        this.filepath = filepath;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}


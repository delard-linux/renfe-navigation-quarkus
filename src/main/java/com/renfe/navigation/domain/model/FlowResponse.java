package com.renfe.navigation.domain.model;

/**
 * Result of the flow operation
 */
public class FlowResponse {
    private String message;
    private String filepath;

    public FlowResponse() {
    }

    public FlowResponse(String message, String filepath) {
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


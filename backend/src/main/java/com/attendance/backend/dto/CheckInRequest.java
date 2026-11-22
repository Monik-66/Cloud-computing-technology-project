package com.attendance.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CheckInRequest {

    @NotBlank(message = "Session token is required")
    private String sessionToken;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    public CheckInRequest() {
    }

    public CheckInRequest(String sessionToken, Long studentId) {
        this.sessionToken = sessionToken;
        this.studentId = studentId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}

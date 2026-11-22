package com.attendance.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for outgoing department response.
 * Written with standard Java getters, setters, and constructors.
 */
public class DepartmentResponse {

    private Long id;
    private String departmentName;
    private String departmentCode;
    private LocalDateTime createdAt;

    public DepartmentResponse() {
    }

    public DepartmentResponse(Long id, String departmentName, String departmentCode, LocalDateTime createdAt) {
        this.id = id;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

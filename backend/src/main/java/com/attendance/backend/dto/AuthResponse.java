package com.attendance.backend.dto;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private Long profileId; // Student ID or Faculty ID
    private String email;
    private String fullName;
    private String role;
    private String departmentName;
    private String className;
    private String labBatch;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, Long profileId, String email, String fullName,
                        String role, String departmentName, String className, String labBatch) {
        this.token = token;
        this.type = "Bearer";
        this.userId = userId;
        this.profileId = profileId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.departmentName = departmentName;
        this.className = className;
        this.labBatch = labBatch;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getLabBatch() {
        return labBatch;
    }

    public void setLabBatch(String labBatch) {
        this.labBatch = labBatch;
    }
}

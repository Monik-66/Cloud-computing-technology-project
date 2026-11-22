package com.attendance.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Maps to 'departments' table in PostgreSQL.
 * Written with standard Java getters, setters, and constructors.
 */
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "department_code", nullable = false, unique = true, length = 20)
    private String departmentCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default Constructor (Required by JPA/Hibernate)
    public Department() {
    }

    // All-Arguments Constructor
    public Department(Long id, String departmentName, String departmentCode, LocalDateTime createdAt) {
        this.id = id;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.createdAt = createdAt;
    }

    // Standard Getters and Setters
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

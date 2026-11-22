package com.attendance.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"department_id", "batch_year", "semester", "section"})
})
public class CollegeClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "batch_year", nullable = false)
    private Integer batchYear;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false, length = 20)
    private String section; // e.g. "5CSE21"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_advisor_id")
    private Faculty classAdvisor; // e.g. HEMASHREE R

    @Column(name = "default_room", length = 30)
    private String defaultRoom = "A403";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CollegeClass() {
    }

    public CollegeClass(Department department, Integer batchYear, Integer semester, String section, Faculty classAdvisor, String defaultRoom) {
        this.department = department;
        this.batchYear = batchYear;
        this.semester = semester;
        this.section = section;
        this.classAdvisor = classAdvisor;
        this.defaultRoom = defaultRoom;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Integer getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(Integer batchYear) {
        this.batchYear = batchYear;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Faculty getClassAdvisor() {
        return classAdvisor;
    }

    public void setClassAdvisor(Faculty classAdvisor) {
        this.classAdvisor = classAdvisor;
    }

    public String getDefaultRoom() {
        return defaultRoom;
    }

    public void setDefaultRoom(String defaultRoom) {
        this.defaultRoom = defaultRoom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

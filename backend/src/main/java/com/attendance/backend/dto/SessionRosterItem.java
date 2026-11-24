package com.attendance.backend.dto;

import com.attendance.backend.entity.AttendanceMethod;
import com.attendance.backend.entity.AttendanceStatus;

import java.time.LocalDateTime;

public class SessionRosterItem {

    private Long studentId;
    private String registerNumber; // SRN
    private String rollNumber;
    private String fullName;
    private String labBatch;
    private AttendanceStatus status;
    private AttendanceMethod verificationMethod;
    private LocalDateTime markedAt;

    public SessionRosterItem() {
    }

    public SessionRosterItem(Long studentId, String registerNumber, String rollNumber,
                             String fullName, String labBatch, AttendanceStatus status,
                             AttendanceMethod verificationMethod, LocalDateTime markedAt) {
        this.studentId = studentId;
        this.registerNumber = registerNumber;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.labBatch = labBatch;
        this.status = status;
        this.verificationMethod = verificationMethod;
        this.markedAt = markedAt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLabBatch() {
        return labBatch;
    }

    public void setLabBatch(String labBatch) {
        this.labBatch = labBatch;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public AttendanceMethod getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(AttendanceMethod verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
}

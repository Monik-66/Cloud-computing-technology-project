package com.attendance.backend.dto;

import com.attendance.backend.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ManualAttendanceRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    private List<StudentStatusItem> records;

    public ManualAttendanceRequest() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public List<StudentStatusItem> getRecords() {
        return records;
    }

    public void setRecords(List<StudentStatusItem> records) {
        this.records = records;
    }

    public static class StudentStatusItem {
        private Long studentId;
        private AttendanceStatus status;
        private String remarks;

        public StudentStatusItem() {
        }

        public StudentStatusItem(Long studentId, AttendanceStatus status, String remarks) {
            this.studentId = studentId;
            this.status = status;
            this.remarks = remarks;
        }

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public AttendanceStatus getStatus() {
            return status;
        }

        public void setStatus(AttendanceStatus status) {
            this.status = status;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}

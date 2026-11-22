package com.attendance.backend.dto;

import com.attendance.backend.entity.AttendanceMethod;
import jakarta.validation.constraints.NotNull;

public class AttendanceSessionRequest {

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private String targetBatch = "ALL"; // "ALL", "B1", "B2"

    private Integer durationMinutes = 60;

    private AttendanceMethod attendanceMethod = AttendanceMethod.QR_AND_MANUAL;

    private String roomNumber;

    public AttendanceSessionRequest() {
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getTargetBatch() {
        return targetBatch;
    }

    public void setTargetBatch(String targetBatch) {
        this.targetBatch = targetBatch;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public AttendanceMethod getAttendanceMethod() {
        return attendanceMethod;
    }

    public void setAttendanceMethod(AttendanceMethod attendanceMethod) {
        this.attendanceMethod = attendanceMethod;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}

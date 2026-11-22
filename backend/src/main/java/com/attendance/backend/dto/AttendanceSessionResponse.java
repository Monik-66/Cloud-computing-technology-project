package com.attendance.backend.dto;

import com.attendance.backend.entity.AttendanceMethod;
import com.attendance.backend.entity.SessionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceSessionResponse {

    private Long sessionId;
    private Long classId;
    private String section;
    private Long subjectId;
    private String courseName;
    private String shortCode;
    private String facultyName;
    private LocalDate sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String targetBatch;
    private String sessionToken;
    private SessionStatus status;
    private AttendanceMethod attendanceMethod;
    private String roomNumber;
    private int presentCount;
    private int totalEligibleCount;

    public AttendanceSessionResponse() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTargetBatch() {
        return targetBatch;
    }

    public void setTargetBatch(String targetBatch) {
        this.targetBatch = targetBatch;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
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

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getTotalEligibleCount() {
        return totalEligibleCount;
    }

    public void setTotalEligibleCount(int totalEligibleCount) {
        this.totalEligibleCount = totalEligibleCount;
    }
}

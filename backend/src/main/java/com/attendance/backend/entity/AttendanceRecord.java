package com.attendance.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"session_id", "student_id"}, name = "uq_session_student")
})
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession attendanceSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", length = 20)
    private AttendanceMethod verificationMethod = AttendanceMethod.QR_CODE;

    @Column(name = "marked_at", updatable = false)
    private LocalDateTime markedAt = LocalDateTime.now();

    @Column(length = 255)
    private String remarks;

    public AttendanceRecord() {
    }

    public AttendanceRecord(AttendanceSession attendanceSession, Student student, AttendanceStatus status,
                            AttendanceMethod verificationMethod, String remarks) {
        this.attendanceSession = attendanceSession;
        this.student = student;
        this.status = status;
        this.verificationMethod = verificationMethod;
        this.remarks = remarks;
        this.markedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttendanceSession getAttendanceSession() {
        return attendanceSession;
    }

    public void setAttendanceSession(AttendanceSession attendanceSession) {
        this.attendanceSession = attendanceSession;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

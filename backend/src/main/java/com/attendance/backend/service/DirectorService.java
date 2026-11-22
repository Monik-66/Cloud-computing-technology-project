package com.attendance.backend.service;

import com.attendance.backend.dto.StudentAttendanceStatsResponse;
import com.attendance.backend.entity.AuditLog;
import com.attendance.backend.entity.Student;
import com.attendance.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DirectorService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CollegeClassRepository classRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final StudentService studentService;

    public DirectorService(StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           CollegeClassRepository classRepository,
                           AttendanceRecordRepository recordRepository,
                           AttendanceSessionRepository sessionRepository,
                           AuditLogRepository auditLogRepository,
                           StudentService studentService) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.classRepository = classRepository;
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
        this.auditLogRepository = auditLogRepository;
        this.studentService = studentService;
    }

    /**
     * Get aggregate statistics across the institution for Director/Dean dashboard
     */
    public Map<String, Object> getOverallDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalStudents = studentRepository.count();
        long totalFaculty = facultyRepository.count();
        long totalClasses = classRepository.count();
        long totalSessions = sessionRepository.count();
        long totalRecords = recordRepository.count();

        stats.put("totalStudents", totalStudents);
        stats.put("totalFaculty", totalFaculty);
        stats.put("totalClasses", totalClasses);
        stats.put("totalAttendanceSessions", totalSessions);
        stats.put("totalAttendanceRecords", totalRecords);

        return stats;
    }

    /**
     * Get list of students whose attendance percentage is below threshold (e.g. 75%)
     */
    public List<StudentAttendanceStatsResponse> getLowAttendanceStudents(double threshold) {
        List<Student> allStudents = studentRepository.findAll();
        List<StudentAttendanceStatsResponse> lowAttendanceList = new ArrayList<>();

        for (Student student : allStudents) {
            StudentAttendanceStatsResponse stats = studentService.getAttendanceStats(student.getId());
            if (stats.getTotalSessions() > 0 && stats.getOverallPercentage() < threshold) {
                lowAttendanceList.add(stats);
            }
        }

        lowAttendanceList.sort(Comparator.comparingDouble(StudentAttendanceStatsResponse::getOverallPercentage));
        return lowAttendanceList;
    }

    /**
     * Get audit logs for accountability
     */
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}

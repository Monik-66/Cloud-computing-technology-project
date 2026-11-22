package com.attendance.backend.controller;

import com.attendance.backend.dto.NotificationDto;
import com.attendance.backend.dto.StudentAttendanceStatsResponse;
import com.attendance.backend.dto.TimetableSlotDto;
import com.attendance.backend.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Get student attendance stats (overall % and per-subject breakdown)
     * GET /api/v1/student/{id}/attendance
     */
    @GetMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'DIRECTOR')")
    public ResponseEntity<StudentAttendanceStatsResponse> getAttendanceStats(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getAttendanceStats(id));
    }

    /**
     * Get student weekly timetable (filtered by their section and lab batch B1/B2)
     * GET /api/v1/student/{id}/timetable
     */
    @GetMapping("/{id}/timetable")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'DIRECTOR')")
    public ResponseEntity<List<TimetableSlotDto>> getStudentTimetable(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentTimetable(id));
    }

    /**
     * Get student notifications & alerts
     * GET /api/v1/student/{id}/notifications
     */
    @GetMapping("/{id}/notifications")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'DIRECTOR')")
    public ResponseEntity<List<NotificationDto>> getStudentNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentNotifications(id));
    }
}

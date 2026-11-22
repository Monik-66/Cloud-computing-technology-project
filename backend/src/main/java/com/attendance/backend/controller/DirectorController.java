package com.attendance.backend.controller;

import com.attendance.backend.dto.StudentAttendanceStatsResponse;
import com.attendance.backend.entity.AuditLog;
import com.attendance.backend.service.DirectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/director")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('DIRECTOR')")
public class DirectorController {

    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    /**
     * Get aggregate statistics across institution
     * GET /api/v1/director/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(directorService.getOverallDashboardStats());
    }

    /**
     * Get students with low attendance below a threshold
     * GET /api/v1/director/low-attendance?threshold=75.0
     */
    @GetMapping("/low-attendance")
    public ResponseEntity<List<StudentAttendanceStatsResponse>> getLowAttendanceStudents(
            @RequestParam(defaultValue = "75.0") double threshold) {
        return ResponseEntity.ok(directorService.getLowAttendanceStudents(threshold));
    }

    /**
     * View audit logs
     * GET /api/v1/director/audit-logs
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(directorService.getAuditLogs());
    }
}

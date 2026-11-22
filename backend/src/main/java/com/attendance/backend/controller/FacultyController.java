package com.attendance.backend.controller;

import com.attendance.backend.dto.AttendanceSessionResponse;
import com.attendance.backend.dto.TimetableSlotDto;
import com.attendance.backend.service.FacultyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty")
@CrossOrigin(origins = "*")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    /**
     * Get weekly schedule for teacher
     * GET /api/v1/faculty/{id}/timetable
     */
    @GetMapping("/{id}/timetable")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<List<TimetableSlotDto>> getFacultyTimetable(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getFacultyTimetable(id));
    }

    /**
     * Get active/ongoing attendance sessions conducted by this teacher
     * GET /api/v1/faculty/{id}/sessions/active
     */
    @GetMapping("/{id}/sessions/active")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<List<AttendanceSessionResponse>> getActiveSessions(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getActiveSessions(id));
    }

    /**
     * Get past attendance sessions history
     * GET /api/v1/faculty/{id}/sessions/history
     */
    @GetMapping("/{id}/sessions/history")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<List<AttendanceSessionResponse>> getSessionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getSessionHistory(id));
    }
}

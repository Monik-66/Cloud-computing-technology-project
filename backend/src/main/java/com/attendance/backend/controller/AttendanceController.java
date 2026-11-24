package com.attendance.backend.controller;

import com.attendance.backend.dto.AttendanceSessionRequest;
import com.attendance.backend.dto.AttendanceSessionResponse;
import com.attendance.backend.dto.CheckInRequest;
import com.attendance.backend.dto.ManualAttendanceRequest;
import com.attendance.backend.dto.SessionRosterItem;
import com.attendance.backend.entity.AttendanceRecord;
import com.attendance.backend.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Teacher starts an attendance session and receives the dynamic QR code token
     * POST /api/v1/attendance/session/start?facultyId=1
     */
    @PostMapping("/session/start")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<AttendanceSessionResponse> startSession(
            @RequestParam Long facultyId,
            @Valid @RequestBody AttendanceSessionRequest request) {
        AttendanceSessionResponse response = attendanceService.startSession(facultyId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Student scans QR code and marks attendance
     * POST /api/v1/attendance/check-in
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'DIRECTOR')")
    public ResponseEntity<?> checkIn(@Valid @RequestBody CheckInRequest request) {
        try {
            AttendanceRecord record = attendanceService.checkIn(request);
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * Teacher views live attendance status of a session
     * GET /api/v1/attendance/session/{id}/live
     */
    @GetMapping("/session/{id}/live")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<AttendanceSessionResponse> getLiveSession(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getLiveSession(id));
    }

    /**
     * Teacher ends/closes the session (unverified students marked ABSENT)
     * POST /api/v1/attendance/session/{id}/close
     */
    @PostMapping("/session/{id}/close")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<AttendanceSessionResponse> closeSession(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.closeSession(id));
    }

    /**
     * Teacher cancels the session: marks CANCELLED and clears records
     * POST /api/v1/attendance/session/{id}/cancel
     */
    @PostMapping("/session/{id}/cancel")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<AttendanceSessionResponse> cancelSession(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.cancelSession(id));
    }

    /**
     * Teacher views live roster of all students in session with their present/absent status
     * GET /api/v1/attendance/session/{id}/roster
     */
    @GetMapping("/session/{id}/roster")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<List<SessionRosterItem>> getSessionRoster(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getSessionRoster(id));
    }

    /**
     * Teacher manually marks/overrides attendance
     * POST /api/v1/attendance/manual
     */
    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('FACULTY', 'DIRECTOR')")
    public ResponseEntity<?> manualMarkAttendance(@RequestBody ManualAttendanceRequest request) {
        attendanceService.manualMarkAttendance(request);
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Attendance updated successfully"));
    }
}

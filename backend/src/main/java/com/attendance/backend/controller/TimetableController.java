package com.attendance.backend.controller;

import com.attendance.backend.dto.TimetableSlotDto;
import com.attendance.backend.service.TimetableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timetable")
@CrossOrigin(origins = "*")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    /**
     * Get timetable for a section (e.g. 5CSE21)
     * GET /api/v1/timetable/class/{classId}
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<List<TimetableSlotDto>> getTimetableByClassId(@PathVariable Long classId) {
        return ResponseEntity.ok(timetableService.getTimetableByClassId(classId));
    }

    /**
     * Add a timetable period/slot
     * POST /api/v1/timetable/slot
     */
    @PostMapping("/slot")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<TimetableSlotDto> createSlot(@Valid @RequestBody TimetableSlotDto dto) {
        TimetableSlotDto created = timetableService.createSlot(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}

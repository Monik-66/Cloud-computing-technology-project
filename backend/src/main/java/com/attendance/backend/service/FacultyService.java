package com.attendance.backend.service;

import com.attendance.backend.dto.AttendanceSessionResponse;
import com.attendance.backend.dto.TimetableSlotDto;
import com.attendance.backend.entity.AttendanceSession;
import com.attendance.backend.entity.Faculty;
import com.attendance.backend.entity.SessionStatus;
import com.attendance.backend.entity.TimetableSlot;
import com.attendance.backend.repository.AttendanceRecordRepository;
import com.attendance.backend.repository.AttendanceSessionRepository;
import com.attendance.backend.repository.FacultyRepository;
import com.attendance.backend.repository.TimetableSlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;

    public FacultyService(FacultyRepository facultyRepository,
                          TimetableSlotRepository timetableSlotRepository,
                          AttendanceSessionRepository sessionRepository,
                          AttendanceRecordRepository recordRepository) {
        this.facultyRepository = facultyRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
    }

    /**
     * Get weekly schedule for a teacher
     */
    public List<TimetableSlotDto> getFacultyTimetable(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found with id: " + facultyId));

        return timetableSlotRepository.findByFacultyIdOrderByDayOfWeekAscPeriodNumberAsc(facultyId)
                .stream()
                .map(this::mapSlotToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active/ongoing sessions conducted by this faculty
     */
    public List<AttendanceSessionResponse> getActiveSessions(Long facultyId) {
        return sessionRepository.findByFacultyIdAndStatus(facultyId, SessionStatus.ACTIVE)
                .stream()
                .map(this::mapSessionToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get past sessions conducted by this faculty
     */
    public List<AttendanceSessionResponse> getSessionHistory(Long facultyId) {
        return sessionRepository.findByFacultyIdOrderByStartTimeDesc(facultyId)
                .stream()
                .map(this::mapSessionToResponse)
                .collect(Collectors.toList());
    }

    private TimetableSlotDto mapSlotToDto(TimetableSlot s) {
        TimetableSlotDto dto = new TimetableSlotDto();
        dto.setId(s.getId());
        dto.setClassId(s.getCollegeClass().getId());
        dto.setSection(s.getCollegeClass().getSection());
        dto.setSubjectId(s.getSubject().getId());
        dto.setCourseName(s.getSubject().getCourseName());
        dto.setShortCode(s.getSubject().getShortCode());
        dto.setFacultyId(s.getFaculty().getId());
        dto.setFacultyName(s.getFaculty().getFullName());
        dto.setDayOfWeek(s.getDayOfWeek());
        dto.setPeriodNumber(s.getPeriodNumber());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setTargetBatch(s.getTargetBatch());
        dto.setRoomNumber(s.getRoomNumber());
        return dto;
    }

    private AttendanceSessionResponse mapSessionToResponse(AttendanceSession s) {
        AttendanceSessionResponse res = new AttendanceSessionResponse();
        res.setSessionId(s.getId());
        res.setClassId(s.getCollegeClass().getId());
        res.setSection(s.getCollegeClass().getSection());
        res.setSubjectId(s.getSubject().getId());
        res.setCourseName(s.getSubject().getCourseName());
        res.setShortCode(s.getSubject().getShortCode());
        res.setFacultyName(s.getFaculty().getFullName());
        res.setSessionDate(s.getSessionDate());
        res.setStartTime(s.getStartTime());
        res.setEndTime(s.getEndTime());
        res.setTargetBatch(s.getTargetBatch());
        res.setSessionToken(s.getSessionToken());
        res.setStatus(s.getStatus());
        res.setAttendanceMethod(s.getAttendanceMethod());
        res.setRoomNumber(s.getRoomNumber());
        res.setPresentCount(recordRepository.findByAttendanceSessionId(s.getId()).size());
        return res;
    }
}

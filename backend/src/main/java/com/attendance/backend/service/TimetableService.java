package com.attendance.backend.service;

import com.attendance.backend.dto.TimetableSlotDto;
import com.attendance.backend.entity.CollegeClass;
import com.attendance.backend.entity.Faculty;
import com.attendance.backend.entity.Subject;
import com.attendance.backend.entity.TimetableSlot;
import com.attendance.backend.repository.CollegeClassRepository;
import com.attendance.backend.repository.FacultyRepository;
import com.attendance.backend.repository.SubjectRepository;
import com.attendance.backend.repository.TimetableSlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    private final TimetableSlotRepository timetableSlotRepository;
    private final CollegeClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;

    public TimetableService(TimetableSlotRepository timetableSlotRepository,
                            CollegeClassRepository classRepository,
                            SubjectRepository subjectRepository,
                            FacultyRepository facultyRepository) {
        this.timetableSlotRepository = timetableSlotRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
    }

    public List<TimetableSlotDto> getTimetableByClassId(Long classId) {
        return timetableSlotRepository.findByCollegeClassIdOrderByDayOfWeekAscPeriodNumberAsc(classId)
                .stream()
                .map(this::mapSlotToDto)
                .collect(Collectors.toList());
    }

    public TimetableSlotDto createSlot(TimetableSlotDto dto) {
        CollegeClass collegeClass = classRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + dto.getClassId()));

        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + dto.getSubjectId()));

        Faculty faculty = facultyRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty not found with id: " + dto.getFacultyId()));

        TimetableSlot slot = new TimetableSlot(
                collegeClass,
                subject,
                faculty,
                dto.getDayOfWeek().toUpperCase(),
                dto.getPeriodNumber(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getTargetBatch() != null ? dto.getTargetBatch() : "ALL",
                dto.getRoomNumber() != null ? dto.getRoomNumber() : collegeClass.getDefaultRoom()
        );

        TimetableSlot saved = timetableSlotRepository.save(slot);
        return mapSlotToDto(saved);
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
}

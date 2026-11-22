package com.attendance.backend.service;

import com.attendance.backend.dto.NotificationDto;
import com.attendance.backend.dto.StudentAttendanceStatsResponse;
import com.attendance.backend.dto.TimetableSlotDto;
import com.attendance.backend.entity.*;
import com.attendance.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final NotificationRepository notificationRepository;

    public StudentService(StudentRepository studentRepository,
                          AttendanceRecordRepository recordRepository,
                          AttendanceSessionRepository sessionRepository,
                          EnrollmentRepository enrollmentRepository,
                          TimetableSlotRepository timetableSlotRepository,
                          NotificationRepository notificationRepository) {
        this.studentRepository = studentRepository;
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.timetableSlotRepository = timetableSlotRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Compute real-time attendance statistics (overall % + subject-by-subject breakdown)
     */
    public StudentAttendanceStatsResponse getAttendanceStats(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        List<AttendanceRecord> records = recordRepository.findByStudentId(studentId);

        long totalSessions = records.size();
        long attendedSessions = records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .count();

        double overallPct = totalSessions > 0 ? (attendedSessions * 100.0) / totalSessions : 100.0;
        overallPct = Math.round(overallPct * 100.0) / 100.0;

        // Group by subject
        Map<Subject, List<AttendanceRecord>> bySubject = new HashMap<>();
        for (AttendanceRecord r : records) {
            Subject sub = r.getAttendanceSession().getSubject();
            bySubject.computeIfAbsent(sub, k -> new ArrayList<>()).add(r);
        }

        List<StudentAttendanceStatsResponse.SubjectAttendanceItem> subjectStats = new ArrayList<>();
        for (Map.Entry<Subject, List<AttendanceRecord>> entry : bySubject.entrySet()) {
            Subject sub = entry.getKey();
            List<AttendanceRecord> subRecords = entry.getValue();

            long subTotal = subRecords.size();
            long subAttended = subRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            double subPct = subTotal > 0 ? (subAttended * 100.0) / subTotal : 100.0;
            subPct = Math.round(subPct * 100.0) / 100.0;

            subjectStats.add(new StudentAttendanceStatsResponse.SubjectAttendanceItem(
                    sub.getId(),
                    sub.getCourseCode(),
                    sub.getCourseName(),
                    sub.getShortCode(),
                    subTotal,
                    subAttended,
                    subPct
            ));
        }

        return new StudentAttendanceStatsResponse(
                student.getId(),
                student.getFullName(),
                student.getRollNumber(),
                overallPct,
                totalSessions,
                attendedSessions,
                subjectStats
        );
    }

    /**
     * Get weekly timetable for a student, filtered by their section and lab batch (B1 / B2)
     */
    public List<TimetableSlotDto> getStudentTimetable(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        Long classId = student.getCollegeClass().getId();
        String studentBatch = student.getLabBatch();

        List<TimetableSlot> allSlots = timetableSlotRepository
                .findByCollegeClassIdOrderByDayOfWeekAscPeriodNumberAsc(classId);

        // Filter: Theory classes ('ALL') or student's specific lab batch ('B1' or 'B2')
        return allSlots.stream()
                .filter(slot -> "ALL".equalsIgnoreCase(slot.getTargetBatch())
                        || studentBatch.equalsIgnoreCase(slot.getTargetBatch()))
                .map(this::mapSlotToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get student's notifications
     */
    public List<NotificationDto> getStudentNotifications(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(student.getUser().getId())
                .stream()
                .map(n -> new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.getIsRead(), n.getCreatedAt()))
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
}

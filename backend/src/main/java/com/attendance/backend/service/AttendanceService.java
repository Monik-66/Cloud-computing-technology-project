package com.attendance.backend.service;

import com.attendance.backend.dto.AttendanceSessionRequest;
import com.attendance.backend.dto.AttendanceSessionResponse;
import com.attendance.backend.dto.CheckInRequest;
import com.attendance.backend.dto.ManualAttendanceRequest;
import com.attendance.backend.dto.SessionRosterItem;
import com.attendance.backend.entity.*;
import com.attendance.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CollegeClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AttendanceService(AttendanceSessionRepository sessionRepository,
                             AttendanceRecordRepository recordRepository,
                             StudentRepository studentRepository,
                             FacultyRepository facultyRepository,
                             CollegeClassRepository classRepository,
                             SubjectRepository subjectRepository,
                             EnrollmentRepository enrollmentRepository) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Teacher starts an attendance session and generates a dynamic QR session token
     */
    @Transactional
    public AttendanceSessionResponse startSession(Long facultyId, AttendanceSessionRequest request) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found with id: " + facultyId));

        CollegeClass collegeClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + request.getClassId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + request.getSubjectId()));

        LocalDateTime now = LocalDateTime.now();
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 60;
        LocalDateTime end = now.plusMinutes(duration);

        // Generate unique dynamic session token for QR code
        String sessionToken = "ATT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String room = request.getRoomNumber() != null ? request.getRoomNumber() : collegeClass.getDefaultRoom();
        String targetBatch = request.getTargetBatch() != null ? request.getTargetBatch() : "ALL";

        AttendanceSession session = new AttendanceSession(
                collegeClass,
                subject,
                faculty,
                LocalDate.now(),
                now,
                end,
                targetBatch,
                sessionToken,
                SessionStatus.ACTIVE,
                request.getAttendanceMethod(),
                room
        );

        AttendanceSession saved = sessionRepository.save(session);
        return mapToResponse(saved);
    }

    /**
     * Student scans QR code via Android App or Web and marks attendance
     */
    @Transactional
    public AttendanceRecord checkIn(CheckInRequest request) {
        AttendanceSession session = sessionRepository.findBySessionToken(request.getSessionToken())
                .orElseThrow(() -> new RuntimeException("Invalid session QR token"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Attendance session is no longer active");
        }

        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            session.setStatus(SessionStatus.COMPLETED);
            sessionRepository.save(session);
            throw new RuntimeException("Attendance session has expired");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + request.getStudentId()));

        // Verify class match
        if (!student.getCollegeClass().getId().equals(session.getCollegeClass().getId())) {
            throw new RuntimeException("Student does not belong to section " + session.getCollegeClass().getSection());
        }

        // Verify lab batch if applicable
        if (!"ALL".equalsIgnoreCase(session.getTargetBatch())
                && !session.getTargetBatch().equalsIgnoreCase(student.getLabBatch())) {
            throw new RuntimeException("Session is only for Batch " + session.getTargetBatch() +
                    ", but you belong to Batch " + student.getLabBatch());
        }

        // Prevent duplicate check-in
        if (recordRepository.existsByAttendanceSessionIdAndStudentId(session.getId(), student.getId())) {
            throw new RuntimeException("Attendance already marked for this session");
        }

        AttendanceRecord record = new AttendanceRecord(
                session,
                student,
                AttendanceStatus.PRESENT,
                AttendanceMethod.QR_CODE,
                "Checked in via QR code"
        );

        return recordRepository.save(record);
    }

    /**
     * Teacher manually marks/overrides attendance for students
     */
    @Transactional
    public void manualMarkAttendance(ManualAttendanceRequest request) {
        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + request.getSessionId()));

        if (request.getRecords() != null) {
            for (ManualAttendanceRequest.StudentStatusItem item : request.getRecords()) {
                Student student = studentRepository.findById(item.getStudentId()).orElse(null);
                if (student != null) {
                    Optional<AttendanceRecord> existing = recordRepository
                            .findByAttendanceSessionIdAndStudentId(session.getId(), student.getId());

                    if (existing.isPresent()) {
                        AttendanceRecord record = existing.get();
                        record.setStatus(item.getStatus());
                        record.setVerificationMethod(AttendanceMethod.MANUAL);
                        record.setRemarks(item.getRemarks() != null ? item.getRemarks() : "Manual override by faculty");
                        recordRepository.save(record);
                    } else {
                        AttendanceRecord record = new AttendanceRecord(
                                session,
                                student,
                                item.getStatus(),
                                AttendanceMethod.MANUAL,
                                item.getRemarks() != null ? item.getRemarks() : "Manually marked by faculty"
                        );
                        recordRepository.save(record);
                    }
                }
            }
        }
    }

    /**
     * Teacher closes the session: unverified students are automatically logged as ABSENT
     */
    @Transactional
    public AttendanceSessionResponse closeSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        AttendanceSession saved = sessionRepository.save(session);

        // Get all eligible students
        List<Student> eligibleStudents;
        if ("ALL".equalsIgnoreCase(session.getTargetBatch())) {
            eligibleStudents = studentRepository.findByCollegeClassId(session.getCollegeClass().getId());
        } else {
            eligibleStudents = studentRepository.findByCollegeClassIdAndLabBatch(
                    session.getCollegeClass().getId(), session.getTargetBatch());
        }

        // For any student who didn't scan QR, mark as ABSENT
        for (Student student : eligibleStudents) {
            if (!recordRepository.existsByAttendanceSessionIdAndStudentId(session.getId(), student.getId())) {
                AttendanceRecord absentRecord = new AttendanceRecord(
                        session,
                        student,
                        AttendanceStatus.ABSENT,
                        AttendanceMethod.MANUAL,
                        "Unverified at session close"
                );
                recordRepository.save(absentRecord);
            }
        }

        return mapToResponse(saved);
    }

    /**
     * Teacher views live attendance details of an ongoing or completed session
     */
    public AttendanceSessionResponse getLiveSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));
        return mapToResponse(session);
    }

    /**
     * Teacher cancels the session: marks it CANCELLED, wipes any scanned records
     */
    @Transactional
    public AttendanceSessionResponse cancelSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        session.setStatus(SessionStatus.CANCELLED);
        session.setEndTime(LocalDateTime.now());
        recordRepository.deleteAll(recordRepository.findByAttendanceSessionId(sessionId));
        AttendanceSession saved = sessionRepository.save(session);
        return mapToResponse(saved);
    }

    /**
     * Get live class roster for a session with each student's current status (PRESENT / ABSENT)
     */
    public List<SessionRosterItem> getSessionRoster(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        List<Student> eligibleStudents;
        if ("ALL".equalsIgnoreCase(session.getTargetBatch())) {
            eligibleStudents = studentRepository.findByCollegeClassId(session.getCollegeClass().getId());
        } else {
            eligibleStudents = studentRepository.findByCollegeClassIdAndLabBatch(
                    session.getCollegeClass().getId(), session.getTargetBatch());
        }

        List<AttendanceRecord> records = recordRepository.findByAttendanceSessionId(sessionId);
        Map<Long, AttendanceRecord> recordMap = new HashMap<>();
        for (AttendanceRecord r : records) {
            recordMap.put(r.getStudent().getId(), r);
        }

        List<SessionRosterItem> roster = new ArrayList<>();
        for (Student s : eligibleStudents) {
            AttendanceRecord rec = recordMap.get(s.getId());
            AttendanceStatus status = (rec != null) ? rec.getStatus() : AttendanceStatus.ABSENT;
            AttendanceMethod method = (rec != null) ? rec.getVerificationMethod() : null;
            LocalDateTime markedAt = (rec != null) ? rec.getMarkedAt() : null;

            roster.add(new SessionRosterItem(
                    s.getId(),
                    s.getRegisterNumber(),
                    s.getRollNumber(),
                    s.getFullName(),
                    s.getLabBatch(),
                    status,
                    method,
                    markedAt
            ));
        }

        roster.sort(Comparator.comparing(SessionRosterItem::getRegisterNumber));
        return roster;
    }

    private AttendanceSessionResponse mapToResponse(AttendanceSession s) {
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

        List<AttendanceRecord> records = recordRepository.findByAttendanceSessionId(s.getId());
        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        res.setPresentCount(present);

        int eligible = "ALL".equalsIgnoreCase(s.getTargetBatch())
                ? studentRepository.findByCollegeClassId(s.getCollegeClass().getId()).size()
                : studentRepository.findByCollegeClassIdAndLabBatch(s.getCollegeClass().getId(), s.getTargetBatch()).size();
        res.setTotalEligibleCount(eligible);

        return res;
    }
}

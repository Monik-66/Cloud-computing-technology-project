package com.attendance.backend.repository;

import com.attendance.backend.entity.AttendanceSession;
import com.attendance.backend.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findBySessionToken(String sessionToken);
    List<AttendanceSession> findByFacultyIdAndStatus(Long facultyId, SessionStatus status);
    List<AttendanceSession> findByCollegeClassIdAndSessionDate(Long classId, LocalDate sessionDate);
    List<AttendanceSession> findBySubjectIdAndCollegeClassId(Long subjectId, Long classId);
    List<AttendanceSession> findByFacultyIdOrderByStartTimeDesc(Long facultyId);
}

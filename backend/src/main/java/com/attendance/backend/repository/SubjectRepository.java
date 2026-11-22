package com.attendance.backend.repository;

import com.attendance.backend.entity.CourseType;
import com.attendance.backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCourseCode(String courseCode);
    Optional<Subject> findByShortCode(String shortCode);
    List<Subject> findByDepartmentIdAndSemester(Long departmentId, Integer semester);
    List<Subject> findByCourseType(CourseType courseType);
}

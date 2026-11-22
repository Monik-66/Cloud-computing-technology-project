package com.attendance.backend.repository;

import com.attendance.backend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByRegisterNumber(String registerNumber);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByCollegeClassId(Long classId);
    List<Student> findByCollegeClassIdAndLabBatch(Long classId, String labBatch);
    List<Student> findByDepartmentId(Long departmentId);
}

package com.attendance.backend.repository;

import com.attendance.backend.entity.CollegeClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeClassRepository extends JpaRepository<CollegeClass, Long> {
    Optional<CollegeClass> findBySection(String section);
    List<CollegeClass> findByDepartmentId(Long departmentId);
    List<CollegeClass> findByDepartmentIdAndSemester(Long departmentId, Integer semester);
}

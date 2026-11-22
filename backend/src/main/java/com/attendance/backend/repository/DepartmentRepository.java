package com.attendance.backend.repository;

import com.attendance.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository: The Data Access Layer.
 *
 * Extending JpaRepository<Department, Long> gives us standard database operations
 * out of the box with ZERO SQL required:
 * - save(department)
 * - findById(id)
 * - findAll()
 * - deleteById(id)
 *
 * Spring Data JPA also creates queries automatically based on method names:
 * e.g., 'findByDepartmentCode' automatically generates:
 * "SELECT * FROM departments WHERE department_code = ?"
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Automatically queries: SELECT * FROM departments WHERE department_code = ?
    Optional<Department> findByDepartmentCode(String departmentCode);

    // Automatically queries: SELECT COUNT(*) > 0 FROM departments WHERE department_code = ?
    boolean existsByDepartmentCode(String departmentCode);
}

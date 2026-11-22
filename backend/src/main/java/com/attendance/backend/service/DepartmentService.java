package com.attendance.backend.service;

import com.attendance.backend.dto.DepartmentRequest;
import com.attendance.backend.dto.DepartmentResponse;
import com.attendance.backend.entity.Department;
import com.attendance.backend.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service: The Business Logic Layer.
 *
 * It acts as the bridge between Controller and Repository.
 * All validation, business rules, and transformations take place here.
 */
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // Standard constructor injection - Spring Boot automatically passes the repository instance
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    /**
     * Fetch all departments from the database
     */
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch a single department by its ID
     */
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return mapToResponse(department);
    }

    /**
     * Create and save a new department into PostgreSQL
     */
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        // Business rule: Department code must be unique
        if (departmentRepository.existsByDepartmentCode(request.getDepartmentCode())) {
            throw new RuntimeException("Department with code '" + request.getDepartmentCode() + "' already exists");
        }

        Department department = new Department();
        department.setDepartmentName(request.getDepartmentName());
        department.setDepartmentCode(request.getDepartmentCode().toUpperCase());

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    /**
     * Delete a department by ID
     */
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    // Helper method to convert Entity -> DTO
    private DepartmentResponse mapToResponse(Department dept) {
        return new DepartmentResponse(
                dept.getId(),
                dept.getDepartmentName(),
                dept.getDepartmentCode(),
                dept.getCreatedAt()
        );
    }
}

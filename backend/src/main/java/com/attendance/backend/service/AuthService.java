package com.attendance.backend.service;

import com.attendance.backend.dto.AuthRequest;
import com.attendance.backend.dto.AuthResponse;
import com.attendance.backend.dto.RegisterRequest;
import com.attendance.backend.entity.*;
import com.attendance.backend.repository.*;
import com.attendance.backend.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final CollegeClassRepository collegeClassRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       FacultyRepository facultyRepository,
                       DepartmentRepository departmentRepository,
                       CollegeClassRepository collegeClassRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.collegeClassRepository = collegeClassRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        // 1. Create User
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        User savedUser = userRepository.save(user);

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElse(null);
        }

        Long profileId = null;
        String className = null;
        String labBatch = null;

        // 2. Create Student / Faculty profile based on role
        if (request.getRole() == Role.STUDENT) {
            CollegeClass collegeClass = null;
            if (request.getClassId() != null) {
                collegeClass = collegeClassRepository.findById(request.getClassId())
                        .orElse(null);
                if (collegeClass != null) {
                    className = collegeClass.getSection();
                }
            }
            labBatch = request.getLabBatch() != null ? request.getLabBatch() : "B1";

            Student student = new Student(
                    savedUser,
                    request.getIdentifierCode() != null ? request.getIdentifierCode() : "REG-" + savedUser.getId(),
                    request.getRollNumber() != null ? request.getRollNumber() : String.valueOf(savedUser.getId()),
                    request.getFullName(),
                    request.getPhone(),
                    department,
                    collegeClass,
                    labBatch
            );
            Student savedStudent = studentRepository.save(student);
            profileId = savedStudent.getId();

        } else if (request.getRole() == Role.FACULTY || request.getRole() == Role.DIRECTOR) {
            Faculty faculty = new Faculty(
                    savedUser,
                    request.getIdentifierCode() != null ? request.getIdentifierCode() : "FAC-" + savedUser.getId(),
                    request.getFullName(),
                    request.getPhone(),
                    department,
                    request.getDesignation() != null ? request.getDesignation() : (request.getRole() == Role.DIRECTOR ? "Director" : "Assistant Professor")
            );
            Faculty savedFaculty = facultyRepository.save(faculty);
            profileId = savedFaculty.getId();
        }

        String token = jwtUtils.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return new AuthResponse(
                token,
                savedUser.getId(),
                profileId,
                savedUser.getEmail(),
                request.getFullName(),
                savedUser.getRole().name(),
                department != null ? department.getDepartmentName() : null,
                className,
                labBatch
        );
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User account is inactive or suspended");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

        Long profileId = null;
        String fullName = user.getEmail();
        String departmentName = null;
        String className = null;
        String labBatch = null;

        if (user.getRole() == Role.STUDENT) {
            Student student = studentRepository.findByUserId(user.getId()).orElse(null);
            if (student != null) {
                profileId = student.getId();
                fullName = student.getFullName();
                departmentName = student.getDepartment() != null ? student.getDepartment().getDepartmentName() : null;
                className = student.getCollegeClass() != null ? student.getCollegeClass().getSection() : null;
                labBatch = student.getLabBatch();
            }
        } else if (user.getRole() == Role.FACULTY || user.getRole() == Role.DIRECTOR) {
            Faculty faculty = facultyRepository.findByUserId(user.getId()).orElse(null);
            if (faculty != null) {
                profileId = faculty.getId();
                fullName = faculty.getFullName();
                departmentName = faculty.getDepartment() != null ? faculty.getDepartment().getDepartmentName() : null;
            }
        }

        return new AuthResponse(
                token,
                user.getId(),
                profileId,
                user.getEmail(),
                fullName,
                user.getRole().name(),
                departmentName,
                className,
                labBatch
        );
    }
}

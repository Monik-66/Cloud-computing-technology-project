-- ============================================================================
-- CLOUD-BASED STUDENT ATTENDANCE MANAGEMENT SYSTEM
-- Tailored for Engineering Timetable (Theory, Lab Batches B1/B2, Electives)
-- Roles: STUDENT, FACULTY, DIRECTOR
-- ============================================================================

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS attendance_records CASCADE;
DROP TABLE IF EXISTS attendance_sessions CASCADE;
DROP TABLE IF EXISTS timetable_slots CASCADE;
DROP TABLE IF EXISTS enrollments CASCADE;
DROP TABLE IF EXISTS subjects CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS classes CASCADE;
DROP TABLE IF EXISTS faculty CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ----------------------------------------------------------------------------
-- 1. USERS TABLE (Supports STUDENT, FACULTY, and DIRECTOR logins)
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'FACULTY', 'DIRECTOR')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 2. DEPARTMENTS TABLE (e.g., Dept of Computer Science and Engineering)
-- ----------------------------------------------------------------------------
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    department_code VARCHAR(20) UNIQUE NOT NULL, -- e.g., 'CSE'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 3. FACULTY TABLE (Teachers, Advisors, and HODs)
-- ----------------------------------------------------------------------------
CREATE TABLE faculty (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    faculty_code VARCHAR(50) UNIQUE NOT NULL,    -- e.g., 'FAC-CSE-001'
    full_name VARCHAR(100) NOT NULL,             -- e.g., 'HEMASHREE R', 'SANJANA BALA J'
    phone VARCHAR(20),
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    designation VARCHAR(60),                     -- 'Assistant Professor', 'Class Advisor', 'HOD'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 4. CLASSES TABLE (Section e.g., '5CSE21', Sem 5)
-- ----------------------------------------------------------------------------
CREATE TABLE classes (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    batch_year INT NOT NULL,                     -- e.g., 2024
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8), -- e.g., 5
    section VARCHAR(20) NOT NULL,                -- e.g., '5CSE21'
    class_advisor_id BIGINT REFERENCES faculty(id) ON DELETE SET NULL, -- e.g., HEMASHREE R
    default_room VARCHAR(30) DEFAULT 'A403',     -- Main classroom
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(department_id, batch_year, semester, section)
);

-- ----------------------------------------------------------------------------
-- 5. STUDENTS TABLE (Includes Lab Batch B1 / B2)
-- ----------------------------------------------------------------------------
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    register_number VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'ENG21CS0001'
    roll_number VARCHAR(50) UNIQUE NOT NULL,     -- e.g., '21'
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE RESTRICT, -- Links to 5CSE21
    lab_batch VARCHAR(10) NOT NULL DEFAULT 'B1' CHECK (lab_batch IN ('B1', 'B2')), -- Crucial for Lab split!
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 6. SUBJECTS TABLE (Course Code, Short Code, Type)
-- ----------------------------------------------------------------------------
CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    course_code VARCHAR(30) UNIQUE NOT NULL,     -- e.g., '24BECSE501', '24BECSE521'
    course_name VARCHAR(120) NOT NULL,           -- e.g., 'Cloud Computing Technology'
    short_code VARCHAR(20) NOT NULL,             -- e.g., 'CCT', 'BDT', 'ML', 'FLAT', 'CCTL'
    course_type VARCHAR(20) NOT NULL CHECK (course_type IN ('THEORY', 'LAB', 'ELECTIVE', 'VALUE_ADDED', 'MEETING')),
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    credits INT DEFAULT 3,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 7. ENROLLMENTS TABLE (Crucial for Electives PE-2 & PE-3)
-- ----------------------------------------------------------------------------
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, subject_id)
);

-- ----------------------------------------------------------------------------
-- 8. TIMETABLE_SLOTS TABLE (Your Weekly Recurring Schedule)
-- ----------------------------------------------------------------------------
CREATE TABLE timetable_slots (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    faculty_id BIGINT NOT NULL REFERENCES faculty(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL CHECK (day_of_week IN ('MON', 'TUE', 'WED', 'THUR', 'FRI')),
    period_number INT NOT NULL CHECK (period_number BETWEEN 1 AND 8),
    start_time TIME NOT NULL,                    -- '09:00:00'
    end_time TIME NOT NULL,                      -- '10:00:00'
    target_batch VARCHAR(10) NOT NULL DEFAULT 'ALL' CHECK (target_batch IN ('ALL', 'B1', 'B2')),
    room_number VARCHAR(30) NOT NULL             -- 'A403', 'A321', 'A407'
);

-- ----------------------------------------------------------------------------
-- 9. ATTENDANCE_SESSIONS TABLE (Each Active Class Session / QR Code)
-- ----------------------------------------------------------------------------
CREATE TABLE attendance_sessions (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    faculty_id BIGINT NOT NULL REFERENCES faculty(id) ON DELETE CASCADE,
    session_date DATE NOT NULL DEFAULT CURRENT_DATE,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    target_batch VARCHAR(10) NOT NULL DEFAULT 'ALL' CHECK (target_batch IN ('ALL', 'B1', 'B2')),
    session_token VARCHAR(100) UNIQUE,           -- For dynamic QR code rotation
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    attendance_method VARCHAR(20) DEFAULT 'QR_AND_MANUAL' CHECK (attendance_method IN ('MANUAL', 'QR_CODE', 'QR_AND_MANUAL')),
    room_number VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 10. ATTENDANCE_RECORDS TABLE (Individual Student Attendance Log)
-- ----------------------------------------------------------------------------
CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    verification_method VARCHAR(20) DEFAULT 'QR_CODE' CHECK (verification_method IN ('QR_CODE', 'MANUAL')),
    marked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    -- Strictly prevents marking duplicate attendance for the same student in the same session
    CONSTRAINT uq_session_student UNIQUE (session_id, student_id)
);

-- ----------------------------------------------------------------------------
-- 11. NOTIFICATIONS TABLE (Low attendance alerts, timetable change notices)
-- ----------------------------------------------------------------------------
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) DEFAULT 'GENERAL' CHECK (type IN ('GENERAL', 'LOW_ATTENDANCE', 'TIMETABLE_ALERT')),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 12. AUDIT_LOGS TABLE (Director/HOD oversight & tracking changes)
-- ----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,                 -- e.g., 'MANUAL_ATTENDANCE_OVERRIDE'
    entity_name VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- PERFORMANCE INDEXES (Fast Attendance Calculation & Real-Time Lookups)
-- ============================================================================
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_students_class_batch ON students(class_id, lab_batch);
CREATE INDEX idx_attendance_records_student ON attendance_records(student_id, status);
CREATE INDEX idx_attendance_sessions_class ON attendance_sessions(class_id, session_date);
CREATE INDEX idx_timetable_lookup ON timetable_slots(class_id, day_of_week, period_number);
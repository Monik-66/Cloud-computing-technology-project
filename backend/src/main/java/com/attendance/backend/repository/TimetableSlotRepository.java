package com.attendance.backend.repository;

import com.attendance.backend.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findByCollegeClassIdOrderByDayOfWeekAscPeriodNumberAsc(Long classId);
    List<TimetableSlot> findByCollegeClassIdAndDayOfWeekOrderByPeriodNumberAsc(Long classId, String dayOfWeek);
    List<TimetableSlot> findByFacultyIdOrderByDayOfWeekAscPeriodNumberAsc(Long facultyId);
    List<TimetableSlot> findByFacultyIdAndDayOfWeekOrderByPeriodNumberAsc(Long facultyId, String dayOfWeek);
}

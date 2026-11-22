package com.attendance.backend.dto;

import java.util.List;

public class StudentAttendanceStatsResponse {

    private Long studentId;
    private String fullName;
    private String rollNumber;
    private double overallPercentage;
    private long totalSessions;
    private long attendedSessions;
    private List<SubjectAttendanceItem> subjectBreakdown;

    public StudentAttendanceStatsResponse() {
    }

    public StudentAttendanceStatsResponse(Long studentId, String fullName, String rollNumber, double overallPercentage,
                                          long totalSessions, long attendedSessions, List<SubjectAttendanceItem> subjectBreakdown) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.rollNumber = rollNumber;
        this.overallPercentage = overallPercentage;
        this.totalSessions = totalSessions;
        this.attendedSessions = attendedSessions;
        this.subjectBreakdown = subjectBreakdown;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public double getOverallPercentage() {
        return overallPercentage;
    }

    public void setOverallPercentage(double overallPercentage) {
        this.overallPercentage = overallPercentage;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getAttendedSessions() {
        return attendedSessions;
    }

    public void setAttendedSessions(long attendedSessions) {
        this.attendedSessions = attendedSessions;
    }

    public List<SubjectAttendanceItem> getSubjectBreakdown() {
        return subjectBreakdown;
    }

    public void setSubjectBreakdown(List<SubjectAttendanceItem> subjectBreakdown) {
        this.subjectBreakdown = subjectBreakdown;
    }

    public static class SubjectAttendanceItem {
        private Long subjectId;
        private String courseCode;
        private String courseName;
        private String shortCode;
        private long totalClasses;
        private long attendedClasses;
        private double percentage;

        public SubjectAttendanceItem() {
        }

        public SubjectAttendanceItem(Long subjectId, String courseCode, String courseName, String shortCode,
                                     long totalClasses, long attendedClasses, double percentage) {
            this.subjectId = subjectId;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.shortCode = shortCode;
            this.totalClasses = totalClasses;
            this.attendedClasses = attendedClasses;
            this.percentage = percentage;
        }

        public Long getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Long subjectId) {
            this.subjectId = subjectId;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getShortCode() {
            return shortCode;
        }

        public void setShortCode(String shortCode) {
            this.shortCode = shortCode;
        }

        public long getTotalClasses() {
            return totalClasses;
        }

        public void setTotalClasses(long totalClasses) {
            this.totalClasses = totalClasses;
        }

        public long getAttendedClasses() {
            return attendedClasses;
        }

        public void setAttendedClasses(long attendedClasses) {
            this.attendedClasses = attendedClasses;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }
}

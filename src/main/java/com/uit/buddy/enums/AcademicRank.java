package com.uit.buddy.enums;

import lombok.Getter;

public enum AcademicRank {
    EXCELLENT(9.0), // ≥ 9
    GOOD(8.0), // ≥ 8
    FINE(6.5), // ≥ 6.5
    AVERAGE(5.0), // ≥ 5
    POOR(0.0); // < 5

    private final Double minGpa;

    AcademicRank(Double minGpa) {
        this.minGpa = minGpa;
    }

    public Double getMinGpa() {
        return minGpa;
    }

    public static AcademicRank fromGpa(Float gpa) {
        if (gpa == null) {
            return POOR;
        }

        double gpaValue = gpa.doubleValue();
        if (gpaValue >= 9.0) {
            return EXCELLENT;
        } else if (gpaValue >= 8.0) {
            return GOOD;
        } else if (gpaValue >= 6.5) {
            return FINE;
        } else if (gpaValue >= 5.0) {
            return AVERAGE;
        } else {
            return POOR;
        }
    }
}

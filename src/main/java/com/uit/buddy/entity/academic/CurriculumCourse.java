package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "curriculum_courses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_curr_course_cat", columnNames = { "curriculum_code", "course_code",
                "category_code" })
}, indexes = {
        @Index(name = "idx_curr_course_lookup", columnList = "curriculum_code, course_code"),
        @Index(name = "idx_curr_category", columnList = "category_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurriculumCourse extends AbstractBaseEntity {

    @Column(name = "curriculum_code", length = 50, insertable = false, updatable = false)
    private String curriculumCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curriculum_code", referencedColumnName = "curriculum_code", foreignKey = @ForeignKey(name = "fk_curr_course_curriculum"))
    private Curriculum curriculum;

    @Column(name = "course_code", length = 20, insertable = false, updatable = false)
    private String courseCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", foreignKey = @ForeignKey(name = "fk_curr_course_course"))
    private Course course;

    @Column(name = "category_code", length = 20, insertable = false, updatable = false)
    private String categoryCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_code", referencedColumnName = "category_code", foreignKey = @ForeignKey(name = "fk_curr_course_category"))
    private CourseCategory category;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    @Column(name = "prerequisite_course_code", length = 20, insertable = false, updatable = false)
    private String prerequisiteCourseCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_course_code", referencedColumnName = "course_code", foreignKey = @ForeignKey(name = "fk_curr_course_prerequisite"))
    private Course prerequisiteCourse;
}
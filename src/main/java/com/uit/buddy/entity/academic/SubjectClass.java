package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity;
import jakarta.persistence.*;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(name = "classes", indexes = { @Index(name = "idx_class_code", columnList = "class_code", unique = true),
        @Index(name = "idx_class_semester", columnList = "semester_code"),
        @Index(name = "idx_class_course", columnList = "course_code") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectClass extends AbstractAuditEntity {

    @Id
    @Column(name = "class_code", length = 30, unique = true, nullable = false)
    private String classCode;

    @Column(name = "course_code", length = 20, insertable = false, updatable = false)
    private String courseCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", foreignKey = @ForeignKey(name = "fk_class_course"))
    private Course course; //

    @Column(name = "semester_code", length = 20, insertable = false, updatable = false)
    private String semesterCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_code", referencedColumnName = "semester_code", foreignKey = @ForeignKey(name = "fk_class_semester"))
    private Semester semester;

    @Column(name = "teacher_name", length = 150)
    private String teacherName;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "start_lesson")
    private Integer startLesson;

    @Column(name = "end_lesson")
    private Integer endLesson;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "room_code", length = 20)
    private String roomCode;
}

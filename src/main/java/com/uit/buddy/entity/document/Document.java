package com.uit.buddy.entity.document;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.AccessLevel;
import com.uit.buddy.enums.DocumentPriority;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documents", indexes = { @Index(name = "idx_doc_owner", columnList = "mssv"),
        @Index(name = "idx_doc_class", columnList = "class_code") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_document_owner"))
    private Student owner;

    @Column(name = "class_code", length = 30, insertable = false, updatable = false)
    private String classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_code", referencedColumnName = "class_code")
    private SubjectClass subjectClass;

    @Column(name = "file_url", nullable = false, length = 512)
    private String fileUrl;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", length = 50)
    private AccessLevel accessLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private DocumentPriority priority; //
}

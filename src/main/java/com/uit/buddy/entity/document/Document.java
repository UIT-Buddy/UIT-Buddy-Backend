package com.uit.buddy.entity.document;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FileType;
import jakarta.persistence.*;
import java.util.UUID;
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

    @Column(name = "file_url", nullable = false, length = 512)
    private String fileUrl;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "folder_id", insertable = false, updatable = false)
    private UUID folderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_document_folder"))
    private Folder folder;

    @Column(name = "file_size")
    private Float fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 20)
    private FileType fileType;
}

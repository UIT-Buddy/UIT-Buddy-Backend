package com.uit.buddy.entity.document;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.AccessRole;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "share_document", uniqueConstraints = {
        @UniqueConstraint(name = "uk_document_recipient", columnNames = { "document_id", "mssv" }) }, indexes = {
                @Index(name = "idx_share_doc_id", columnList = "document_id"),
                @Index(name = "idx_share_recipient", columnList = "mssv") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareDocument extends AbstractBaseEntity {

    @Column(name = "document_id", length = 50, insertable = false, updatable = false)
    private UUID documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_share_document"))
    private Document document;

    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_share_recipient"))
    private Student recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_role", length = 20, nullable = false)
    private AccessRole accessRole;
}

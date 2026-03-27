package com.uit.buddy.entity.document;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.AccessRole;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "share_folder", uniqueConstraints = {
        @UniqueConstraint(name = "uk_folder_recipient", columnNames = { "folder_id", "mssv" }) }, indexes = {
                @Index(name = "idx_share_folder_id", columnList = "folder_id"),
                @Index(name = "idx_share_recipient", columnList = "mssv") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareFolder extends AbstractBaseEntity {
    @Column(name = "folder_id", length = 50, insertable = false, updatable = false)
    private UUID folderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_share_folder"))
    private Folder folder;

    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_share_recipient"))
    private Student recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_role", length = 20, nullable = false)
    private AccessRole accessRole;
}

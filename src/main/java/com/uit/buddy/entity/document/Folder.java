package com.uit.buddy.entity.document;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "folders", indexes = { @Index(name = "idx_doc_owner", columnList = "mssv") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder extends AbstractBaseEntity {
    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_folder_owner"))
    private Student owner;

    @Column(name = "folder_name", nullable = false, length = 255)
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> children = new ArrayList<>();

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> files = new ArrayList<>();

}

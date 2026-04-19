package com.uit.buddy.entity.note;

import com.uit.buddy.entity.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "note_nodes", indexes = { @Index(name = "idx_note_nodes_mssv", columnList = "mssv"),
        @Index(name = "idx_note_nodes_parent", columnList = "parent_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteNode extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, nullable = false)
    private String mssv;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "name", length = 120, nullable = false)
    private String name;
}

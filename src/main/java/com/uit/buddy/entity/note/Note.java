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
@Table(name = "notes", indexes = {
        @Index(name = "idx_notes_mssv", columnList = "mssv"),
        @Index(name = "idx_notes_node", columnList = "node_id"),
        @Index(name = "idx_notes_updated_at", columnList = "updated_at") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, nullable = false)
    private String mssv;

    @Column(name = "node_id")
    private UUID nodeId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}

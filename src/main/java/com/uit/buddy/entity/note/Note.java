package com.uit.buddy.entity.note;

import com.uit.buddy.entity.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notes", indexes = { @Index(name = "idx_notes_mssv", columnList = "mssv", unique = true) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, nullable = false, unique = true)
    private String mssv;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}

-- Add collaborative editing fields to documents table
ALTER TABLE documents
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL,
ADD COLUMN content TEXT,
ADD COLUMN last_edited_by VARCHAR(12),
ADD COLUMN last_edited_at TIMESTAMP;

-- Add foreign key constraint for last_edited_by (matching entity @ForeignKey name)
ALTER TABLE documents
ADD CONSTRAINT fk_document_last_editor FOREIGN KEY (last_edited_by) REFERENCES students(mssv) ON DELETE SET NULL;

-- Add indexes for share_document table
CREATE INDEX idx_share_document_mssv ON share_document(mssv) WHERE deleted_at IS NULL;
CREATE INDEX idx_share_document_document_id ON share_document(document_id) WHERE deleted_at IS NULL;

-- Add foreign key constraints for share_document
ALTER TABLE share_document
ADD CONSTRAINT fk_share_document_document_id FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE;

ALTER TABLE share_document
ADD CONSTRAINT fk_share_document_mssv FOREIGN KEY (mssv) REFERENCES students(mssv) ON DELETE CASCADE;

ALTER TABLE documents
ADD COLUMN folder_id UUID,
ADD COLUMN file_size REAL,
ADD COLUMN file_type VARCHAR(20);

ALTER TABLE documents
DROP COLUMN access_level;

ALTER TABLE documents
DROP COLUMN priority;

ALTER TABLE documents
DROP COLUMN class_id

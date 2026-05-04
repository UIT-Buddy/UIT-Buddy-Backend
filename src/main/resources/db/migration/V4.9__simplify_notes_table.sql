-- Simplify notes table: remove node_id, title columns and make mssv unique
-- Drop old indexes
DROP INDEX IF EXISTS idx_notes_node;
DROP INDEX IF EXISTS idx_notes_updated_at;

-- Drop node_id and title columns
ALTER TABLE notes DROP COLUMN IF EXISTS node_id;
ALTER TABLE notes DROP COLUMN IF EXISTS title;

-- Add unique constraint on mssv (one note per user)
ALTER TABLE notes ADD CONSTRAINT notes_mssv_unique UNIQUE (mssv);

-- Drop note_nodes table (no longer needed)
DROP TABLE IF EXISTS note_nodes CASCADE;

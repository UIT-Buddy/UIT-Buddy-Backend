-- Add version column to comments table for optimistic locking
ALTER TABLE comments 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
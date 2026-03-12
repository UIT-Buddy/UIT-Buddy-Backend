-- Add version column for optimistic locking on posts
ALTER TABLE posts 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

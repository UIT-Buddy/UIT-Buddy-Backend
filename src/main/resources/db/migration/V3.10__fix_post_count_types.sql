-- Fix count column types from INT to BIGINT for better scalability
ALTER TABLE posts 
ALTER COLUMN comment_count TYPE BIGINT,
ALTER COLUMN like_count TYPE BIGINT,
ALTER COLUMN share_count TYPE BIGINT;

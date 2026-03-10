/*insert index for cursor pagination */
CREATE UNIQUE INDEX idx_post_created_id ON posts(created_at, id);

-- src/main/resources/db/migration/V3.3__add_version_to_posts.sql

ALTER TABLE posts ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Set version = 0 cho tất cả records hiện tại
UPDATE posts SET version = 0 WHERE version IS NULL;

-- Đặt NOT NULL sau khi đã có data
ALTER TABLE posts ALTER COLUMN version SET NOT NULL;


ALTER TABLE comments 
ADD COLUMN parent_comment_id UUID,
ADD CONSTRAINT fk_comment_parent 
    FOREIGN KEY (parent_comment_id) 
    REFERENCES comments(id) ON DELETE CASCADE;

CREATE INDEX idx_comment_parent ON comments(parent_comment_id);

ALTER TABLE reactions 
ADD COLUMN reaction_type VARCHAR(20) NOT NULL DEFAULT 'LIKE';

CREATE INDEX idx_reaction_type ON reactions(post_id, reaction_type);

ALTER TABLE posts DROP COLUMN image_url;
ALTER TABLE posts DROP COLUMN video_url;
ALTER TABLE posts ADD COLUMN medias jsonb NOT NULL DEFAULT '[]';

ALTER TABLE posts
ADD COLUMN comment_count INT NOT NULL DEFAULT 0,
ADD COLUMN like_count INT NOT NULL DEFAULT 0,
ADD COLUMN share_count INT NOT NULL DEFAULT 0;
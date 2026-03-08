ALTER TABLE comments 
ADD COLUMN parent_comment_id UUID,
ADD CONSTRAINT fk_comment_parent 
    FOREIGN KEY (parent_comment_id) 
    REFERENCES comments(id) ON DELETE CASCADE;

CREATE INDEX idx_comment_parent ON comments(parent_comment_id);

ALTER TABLE reactions 
ADD COLUMN reaction_type VARCHAR(20) NOT NULL DEFAULT 'LIKE';

CREATE INDEX idx_reaction_type ON reactions(post_id, reaction_type);

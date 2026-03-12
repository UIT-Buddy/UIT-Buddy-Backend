-- Add fields for post sharing feature
ALTER TABLE posts 
ADD COLUMN original_post_id UUID,
ADD COLUMN type VARCHAR(20);

-- Add foreign key constraint for original post
ALTER TABLE posts
ADD CONSTRAINT fk_post_original 
    FOREIGN KEY (original_post_id) 
    REFERENCES posts(id) ON DELETE CASCADE;

-- Add indexes for better query performance
CREATE INDEX idx_post_original ON posts(original_post_id);
CREATE INDEX idx_post_type ON posts(type);

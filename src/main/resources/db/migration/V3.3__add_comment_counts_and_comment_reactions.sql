-- Add missing columns to comments table
ALTER TABLE comments 
ADD COLUMN IF NOT EXISTS like_count BIGINT NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS reply_count BIGINT NOT NULL DEFAULT 0;

-- Create comment_reactions table for likes on comments
CREATE TABLE IF NOT EXISTS comment_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comment_id UUID NOT NULL,
    mssv VARCHAR(12) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comment_reaction_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_reaction_student FOREIGN KEY (mssv) REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT uk_comment_reaction_user UNIQUE (comment_id, mssv)
);

-- Create indexes for comment_reactions
CREATE INDEX IF NOT EXISTS idx_comment_reaction_comment ON comment_reactions(comment_id);
CREATE INDEX IF NOT EXISTS idx_comment_reaction_student ON comment_reactions(mssv);
CREATE INDEX IF NOT EXISTS idx_comment_reaction_created ON comment_reactions(created_at DESC, id DESC);
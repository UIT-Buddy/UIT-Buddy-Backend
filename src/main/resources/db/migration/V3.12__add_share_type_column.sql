-- Add share_type column to shares table
ALTER TABLE shares ADD COLUMN share_type VARCHAR(50) NOT NULL DEFAULT 'PROFILE';

-- Add index for share_type
CREATE INDEX idx_shares_type ON shares(share_type);
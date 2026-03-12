-- Remove unique constraint to allow multiple shares from same user
-- This allows users to share the same post multiple times
DROP INDEX IF EXISTS idx_shares_unique;

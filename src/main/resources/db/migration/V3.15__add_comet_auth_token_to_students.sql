-- Add comet_auth_token column to students table
ALTER TABLE students ADD COLUMN comet_auth_token VARCHAR(512);

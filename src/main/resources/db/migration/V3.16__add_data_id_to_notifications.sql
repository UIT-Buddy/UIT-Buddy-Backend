-- Add data_id column to notifications table
ALTER TABLE notifications ADD COLUMN data_id VARCHAR(100);

-- Add index for faster lookups by data_id
CREATE INDEX idx_notification_data_id ON notifications(data_id);

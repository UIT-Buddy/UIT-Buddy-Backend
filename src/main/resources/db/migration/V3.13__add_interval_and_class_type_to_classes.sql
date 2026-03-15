-- Add interval and class_type columns to classes table
ALTER TABLE classes 
ADD COLUMN interval_weeks INTEGER DEFAULT 1,
ADD COLUMN class_type VARCHAR(20) DEFAULT 'WEEKLY';

-- Add index for better query performance
CREATE INDEX idx_classes_type_interval ON classes(class_type, interval_weeks);
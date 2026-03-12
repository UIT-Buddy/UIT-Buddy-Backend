-- Create user_settings table
CREATE TABLE user_settings (
    mssv VARCHAR(12) PRIMARY KEY,
    enable_notification BOOLEAN NOT NULL DEFAULT true,
    enable_schedule_reminder BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_setting_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Create index
CREATE INDEX idx_user_settings_mssv ON user_settings(mssv);

-- Add trigger to update updated_at column
CREATE OR REPLACE FUNCTION update_user_settings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_settings_updated_at
    BEFORE UPDATE ON user_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_user_settings_updated_at();

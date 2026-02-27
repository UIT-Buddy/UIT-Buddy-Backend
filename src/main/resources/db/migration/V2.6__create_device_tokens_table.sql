CREATE TABLE device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    fcm_token TEXT NOT NULL, 
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_device_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT uk_mssv_token UNIQUE (mssv, fcm_token) 
);

CREATE INDEX idx_device_mssv ON device_tokens(mssv);
CREATE INDEX idx_fcm_token ON device_tokens(fcm_token);

-- Create grades table without class_id (grades are independent from classes)
CREATE TABLE grades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    semester_code VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(255),
    credits INTEGER,
    process_grade FLOAT,
    midterm_grade FLOAT,
    final_grade FLOAT,
    lab_grade FLOAT,
    total_grade FLOAT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    CONSTRAINT uk_grade_student_course_semester UNIQUE (mssv, course_code, semester_code),
    CONSTRAINT fk_grade_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT fk_grade_semester FOREIGN KEY (semester_code) 
        REFERENCES semesters(semester_code) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_grade_mssv ON grades(mssv);
CREATE INDEX idx_grade_semester ON grades(semester_code);
CREATE INDEX idx_grade_mssv_semester ON grades(mssv, semester_code);
CREATE INDEX idx_grade_course ON grades(course_code);

-- Remove grade columns from student_class (if they exist)
ALTER TABLE student_class 
DROP COLUMN IF EXISTS process_grade,
DROP COLUMN IF EXISTS midterm_grade,
DROP COLUMN IF EXISTS final_grade,
DROP COLUMN IF EXISTS lab_grade,
DROP COLUMN IF EXISTS total_grade;

-- Create personal notes tables
CREATE TABLE note_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    parent_id UUID,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_note_node_student FOREIGN KEY (mssv)
        REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT fk_note_node_parent FOREIGN KEY (parent_id)
        REFERENCES note_nodes(id) ON DELETE CASCADE,
    CONSTRAINT uk_note_node_name_per_parent UNIQUE (mssv, parent_id, name)
);

CREATE INDEX idx_note_nodes_mssv ON note_nodes(mssv);
CREATE INDEX idx_note_nodes_parent ON note_nodes(parent_id);

CREATE TABLE notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    node_id UUID,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_note_student FOREIGN KEY (mssv)
        REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT fk_note_node FOREIGN KEY (node_id)
        REFERENCES note_nodes(id) ON DELETE CASCADE
);

CREATE INDEX idx_notes_mssv ON notes(mssv);
CREATE INDEX idx_notes_node ON notes(node_id);
CREATE INDEX idx_notes_updated_at ON notes(updated_at DESC);

-- Add cover image URL for student profile
ALTER TABLE students
ADD COLUMN IF NOT EXISTS cover_url VARCHAR(255);

-- Store grade columns as exact one-decimal numbers instead of floating-point
-- to avoid values like 7.1999998093 in database tools.
ALTER TABLE grades
    ALTER COLUMN process_grade TYPE NUMERIC(4,1) USING ROUND(process_grade::numeric, 1),
    ALTER COLUMN midterm_grade TYPE NUMERIC(4,1) USING ROUND(midterm_grade::numeric, 1),
    ALTER COLUMN final_grade TYPE NUMERIC(4,1) USING ROUND(final_grade::numeric, 1),
    ALTER COLUMN lab_grade TYPE NUMERIC(4,1) USING ROUND(lab_grade::numeric, 1),
    ALTER COLUMN total_grade TYPE NUMERIC(4,1) USING ROUND(total_grade::numeric, 1);

-- Add course type metadata for grade rows
ALTER TABLE grades
    ADD COLUMN IF NOT EXISTS course_type VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_grade_course_type ON grades(course_type);

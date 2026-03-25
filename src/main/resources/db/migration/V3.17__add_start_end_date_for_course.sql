ALTER TABLE classes
ADD COLUMN start_date DATE,
ADD COLUMN end_date DATE;

ALTER TABLE student_class
DROP COLUMN IF EXISTS process_grade,
DROP COLUMN IF EXISTS midterm_grade,
DROP COLUMN IF EXISTS final_grade,
DROP COLUMN IF EXISTS lab_grade,
DROP COLUMN IF EXISTS total_grade;
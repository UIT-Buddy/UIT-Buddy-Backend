ALTER TABLE student_tasks DROP CONSTRAINT IF EXISTS fk_task_assignment;
ALTER TABLE student_tasks DROP CONSTRAINT IF EXISTS fk_task_student;

DROP TABLE IF EXISTS assignments CASCADE;

ALTER TABLE student_tasks
DROP COLUMN IF EXISTS priority,
DROP COLUMN IF EXISTS assignment_id,
DROP COLUMN IF EXISTS grade;

ALTER TABLE student_tasks
ADD COLUMN class_code VARCHAR(50);
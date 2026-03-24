CREATE TABLE faculties (
    faculty_code VARCHAR(20) PRIMARY KEY,
    faculty_name VARCHAR(150) NOT NULL,
    office_location VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE majors (
    major_code VARCHAR(20) PRIMARY KEY,
    major_name VARCHAR(150) NOT NULL,
    major_number_code VARCHAR(50) NOT NULL,
    faculty_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE home_classes (
    home_class_code VARCHAR(20) PRIMARY KEY,
    academic_year VARCHAR(10) NOT NULL,
    advisor_name VARCHAR(150),
    major_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE students (
    mssv VARCHAR(12) PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    avatar_url VARCHAR(255),
    bio TEXT,
    comet_uid VARCHAR(100) NOT NULL,
    home_class_code VARCHAR(20) NOT NULL,
    encrypted_wstoken VARCHAR(512),
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE course_categories (
    category_code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    course_code VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(150) NOT NULL,
    faculty_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE curriculums (
    curriculum_code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    major_code VARCHAR(20) NOT NULL,
    academic_start_year INTEGER,
    total_credits_required INTEGER,
    min_general_education_credits INTEGER,
    min_professional_education_credits INTEGER,
    min_graduation_credits INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE curriculum_courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    curriculum_code VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    category_code VARCHAR(20) NOT NULL,
    credits INTEGER,
    theory_credits INTEGER,
    lab_credits INTEGER,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    prerequisite_course_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE semesters (
    semester_code VARCHAR(20) PRIMARY KEY,
    year_start VARCHAR(10) NOT NULL,
    year_end VARCHAR(10),
    semester_number VARCHAR(20) NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_code VARCHAR(30),
    course_code VARCHAR(20) NOT NULL,
    semester_code VARCHAR(20) NOT NULL,
    teacher_name VARCHAR(150),
    day_of_week INTEGER,
    start_lesson INTEGER,
    end_lesson INTEGER,
    start_time TIME,
    end_time TIME,
    room_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student_class (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    class_id UUID NOT NULL,
    status VARCHAR(20),
    process_grade FLOAT,
    midterm_grade FLOAT,
    final_grade FLOAT,
    lab_grade FLOAT,
    total_grade FLOAT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE semester_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    semester_code VARCHAR(20) NOT NULL,
    term_gpa FLOAT,
    term_credits INTEGER,
    term_rank VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE academic_summary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    attempted_credits INTEGER,
    accumulated_credits INTEGER,
    attempted_gpa FLOAT,
    accumulated_gpa FLOAT,
    major_progress FLOAT,
    accumulated_general_credits INTEGER,
    accumulated_foundation_credits INTEGER,
    accumulated_major_credits INTEGER,
    accumulated_elective_credits INTEGER,
    accumulated_graduation_credits INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    due_date TIMESTAMP,
    open_date TIMESTAMP,
    external_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE student_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    assignment_id UUID,
    task_type VARCHAR(20),
    personal_title VARCHAR(255) NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority VARCHAR(20),
    reminder_at TIMESTAMP,
    grade FLOAT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    class_id UUID NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    access_level VARCHAR(50),
    priority VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE share_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL,
    mssv VARCHAR(12) NOT NULL,
    access_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    image_url VARCHAR(512),
    video_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL,
    mssv VARCHAR(12) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    post_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id VARCHAR(12) NOT NULL,
    name VARCHAR(150) NOT NULL,
    avatar_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE group_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    mssv VARCHAR(12) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID,
    mssv_sender VARCHAR(12) NOT NULL,
    mssv_receiver VARCHAR(12),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_faculty_code ON faculties(faculty_code);

CREATE INDEX idx_major_code ON majors(major_code);
CREATE INDEX idx_major_faculty ON majors(faculty_code);

CREATE INDEX idx_home_class_code ON home_classes(home_class_code);
CREATE INDEX idx_home_class_major ON home_classes(major_code);

CREATE UNIQUE INDEX idx_student_mssv ON students(mssv);
CREATE INDEX idx_student_home_class ON students(home_class_code);

CREATE INDEX idx_category_code ON course_categories(category_code);

CREATE INDEX idx_course_code ON courses(course_code);
CREATE INDEX idx_course_faculty ON courses(faculty_code);

CREATE INDEX idx_curriculum_major ON curriculums(major_code);
CREATE INDEX idx_curriculum_start_year ON curriculums(academic_start_year);

CREATE INDEX idx_curr_course_lookup ON curriculum_courses(curriculum_code, course_code);
CREATE INDEX idx_curr_category ON curriculum_courses(category_code);

CREATE INDEX idx_semester_code ON semesters(semester_code);

CREATE INDEX idx_class_code ON classes(class_code);
CREATE INDEX idx_class_semester ON classes(semester_code);
CREATE INDEX idx_class_course ON classes(course_code);

CREATE INDEX idx_st_class_mssv ON student_class(mssv);
CREATE INDEX idx_st_class_id ON student_class(class_id);

CREATE INDEX idx_semester_summary_student ON semester_summaries(mssv);
CREATE INDEX idx_semester_summary_semester ON semester_summaries(semester_code);

CREATE UNIQUE INDEX idx_academic_summary_mssv ON academic_summary(mssv);

CREATE INDEX idx_assignment_class ON assignments(class_id);

CREATE INDEX idx_task_student ON student_tasks(mssv);
CREATE INDEX idx_task_assignment ON student_tasks(assignment_id);

CREATE INDEX idx_doc_owner ON documents(mssv);
CREATE INDEX idx_doc_class ON documents(class_id);

CREATE INDEX idx_share_doc_id ON share_document(document_id);
CREATE INDEX idx_share_recipient ON share_document(mssv);

CREATE INDEX idx_notification_student ON notifications(mssv);
CREATE INDEX idx_notification_is_read ON notifications(is_read);

CREATE INDEX idx_post_author ON posts(mssv);

CREATE INDEX idx_comment_post ON comments(post_id);
CREATE INDEX idx_comment_author ON comments(mssv);

CREATE INDEX idx_reaction_post ON reactions(post_id);
CREATE INDEX idx_reaction_student ON reactions(mssv);
CREATE UNIQUE INDEX idx_reaction_unique ON reactions(mssv, post_id);

CREATE INDEX idx_group_member_student ON group_members(mssv);
CREATE UNIQUE INDEX idx_group_member_unique ON group_members(group_id, mssv);

CREATE INDEX idx_chat_group ON chats(group_id);
CREATE INDEX idx_chat_sender ON chats(mssv_sender);
CREATE INDEX idx_chat_receiver ON chats(mssv_receiver);

ALTER TABLE majors
    ADD CONSTRAINT fk_major_faculty FOREIGN KEY (faculty_code) 
    REFERENCES faculties(faculty_code) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE home_classes
    ADD CONSTRAINT fk_home_class_major FOREIGN KEY (major_code) 
    REFERENCES majors(major_code) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE students
    ADD CONSTRAINT fk_student_home_class FOREIGN KEY (home_class_code) 
    REFERENCES home_classes(home_class_code) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE courses
    ADD CONSTRAINT fk_course_faculty FOREIGN KEY (faculty_code) 
    REFERENCES faculties(faculty_code) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE curriculums
    ADD CONSTRAINT fk_curriculum_major FOREIGN KEY (major_code) 
    REFERENCES majors(major_code) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE curriculum_courses
    ADD CONSTRAINT uk_curr_course_cat UNIQUE (curriculum_code, course_code, category_code),
    ADD CONSTRAINT fk_curr_course_curriculum FOREIGN KEY (curriculum_code) 
        REFERENCES curriculums(curriculum_code) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_curr_course_course FOREIGN KEY (course_code) 
        REFERENCES courses(course_code) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_curr_course_category FOREIGN KEY (category_code) 
        REFERENCES course_categories(category_code) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_curr_course_prerequisite FOREIGN KEY (prerequisite_course_code) 
        REFERENCES courses(course_code) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE classes
    ADD CONSTRAINT uk_class_semester UNIQUE (class_code, semester_code),
    ADD CONSTRAINT fk_class_course FOREIGN KEY (course_code) 
        REFERENCES courses(course_code) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_class_semester FOREIGN KEY (semester_code) 
        REFERENCES semesters(semester_code) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE student_class
    ADD CONSTRAINT uk_student_class UNIQUE (mssv, class_id),
    ADD CONSTRAINT fk_st_class_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_st_class_subject FOREIGN KEY (class_id) 
        REFERENCES classes(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE semester_summaries
    ADD CONSTRAINT uk_student_semester_summary UNIQUE (mssv, semester_code),
    ADD CONSTRAINT fk_summary_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_summary_semester FOREIGN KEY (semester_code) 
        REFERENCES semesters(semester_code) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE academic_summary
    ADD CONSTRAINT fk_summary_student FOREIGN KEY (mssv) 
    REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE assignments
    ADD CONSTRAINT fk_assignment_class FOREIGN KEY (class_id) 
    REFERENCES classes(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE student_tasks
    ADD CONSTRAINT fk_task_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_task_assignment FOREIGN KEY (assignment_id) 
        REFERENCES assignments(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE documents
    ADD CONSTRAINT fk_document_owner FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_document_class FOREIGN KEY (class_id) 
        REFERENCES classes(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE share_document
    ADD CONSTRAINT uk_document_recipient UNIQUE (document_id, mssv),
    ADD CONSTRAINT fk_share_document FOREIGN KEY (document_id) 
        REFERENCES documents(id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_share_recipient FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notification_student FOREIGN KEY (mssv) 
    REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE posts
    ADD CONSTRAINT fk_post_author FOREIGN KEY (mssv) 
    REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE comments
    ADD CONSTRAINT fk_comment_post FOREIGN KEY (post_id) 
        REFERENCES posts(id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_comment_author FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE reactions
    ADD CONSTRAINT fk_reaction_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_reaction_post FOREIGN KEY (post_id) 
        REFERENCES posts(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE groups
    ADD CONSTRAINT fk_group_creator FOREIGN KEY (creator_id) 
    REFERENCES students(mssv) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE group_members
    ADD CONSTRAINT fk_member_group FOREIGN KEY (group_id) 
        REFERENCES groups(id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_member_student FOREIGN KEY (mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE chats
    ADD CONSTRAINT fk_chat_group FOREIGN KEY (group_id) 
        REFERENCES groups(id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_chat_sender FOREIGN KEY (mssv_sender) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_chat_receiver FOREIGN KEY (mssv_receiver) 
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE;


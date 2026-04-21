INSERT INTO course_categories (category_code, name, description)
VALUES ('CT', 'Khối kiến thức chính trị', 'Các môn chính trị')
ON CONFLICT (category_code) DO NOTHING;

-- Reclassify all political courses with code SS* to type CT for curricula 2017-2025.
UPDATE curriculum_courses cc
SET category_code = 'CT',
    updated_at = CURRENT_TIMESTAMP
WHERE cc.course_code LIKE 'SS%'
  AND (
    cc.curriculum_code LIKE '%_2017' OR
    cc.curriculum_code LIKE '%_2018' OR
    cc.curriculum_code LIKE '%_2019' OR
    cc.curriculum_code LIKE '%_2020' OR
    cc.curriculum_code LIKE '%_2021' OR
    cc.curriculum_code LIKE '%_2022' OR
    cc.curriculum_code LIKE '%_2023' OR
    cc.curriculum_code LIKE '%_2024' OR
    cc.curriculum_code LIKE '%_2025'
  )
  AND cc.category_code <> 'CT';

ALTER TABLE semester_summaries
    ADD COLUMN IF NOT EXISTS term_ct_credits INTEGER;

ALTER TABLE academic_summary
    ADD COLUMN IF NOT EXISTS accumulated_political_credits INTEGER;

-- Rename term_gpa to term_gpa_scale10 for clarity and naming consistency with term_gpa_scale4
ALTER TABLE semester_summaries
    RENAME COLUMN term_gpa TO term_gpa_scale10;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'semester_summaries'
          AND column_name = 'term_gpa_scale10'
    ) THEN
        ALTER TABLE semester_summaries
            ALTER COLUMN term_gpa_scale10 TYPE NUMERIC(4,2)
            USING ROUND(term_gpa_scale10::numeric, 2);
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'semester_summaries'
          AND column_name = 'term_gpa_scale4'
    ) THEN
        ALTER TABLE semester_summaries
            ALTER COLUMN term_gpa_scale4 TYPE NUMERIC(4,2)
            USING ROUND(term_gpa_scale4::numeric, 2);
    END IF;
END $$;


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

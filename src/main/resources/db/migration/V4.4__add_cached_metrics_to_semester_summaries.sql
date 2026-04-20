ALTER TABLE semester_summaries
    ADD COLUMN IF NOT EXISTS term_gpa_scale4 FLOAT,
    ADD COLUMN IF NOT EXISTS accumulated_credits INTEGER,
    ADD COLUMN IF NOT EXISTS term_dc_credits INTEGER,
    ADD COLUMN IF NOT EXISTS term_csnn_credits INTEGER,
    ADD COLUMN IF NOT EXISTS term_csn_credits INTEGER,
    ADD COLUMN IF NOT EXISTS term_cn_credits INTEGER,
    ADD COLUMN IF NOT EXISTS term_tottn_credits INTEGER,
    ADD COLUMN IF NOT EXISTS term_tc_credits INTEGER;

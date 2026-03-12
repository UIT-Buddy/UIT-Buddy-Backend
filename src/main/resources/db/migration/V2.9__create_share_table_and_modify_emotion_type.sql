CREATE TABLE shares(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    post_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_shares_post ON shares(post_id);
CREATE INDEX idx_shares_student ON shares(mssv);
CREATE UNIQUE INDEX idx_shares_unique ON shares(mssv, post_id);

ALTER TABLE shares
    ADD CONSTRAINT fk_shares_student FOREIGN KEY (mssv)
        REFERENCES students(mssv) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_shares_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE reactions
DROP COLUMN reaction_type;

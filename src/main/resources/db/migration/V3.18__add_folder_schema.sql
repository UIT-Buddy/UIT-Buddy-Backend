CREATE TABLE folders(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mssv VARCHAR(12) NOT NULL,
    parent_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folder_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE share_folder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    folder_id UUID NOT NULL,
    mssv VARCHAR(12) NOT NULL,
    access_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);


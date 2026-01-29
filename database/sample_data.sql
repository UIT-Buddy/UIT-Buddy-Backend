-- Password for all users: Password123!
-- (8+ chars, uppercase P, lowercase assword, number 123, special char !)

INSERT INTO users (id, email, mssv, password, full_name, is_verified)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    '21520001@gm.uit.edu.vn',
    '21520001',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'Nguyễn Văn A',
    true
);

INSERT INTO users (id, email, mssv, password, full_name, is_verified)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    '21520002@gm.uit.edu.vn',
    '21520002',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'Trần Thị B',
    false
);

INSERT INTO users (id, email, mssv, password, full_name, is_verified)
VALUES (
    '550e8400-e29b-41d4-a716-446655440003',
    'admin@gm.uit.edu.vn',
    '00000000',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'Admin User',
    true
);

INSERT INTO users (id, email, mssv, password, full_name, is_verified)
VALUES (
    '550e8400-e29b-41d4-a716-446655440004',
    '21520003@gm.uit.edu.vn',
    '21520003',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'Lê Văn C',
    true
);

INSERT INTO users (id, email, mssv, password, full_name, is_verified)
VALUES (
    '550e8400-e29b-41d4-a716-446655440005',
    '21520004@gm.uit.edu.vn',
    '21520004',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    'Phạm Thị D',
    true
);

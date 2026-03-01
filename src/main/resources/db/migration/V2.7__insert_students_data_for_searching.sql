INSERT INTO home_classes
(home_class_code, academic_year, advisor_name, major_code, created_at, updated_at)
VALUES
('CNTT01', 2022, 'TS. Trần Minh Đức', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT02', 2022, 'PGS. Nguyễn Văn Hòa', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT03', 2023, 'TS. Lê Thanh Hải', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT04', 2023, 'TS. Võ Quốc Bảo', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT05', 2024, 'PGS. Đặng Minh Tuấn', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO students
(mssv, full_name, email, comet_uid, home_class_code, password, created_at, updated_at)
VALUES
('22100001', 'Nguyễn Văn An', '22100001@student.edu.vn', 'comet_22100001', 'CNTT01', 'hashed_pw_1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100002', 'Trần Thị Bình', '22100002@student.edu.vn', 'comet_22100002', 'CNTT01', 'hashed_pw_2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100003', 'Lê Minh Châu', '22100003@student.edu.vn', 'comet_22100003', 'CNTT02', 'hashed_pw_3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100004', 'Phạm Quốc Duy', '22100004@student.edu.vn', 'comet_22100004', 'CNTT02', 'hashed_pw_4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100005', 'Hoàng Thị Em', '22100005@student.edu.vn', 'comet_22100005', 'CNTT03', 'hashed_pw_5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100006', 'Võ Thanh Giang', '22100006@student.edu.vn', 'comet_22100006', 'CNTT03', 'hashed_pw_6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100007', 'Đặng Hữu Hòa', '22100007@student.edu.vn', 'comet_22100007', 'CNTT01', 'hashed_pw_7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100008', 'Bùi Ngọc Hân', '22100008@student.edu.vn', 'comet_22100008', 'CNTT02', 'hashed_pw_8', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100009', 'Phan Đức Khang', '22100009@student.edu.vn', 'comet_22100009', 'CNTT03', 'hashed_pw_9', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100010', 'Ngô Thị Lan', '22100010@student.edu.vn', 'comet_22100010', 'CNTT01', 'hashed_pw_10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100011', 'Trịnh Văn Minh', '22100011@student.edu.vn', 'comet_22100011', 'CNTT02', 'hashed_pw_11', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100012', 'Lý Thu Ngân', '22100012@student.edu.vn', 'comet_22100012', 'CNTT03', 'hashed_pw_12', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100013', 'Tô Hoài Nam', '22100013@student.edu.vn', 'comet_22100013', 'CNTT01', 'hashed_pw_13', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100014', 'Đỗ Khánh Ngọc', '22100014@student.edu.vn', 'comet_22100014', 'CNTT02', 'hashed_pw_14', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100015', 'Hà Quang Phúc', '22100015@student.edu.vn', 'comet_22100015', 'CNTT03', 'hashed_pw_15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100016', 'Vũ Thị Quỳnh', '22100016@student.edu.vn', 'comet_22100016', 'CNTT01', 'hashed_pw_16', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100017', 'Mai Trung Sơn', '22100017@student.edu.vn', 'comet_22100017', 'CNTT02', 'hashed_pw_17', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100018', 'Nguyễn Hải Tú', '22100018@student.edu.vn', 'comet_22100018', 'CNTT03', 'hashed_pw_18', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100019', 'Phùng Gia Uyên', '22100019@student.edu.vn', 'comet_22100019', 'CNTT01', 'hashed_pw_19', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22100020', 'Lâm Anh Vũ', '22100020@student.edu.vn', 'comet_22100020', 'CNTT02', 'hashed_pw_20', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


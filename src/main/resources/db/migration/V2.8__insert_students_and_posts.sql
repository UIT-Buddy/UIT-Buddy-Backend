INSERT INTO home_classes
(home_class_code, academic_year, advisor_name, major_code, created_at, updated_at)
VALUES
('CNTT01', 2022, 'TS. Trần Minh Đức', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT02', 2022, 'PGS. Nguyễn Văn Hòa', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT03', 2023, 'TS. Lê Thanh Hải', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT04', 2023, 'TS. Võ Quốc Bảo', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNTT05', 2024, 'PGS. Đặng Minh Tuấn', 'CNTT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO students
(mssv, full_name, email, comet_uid, home_class_code, password, avatar_url, created_at, updated_at)
VALUES
('22100001','Nguyễn Văn An','22100001@student.edu.vn','comet_22100001','CNTT01','hashed_pw_1','https://api.dicebear.com/9.x/adventurer/svg?seed=22100001',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100002','Trần Thị Bình','22100002@student.edu.vn','comet_22100002','CNTT01','hashed_pw_2','https://api.dicebear.com/9.x/fun-emoji/svg?seed=22100002',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100003','Lê Minh Châu','22100003@student.edu.vn','comet_22100003','CNTT02','hashed_pw_3','https://api.dicebear.com/9.x/bottts/svg?seed=22100003',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100004','Phạm Quốc Duy','22100004@student.edu.vn','comet_22100004','CNTT02','hashed_pw_4','https://api.dicebear.com/9.x/pixel-art/svg?seed=22100004',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100005','Hoàng Thị Em','22100005@student.edu.vn','comet_22100005','CNTT03','hashed_pw_5','https://api.dicebear.com/9.x/notionists/svg?seed=22100005',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100006','Võ Thanh Giang','22100006@student.edu.vn','comet_22100006','CNTT03','hashed_pw_6','https://api.dicebear.com/9.x/thumbs/svg?seed=22100006',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100007','Đặng Hữu Hòa','22100007@student.edu.vn','comet_22100007','CNTT01','hashed_pw_7','https://api.dicebear.com/9.x/lorelei/svg?seed=22100007',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100008','Bùi Ngọc Hân','22100008@student.edu.vn','comet_22100008','CNTT02','hashed_pw_8','https://api.dicebear.com/9.x/adventurer-neutral/svg?seed=22100008',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100009','Phan Đức Khang','22100009@student.edu.vn','comet_22100009','CNTT03','hashed_pw_9','https://ui-avatars.com/api/?name=Phan+Đức+Khang&background=random',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100010','Ngô Thị Lan','22100010@student.edu.vn','comet_22100010','CNTT01','hashed_pw_10','https://ui-avatars.com/api/?name=Ngô+Thị+Lan&background=random',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100011','Trịnh Văn Minh','22100011@student.edu.vn','comet_22100011','CNTT02','hashed_pw_11','https://ui-avatars.com/api/?name=Trịnh+Văn+Minh&background=random',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100012','Lý Thu Ngân','22100012@student.edu.vn','comet_22100012','CNTT03','hashed_pw_12','https://ui-avatars.com/api/?name=Lý+Thu+Ngân&background=random',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100013','Tô Hoài Nam','22100013@student.edu.vn','comet_22100013','CNTT01','hashed_pw_13','https://ui-avatars.com/api/?name=Tô+Hoài+Nam&background=random',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100014','Đỗ Khánh Ngọc','22100014@student.edu.vn','comet_22100014','CNTT02','hashed_pw_14','https://ui-avatars.com/api/?name=Đỗ+Khánh+Ngọc&background=random',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100015','Hà Quang Phúc','22100015@student.edu.vn','comet_22100015','CNTT03','hashed_pw_15','https://i.pravatar.cc/150?img=1',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100016','Vũ Thị Quỳnh','22100016@student.edu.vn','comet_22100016','CNTT01','hashed_pw_16','https://i.pravatar.cc/150?img=2',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100017','Mai Trung Sơn','22100017@student.edu.vn','comet_22100017','CNTT02','hashed_pw_17','https://i.pravatar.cc/150?img=3',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100018','Nguyễn Hải Tú','22100018@student.edu.vn','comet_22100018','CNTT03','hashed_pw_18','https://i.pravatar.cc/150?img=4',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100019','Phùng Gia Uyên','22100019@student.edu.vn','comet_22100019','CNTT01','hashed_pw_19','https://i.pravatar.cc/150?img=5',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

('22100020','Lâm Anh Vũ','22100020@student.edu.vn','comet_22100020','CNTT02','hashed_pw_20','https://i.pravatar.cc/150?img=6',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);


INSERT INTO posts
(
    mssv,
    title,
    content,
    created_at,
    updated_at,
    medias
)
VALUES
(
    '22100001',
    'Thư viện khu nào học yên tĩnh nhất vậy mọi người?',
    'Mình mới bắt đầu lên thư viện học thường xuyên hơn nên muốn hỏi mọi người khu nào ngồi ổn, ít ồn và phù hợp để ngồi lâu với laptop. Ai hay học ở thư viện cho mình xin ít review với nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_38c3d0ae.jpg"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244467/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_51db58be.jpg"}
    ]'::jsonb
),
(
    '22100002',
    'Có ai hay học nhóm ở thư viện không?',
    'Nhóm mình đang tính đổi từ quán cà phê sang thư viện để học nhóm cho đỡ ồn. Không biết ở thư viện có chỗ nào phù hợp để trao đổi nhỏ không và nên đi khung giờ nào để dễ kiếm chỗ?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_38c3d0ae.jpg"}
    ]'::jsonb
),
(
    '22100003',
    'Mọi người giải thích giúp mình Spring Boot lifecycle với servlet được không?',
    'Mình đang xem sơ đồ flow request trong Spring Boot nhưng vẫn chưa hiểu rõ servlet container, dispatcher servlet và controller phối hợp với nhau như thế nào. Ai hiểu phần này giải thích giúp mình theo kiểu từng bước với.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244457/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_f0461996.png"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245149/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_9cf91bdb.jpg"}
    ]'::jsonb
),
(
    '22100004',
    'DispatcherServlet hoạt động ra sao vậy?',
    'Mình hiểu nó là front controller nhưng vẫn chưa hình dung rõ nó nhận request, gọi handler mapping, controller rồi trả response thế nào. Ai có ví dụ đơn giản dễ hiểu cho mình xin với.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244457/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_f0461996.png"}
    ]'::jsonb
),
(
    '22100005',
    'MLOps pipeline nếu làm đồ án thì nên tối giản phần nào?',
    'Nhìn pipeline MLOps có khá nhiều bước từ data ingestion, training, validation tới deployment và monitoring. Nếu làm ở mức sinh viên thì nên giữ những phần nào để vẫn đúng tư duy mà không quá nặng?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244459/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_b1d72f0c.jpg"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245154/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_6d673ed9.jpg"}
    ]'::jsonb
),
(
    '22100006',
    'Flow code cho project ML như này đã ổn chưa?',
    'Hiện tại mình đang tách code thành các bước load_data, preprocess, split_data, train, evaluate, save_model. Theo mọi người nếu muốn dễ maintain hơn thì nên thêm logging, config hay versioning ở đoạn nào?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244459/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_b1d72f0c.jpg"}
    ]'::jsonb
),
(
    '22100007',
    'Tìm teammate làm đồ án cuối kì mảng web',
    'Nhóm mình đang có 2 người và muốn làm một đồ án về web app cho sinh viên. Bọn mình cần thêm 1 hoặc 2 bạn có thể support backend hoặc frontend, ai muốn tham gia nghiêm túc thì comment nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_8f3eb420.jpg"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244464/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_add54eb9.jpg"}
    ]'::jsonb
),
(
    '22100008',
    'Có ai đang thiếu nhóm đồ án không cho mình ghép với',
    'Mình đang tìm nhóm làm đồ án cuối kì, ưu tiên nhóm có hướng làm rõ ràng và chịu code thật. Mình làm được phần database, API cơ bản và test, ai đang thiếu người thì cho mình xin join.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_8f3eb420.jpg"}
    ]'::jsonb
),
(
    '22100009',
    'Làm leader nhóm sao để đỡ bị ôm việc?',
    'Đợt này mình đang làm leader nhóm và cảm giác gần như việc gì cuối cùng cũng dồn về mình. Mọi người có kinh nghiệm chia task, theo dõi tiến độ và giữ deadline sao cho cả nhóm cùng chạy không?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244464/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_add54eb9.jpg"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_8f3eb420.jpg"}
    ]'::jsonb
),
(
    '22100010',
    'Theo mọi người leader tốt cần những kỹ năng gì?',
    'Mình nghĩ leader không chỉ cần kỹ thuật mà còn cần biết giao tiếp, chia task và giải quyết mâu thuẫn. Theo mọi người kỹ năng nào là quan trọng nhất với leader trong một nhóm sinh viên?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244464/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_add54eb9.jpg"}
    ]'::jsonb
),
(
    '22100011',
    'Đi quanh làng đại học buổi chiều thật sự rất chill',
    'Hôm nay mình chạy xe một vòng quanh làng đại học, tự nhiên thấy đầu óc nhẹ hơn hẳn sau mấy ngày deadline. Ai hay đi dạo quanh khu này chắc hiểu cảm giác vừa thoáng vừa vui ấy.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244467/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_51db58be.jpg"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_38c3d0ae.jpg"}
    ]'::jsonb
),
(
    '22100012',
    'Cuối tuần ai rảnh đi dạo quanh làng đại học không?',
    'Dạo này học hơi bí nên mình muốn cuối tuần đi một vòng quanh làng đại học, uống gì đó rồi tám chuyện cho thoải mái đầu óc. Bạn nào muốn đi chung thì comment nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244467/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_51db58be.jpg"}
    ]'::jsonb
),
(
    '22100013',
    'Nhóm sinh viên có nên áp dụng Scrum không?',
    'Mình thấy Scrum nghe rất hay nhưng khi áp vào nhóm sinh viên thì dễ thành hình thức. Theo mọi người nếu nhóm 4 đến 5 người thì nên tối giản quy trình Scrum như thế nào cho hiệu quả?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244469/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_65fa0f3c.jpg"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244471/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_76931c75.jpg"}
    ]'::jsonb
),
(
    '22100014',
    'Daily Scrum có thật sự cần với nhóm nhỏ không?',
    'Nhóm mình đang thử họp nhanh mỗi ngày 10 phút để cập nhật task. Có hôm rất hữu ích nhưng có hôm thấy hơi gượng. Mọi người đã từng áp dụng daily scrum cho project môn học chưa?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244471/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_76931c75.jpg"}
    ]'::jsonb
),
(
    '22100015',
    'Product backlog khác gì task list thông thường vậy?',
    'Mình đang hơi lẫn giữa product backlog, sprint backlog và task list. Nếu làm một project web nhỏ thì quản lý ở mức nào là hợp lý để vừa gọn vừa không bị thiếu việc?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244471/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_76931c75.jpg"}
    ]'::jsonb
),
(
    '22100016',
    'CV sinh viên nên viết sao để đỡ bị trống?',
    'Mình đang làm CV để đi xin thực tập mà thấy phần kinh nghiệm gần như chưa có gì. Ngoài thông tin học tập và project môn học thì nên trình bày thêm gì để CV nhìn ổn hơn?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244471/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_beaa4330.jpg"}
    ]'::jsonb
),
(
    '22100017',
    'DevOps cho project sinh viên nên học gì trước?',
    'Mình mới tìm hiểu DevOps nên đang hơi rối giữa Docker, CI/CD, logging, monitoring và deployment. Nếu mục tiêu là đủ để làm project hoặc đi intern thì nên học theo thứ tự nào?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244472/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_89a462ff.png"}
    ]'::jsonb
),
(
    '22100018',
    'Quy trình DevOps thực tế có khác nhiều với sơ đồ không?',
    'Mình xem sơ đồ DevOps thấy từ code tới deploy và monitor rất đẹp, nhưng chắc đi làm thật sẽ phức tạp hơn nhiều. Ai từng thực tập rồi chia sẻ giúp mình phần nào là quan trọng nhất với intern backend nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245149/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_15f44c23.jpg"}
    ]'::jsonb
),
(
    '22100019',
    'Roadmap học Spring để đi thực tập nên như thế nào?',
    'Mình đang học Java backend và muốn theo hướng Spring để xin intern. Theo mọi người nên đi theo thứ tự Java core, SQL, Spring Boot, JPA, REST, Security rồi Docker có hợp lý không?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245149/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_9cf91bdb.jpg"}
    ]'::jsonb
),
(
    '22100020',
    'Môn nhập môn thị giác máy tính cần chuẩn bị gì trước?',
    'Mình đang quan tâm môn nhập môn thị giác máy tính nhưng chưa biết nên chuẩn bị nền tảng gì trước. Toán, Python, xử lý ảnh hay machine learning cái nào nên ưu tiên hơn?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245151/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_9160b4e6.jpg"}
    ]'::jsonb
),
(
    '22100001',
    'Tìm bạn học chung môn nhập môn thị giác máy tính',
    'Mình muốn tìm vài bạn đang tự học computer vision để cùng nhau học và có động lực hơn. Có thể lập nhóm nhỏ, share tài liệu, làm mini project và hỗ trợ nhau trong quá trình học.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245151/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_a61140ba.jpg"}
    ]'::jsonb
),
(
    '22100002',
    'Mọi người xem giúp mình đoạn code Python này sai ở đâu',
    'Mình có đoạn code như sau: nums = [1, 2, 3, 4], rồi append nums[i] * i. Mình muốn hỏi cách viết này đã ổn chưa, và nếu dùng list comprehension thì nên viết thế nào để dễ đọc hơn?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245154/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_0e8ac613.jpg"}
    ]'::jsonb
),
(
    '22100003',
    'Làm machine learning thì mọi người hay dùng library nào nhất?',
    'Mình thấy học ML có rất nhiều thư viện như NumPy, Pandas, Matplotlib, Scikit-learn, TensorFlow, PyTorch. Nếu mới bắt đầu mà muốn làm được mini project thì nên tập trung cái nào trước?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245154/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_6d673ed9.jpg"}
    ]'::jsonb
),
(
    '22100004',
    'Intern NodeJS cần nắm flow làm việc nào?',
    'Ngoài chuyện biết Express hoặc NestJS thì intern NodeJS có nên sớm làm quen với nhận task, debug API, đọc log, review code và deploy local không? Ai từng intern rồi cho mình xin ít kinh nghiệm thực tế nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245159/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_889c035b.png"}
    ]'::jsonb
),
(
    '22100005',
    'Architecture NodeJS này khác gì so với Spring Boot?',
    'Mình quen project Java kiểu controller, service, repository rồi nên khi xem một số architecture NodeJS thấy khá khác. Theo mọi người khác biệt lớn nhất về tư duy tổ chức code giữa hai bên là gì?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245156/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_b08efd98.jpg"}
    ]'::jsonb
),
(
    '22100006',
    '10 bước học Spring Boot bài bản cho sinh viên',
    'Mình thấy roadmap này khá ổn cho các bạn mới bắt đầu học backend với Spring Boot. Share lên đây để mọi người tham khảo và nếu ai đã đi thực tập rồi thì góp ý thêm giúp.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245159/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_ffe9db02.png"}
    ]'::jsonb
),
(
    '22100007',
    '10 trang tìm việc thực tập IT mình thấy khá hữu ích',
    'Bạn nào đang tìm chỗ thực tập hoặc việc part time có thể tham khảo mấy web này. Theo mình nên chuẩn bị CV ổn rồi apply đều thay vì đợi tới lúc sát deadline mới bắt đầu.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245164/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_1830f573.jpg"}
    ]'::jsonb
),
(
    '22100008',
    'Buổi sáng hay buổi chiều đi thư viện học ổn hơn?',
    'Mình đang muốn đổi không khí học tập nên tính lên thư viện thường xuyên hơn. Không biết buổi sáng hay buổi chiều thì dễ kiếm chỗ và tập trung hơn vậy mọi người?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_38c3d0ae.jpg"}
    ]'::jsonb
),
(
    '22100009',
    'Rủ làm mini project cuối tuần để nâng CV',
    'Mình muốn rủ vài bạn làm một mini project nhỏ để vừa học vừa có thêm cái đưa vào CV. Ý tưởng hiện tại là forum sinh viên hoặc task manager, ai muốn tham gia nghiêm túc thì vào bàn nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244461/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_8f3eb420.jpg"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244464/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_add54eb9.jpg"}
    ]'::jsonb
),
(
    '22100010',
    'Retrospective trong Scrum có đáng để làm không?',
    'Mình thấy nhiều nhóm chỉ planning rồi code luôn, ít khi nhìn lại sprint trước có gì chưa ổn. Theo mọi người retrospective có thật sự giúp team cải thiện không và nên hỏi gì trong buổi retro?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244469/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_65fa0f3c.jpg"}
    ]'::jsonb
),
(
    '22100011',
    'Có ai ôn OOP bằng Java chung không?',
    'Dạo này mình đang ôn lại OOP với Java vì thấy nền này ảnh hưởng rất nhiều môn sau. Nếu có bạn nào cũng đang học class, interface, abstract, polymorphism thì học chung cho có động lực nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[]'::jsonb
),
(
    '22100012',
    'Tìm người làm project quản lý chi tiêu cá nhân',
    'Mình muốn làm một project nhỏ về quản lý chi tiêu cá nhân để luyện backend, database và giao diện cơ bản. Cần thêm 1 đến 2 bạn có hứng thú làm nghiêm túc, ai muốn join thì để lại bình luận nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[]'::jsonb
),
(
    '22100013',
    'Tối mai ai rảnh đi cà phê nói chuyện thực tập không?',
    'Mình muốn ngồi nói chuyện nhẹ nhàng về học tập, CV, thực tập và định hướng backend hoặc frontend vì dạo này thấy hơi mông lung. Bạn nào cũng đang ở giai đoạn chuẩn bị đi thực tập thì có thể đi cùng.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[]'::jsonb
),
(
    '22100014',
    'Mọi người học SQL kiểu gì để viết query đỡ bí?',
    'Mình biết các câu lệnh cơ bản rồi nhưng khi làm bài có join, group by, subquery là bắt đầu chậm lại hẳn. Theo mọi người để học SQL chắc tay thì nên luyện theo dạng bài nào trước?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[]'::jsonb
),
(
    '22100015',
    'Cuối tuần ai muốn đi ăn vặt quanh làng đại học không?',
    'Học mãi cũng cần đổi gió nên mình định cuối tuần đi loanh quanh khu làng đại học, kiếm gì ăn rồi ngồi tám chuyện. Bạn nào muốn đi cùng cho vui thì vào đây nhé.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[]'::jsonb
),
(
    '22100016',
    'Tìm bạn frontend ghép team làm forum sinh viên',
    'Mình đang muốn làm một forum sinh viên đơn giản có đăng bài, bình luận và thả cảm xúc. Mình phụ trách backend được, đang cần một bạn biết frontend cơ bản để làm cùng.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[]'::jsonb
),
(
    '22100017',
    'Phỏng vấn intern backend thường bị hỏi gì?',
    'Mình sắp bắt đầu apply intern backend nên muốn hỏi kinh nghiệm từ mọi người. Thường nhà tuyển dụng sẽ hỏi nặng về Java core, Spring, SQL hay thiên về project mình từng làm?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[]'::jsonb
),
(
    '22100018',
    'CV nên để project môn học ở đâu cho hợp lý?',
    'Mình đang phân vân không biết nên đẩy project môn học lên phần nổi bật hay để trong mục dự án riêng. Vì chưa có kinh nghiệm đi làm nên mình muốn tận dụng project ở trường cho tốt nhất.',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244471/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_beaa4330.jpg"}
    ]'::jsonb
),
(
    '22100019',
    'Nếu chỉ có một tháng thì nên học DevOps phần nào trước?',
    'Giả sử chỉ còn khoảng một tháng để chuẩn bị kiến thức cho project hoặc CV, theo mọi người nên ưu tiên Docker và CI/CD trước hay học thêm Linux, Nginx, monitoring luôn?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773244472/posts/images/423dfc82-a019-4ead-84b3-360d85a45786_89a462ff.png"},
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245149/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_15f44c23.jpg"}
    ]'::jsonb
),
(
    '22100020',
    'Ngoài mấy trang tuyển dụng phổ biến thì mọi người còn tìm intern ở đâu?',
    'Mình thấy apply qua web tuyển dụng khá tiện nhưng cạnh tranh cũng cao. Mọi người thường tìm cơ hội qua group, fanpage công ty, LinkedIn hay nhờ anh chị khóa trên giới thiệu nhiều hơn?',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '[
        {"type": "IMAGE", "url": "https://res.cloudinary.com/dgogrzt5d/image/upload/v1773245164/posts/images/3e0abf61-9818-4390-b2b4-10e63901e60a_1830f573.jpg"}
    ]'::jsonb
);
-- Sample posts from different students
INSERT INTO posts (id, mssv, title, content, image_url, video_url, created_at, updated_at) VALUES
-- Post 1: Text only
('550e8400-e29b-41d4-a716-446655440001', '22100001', 
'Chia sẻ kinh nghiệm học lập trình', 
'Mình vừa hoàn thành khóa học Java Spring Boot và muốn chia sẻ một số tips học tập hiệu quả. Đầu tiên là nên làm project thực tế thay vì chỉ học lý thuyết. Thứ hai là tham gia các cộng đồng developer để học hỏi kinh nghiệm. Cuối cùng là đừng ngại hỏi khi gặp khó khăn!',
NULL, NULL,
'2026-03-01 08:00:00', '2026-03-01 08:00:00'),

-- Post 2: With image
('550e8400-e29b-41d4-a716-446655440002', '22100002',
'Thư viện UIT buổi sáng',
'Không khí học tập ở thư viện UIT buổi sáng thật tuyệt vời. Yên tĩnh, mát mẻ và đầy đủ tài liệu. Các bạn nên đến sớm để có chỗ ngồi đẹp nhé!',
'https://res.cloudinary.com/demo/image/upload/v1234567890/posts/library_morning.jpg',
NULL,
'2026-03-02 09:30:00', '2026-03-02 09:30:00'),

-- Post 3: With video
('550e8400-e29b-41d4-a716-446655440003', '22100003',
'Hướng dẫn cài đặt Docker cho người mới bắt đầu',
'Video hướng dẫn chi tiết cách cài đặt và sử dụng Docker cơ bản. Rất hữu ích cho các bạn đang học môn Công nghệ phần mềm!',
NULL,
'https://res.cloudinary.com/demo/video/upload/v1234567890/posts/docker_tutorial.mp4',
'2026-03-03 14:20:00', '2026-03-03 14:20:00'),

-- Post 4: Text only
('550e8400-e29b-41d4-a716-446655440004', '22100004',
'Tìm teammate làm đồ án cuối kỳ',
'Mình đang tìm 2-3 bạn để làm đồ án môn Phát triển ứng dụng Web. Dự định làm một trang web quản lý thư viện. Ai có hứng thú inbox mình nhé!',
NULL, NULL,
'2026-03-04 10:15:00', '2026-03-04 10:15:00'),

-- Post 5: With image
('550e8400-e29b-41d4-a716-446655440005', '22100005',
'Kết quả thi giữa kỳ môn Cấu trúc dữ liệu',
'Vừa nhận được kết quả thi giữa kỳ, cảm ơn thầy đã ra đề vừa sức. Các bạn nhớ ôn kỹ phần cây nhị phân và đồ thị nhé!',
'https://res.cloudinary.com/demo/image/upload/v1234567890/posts/exam_result.jpg',
NULL,
'2026-03-05 16:45:00', '2026-03-05 16:45:00'),

-- Post 6: Text only
('550e8400-e29b-41d4-a716-446655440006', '22100001',
'Review khóa học online về Machine Learning',
'Mình vừa hoàn thành khóa học ML trên Coursera. Nội dung rất chất lượng, giảng viên giải thích dễ hiểu. Recommend cho các bạn quan tâm đến AI!',
NULL, NULL,
'2026-03-06 11:00:00', '2026-03-06 11:00:00'),

-- Post 7: With image
('550e8400-e29b-41d4-a716-446655440007', '22100006',
'Sự kiện IT Job Fair 2026',
'Hôm nay tham gia IT Job Fair tại UIT, có rất nhiều công ty lớn tuyển dụng. Các bạn sinh viên năm cuối nên tham gia để tìm cơ hội thực tập và việc làm!',
'https://res.cloudinary.com/demo/image/upload/v1234567890/posts/job_fair.jpg',
NULL,
'2026-03-07 15:30:00', '2026-03-07 15:30:00'),

-- Post 8: Text only
('550e8400-e29b-41d4-a716-446655440008', '22100007',
'Câu hỏi về thuật toán Dijkstra',
'Các bạn có thể giải thích cho mình tại sao thuật toán Dijkstra không hoạt động với cạnh có trọng số âm được không? Mình đang bí phần này quá!',
NULL, NULL,
'2026-03-08 09:20:00', '2026-03-08 09:20:00'),

-- Post 9: With video
('550e8400-e29b-41d4-a716-446655440009', '22100008',
'Demo đồ án môn Lập trình di động',
'Video demo ứng dụng quản lý chi tiêu cá nhân của nhóm mình. Sử dụng React Native và Firebase. Mọi người xem và góp ý nhé!',
NULL,
'https://res.cloudinary.com/demo/video/upload/v1234567890/posts/mobile_app_demo.mp4',
'2026-03-08 13:40:00', '2026-03-08 13:40:00'),

-- Post 10: With image
('550e8400-e29b-41d4-a716-446655440010', '22100009',
'Setup bàn làm việc cho dân IT',
'Chia sẻ góc làm việc của mình sau khi sắm thêm màn hình phụ. Năng suất làm việc tăng lên rõ rệt. Các bạn có setup nào hay thì share nhé!',
'https://res.cloudinary.com/demo/image/upload/v1234567890/posts/workspace_setup.jpg',
NULL,
'2026-03-08 17:00:00', '2026-03-08 17:00:00'),

-- Post 11: Text only
('550e8400-e29b-41d4-a716-446655440011', '22100010',
'Lịch thi cuối kỳ học kỳ 2',
'Nhà trường vừa công bố lịch thi cuối kỳ. Các bạn nhớ check kỹ lịch thi và chuẩn bị ôn tập từ sớm nhé. Chúc mọi người thi tốt!',
NULL, NULL,
'2026-03-08 19:15:00', '2026-03-08 19:15:00'),

-- Post 12: With image
('550e8400-e29b-41d4-a716-446655440012', '22100002',
'Buổi workshop về Cloud Computing',
'Hôm nay tham gia workshop về AWS và Azure. Học được rất nhiều kiến thức thực tế về triển khai ứng dụng lên cloud. Cảm ơn các anh chị diễn giả!',
'https://res.cloudinary.com/demo/image/upload/v1234567890/posts/cloud_workshop.jpg',
NULL,
'2026-03-08 20:30:00', '2026-03-08 20:30:00'),

-- Post 13: Text only
('550e8400-e29b-41d4-a716-446655440013', '22100003',
'Tài liệu ôn thi môn Cơ sở dữ liệu',
'Mình có tổng hợp tài liệu ôn thi môn CSDL, bao gồm slide bài giảng, bài tập và đề thi các năm. Ai cần thì inbox mình nhé!',
NULL, NULL,
'2026-03-08 21:45:00', '2026-03-08 21:45:00'),

-- Post 14: With video
('550e8400-e29b-41d4-a716-446655440014', '22100004',
'Giới thiệu về Git và GitHub',
'Video hướng dẫn sử dụng Git và GitHub cho người mới bắt đầu. Từ cài đặt, tạo repository đến làm việc nhóm. Rất cần thiết cho các bạn làm đồ án!',
NULL,
'https://res.cloudinary.com/demo/video/upload/v1234567890/posts/git_tutorial.mp4',
'2026-03-08 22:00:00', '2026-03-08 22:00:00'),

-- Post 15: With image
('550e8400-e29b-41d4-a716-446655440015', '22100005',
'Hoàng hôn trên sân thượng UIT',
'Khoảnh khắc hoàng hôn tuyệt đẹp trên sân thượng tòa nhà A. Sau những giờ học căng thẳng, được ngắm cảnh này thật thư giãn!',
'https://res.cloudinary.com/demo/image/upload/v1234567890/posts/sunset_uit.jpg',
NULL,
'2026-03-08 23:30:00', '2026-03-08 23:30:00');
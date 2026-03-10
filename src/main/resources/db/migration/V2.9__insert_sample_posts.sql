-- Đảm bảo cấu trúc bảng của bạn đã có cột medias (JSONB) và đã xóa/bỏ qua image_url, video_url
INSERT INTO posts (id, mssv, title, content, medias, created_at, updated_at) VALUES
-- Post 1: Text only
('550e8400-e29b-41d4-a716-446655440001', '22100001', 
'Chia sẻ kinh nghiệm học lập trình', 
'Mình vừa hoàn thành khóa học Java Spring Boot và muốn chia sẻ một số tips học tập hiệu quả cho các bạn đang bắt đầu con đường trở thành developer. Đầu tiên và quan trọng nhất là nên làm project thực tế thay vì chỉ học lý thuyết suông. Khi làm project, bạn sẽ gặp rất nhiều vấn đề thực tế mà sách vở không dạy, từ đó học được cách debug, tìm kiếm giải pháp và tư duy giải quyết vấn đề. Thứ hai là tham gia các cộng đồng developer như GitHub, Stack Overflow, các group Facebook về lập trình để học hỏi kinh nghiệm từ những người đi trước. Đừng ngại chia sẻ code của mình và nhận feedback từ cộng đồng. Thứ ba là hãy đọc documentation chính thức thay vì chỉ xem tutorial, vì documentation sẽ giúp bạn hiểu sâu hơn về cách thư viện hoạt động. Thứ tư là practice coding mỗi ngày, dù chỉ 30 phút cũng tốt hơn là học dồn vào cuối tuần. Cuối cùng là đừng ngại hỏi khi gặp khó khăn, nhưng trước khi hỏi hãy tự research kỹ càng để tôn trọng thời gian của người giúp đỡ. Chúc các bạn thành công trên con đường coding!',
'[]'::jsonb,
'2026-03-01 08:00:00', '2026-03-01 08:00:00'),

-- Post 2: With image
('550e8400-e29b-41d4-a716-446655440002', '22100002',
'Thư viện UIT buổi sáng',
'Không khí học tập ở thư viện UIT buổi sáng thật tuyệt vời. Yên tĩnh, mát mẻ và đầy đủ tài liệu. Các bạn nên đến sớm để có chỗ ngồi đẹp nhé!',
'[{"type": "IMAGE", "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/posts/library_morning.jpg"}]'::jsonb,
'2026-03-02 09:30:00', '2026-03-02 09:30:00'),

-- Post 3: With video
('550e8400-e29b-41d4-a716-446655440003', '22100003',
'Hướng dẫn cài đặt Docker cho người mới bắt đầu',
'Video hướng dẫn chi tiết cách cài đặt và sử dụng Docker cơ bản dành cho người mới bắt đầu. Docker là một platform giúp bạn đóng gói ứng dụng cùng với tất cả dependencies vào một container, đảm bảo ứng dụng chạy nhất quán trên mọi môi trường. Trong video này, mình sẽ hướng dẫn các bạn từng bước: Bước 1 - Cài đặt Docker Desktop trên Windows/Mac, Bước 2 - Hiểu về Docker Image và Container, Bước 3 - Viết Dockerfile đầu tiên, Bước 4 - Build và run container, Bước 5 - Sử dụng Docker Compose để quản lý multi-container application. Video rất hữu ích cho các bạn đang học môn Công nghệ phần mềm hoặc chuẩn bị làm đồ án tốt nghiệp. Sau khi xem video, các bạn sẽ có thể tự tin deploy ứng dụng của mình lên server production. Đừng quên like và subscribe để ủng hộ mình nhé!',
'[{"type": "VIDEO", "url": "https://res.cloudinary.com/demo/video/upload/v1234567890/posts/docker_tutorial.mp4"}]'::jsonb,
'2026-03-03 14:20:00', '2026-03-03 14:20:00'),

-- Post 4: Text only
('550e8400-e29b-41d4-a716-446655440004', '22100004',
'Tìm teammate làm đồ án cuối kỳ',
'Mình đang tìm 2-3 bạn để làm đồ án môn Phát triển ứng dụng Web. Dự định làm một trang web quản lý thư viện. Ai có hứng thú inbox mình nhé!',
'[]'::jsonb,
'2026-03-04 10:15:00', '2026-03-04 10:15:00'),

-- Post 5: With image
('550e8400-e29b-41d4-a716-446655440005', '22100005',
'Kết quả thi giữa kỳ môn Cấu trúc dữ liệu',
'Vừa nhận được kết quả thi giữa kỳ, cảm ơn thầy đã ra đề vừa sức. Các bạn nhớ ôn kỹ phần cây nhị phân và đồ thị nhé!',
'[{"type": "IMAGE", "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/posts/exam_result.jpg"}]'::jsonb,
'2026-03-05 16:45:00', '2026-03-05 16:45:00'),

-- Post 6: Text only
('550e8400-e29b-41d4-a716-446655440006', '22100001',
'Review khóa học online về Machine Learning',
'Mình vừa hoàn thành khóa học Machine Learning trên Coursera do giáo sư Andrew Ng giảng dạy và muốn chia sẻ trải nghiệm của mình. Khóa học này thực sự rất chất lượng và phù hợp cho người mới bắt đầu tìm hiểu về AI. Nội dung được chia thành nhiều module từ cơ bản đến nâng cao: Linear Regression, Logistic Regression, Neural Networks, Support Vector Machines, Unsupervised Learning, và Recommender Systems. Điểm mạnh nhất của khóa học là cách giảng viên giải thích các khái niệm phức tạp một cách dễ hiểu, kèm theo rất nhiều ví dụ thực tế. Các bài tập programming được thiết kế rất tốt, giúp bạn hiểu sâu về thuật toán thay vì chỉ sử dụng thư viện có sẵn. Tuy nhiên, khóa học yêu cầu kiến thức toán học khá cao, đặc biệt là đại số tuyến tính và giải tích. Mình recommend các bạn nên ôn lại toán trước khi học. Sau khi hoàn thành khóa học, mình cảm thấy tự tin hơn rất nhiều trong việc áp dụng ML vào các project thực tế. Highly recommend cho các bạn quan tâm đến AI và Data Science!',
'[]'::jsonb,
'2026-03-06 11:00:00', '2026-03-06 11:00:00'),

-- Post 7: With image
('550e8400-e29b-41d4-a716-446655440007', '22100006',
'Sự kiện IT Job Fair 2026',
'Hôm nay tham gia IT Job Fair tại UIT, có rất nhiều công ty lớn tuyển dụng. Các bạn sinh viên năm cuối nên tham gia để tìm cơ hội thực tập và việc làm!',
'[{"type": "IMAGE", "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/posts/job_fair.jpg"}]'::jsonb,
'2026-03-07 15:30:00', '2026-03-07 15:30:00'),

-- Post 8: Text only
('550e8400-e29b-41d4-a716-446655440008', '22100007',
'Câu hỏi về thuật toán Dijkstra',
'Các bạn có thể giải thích cho mình tại sao thuật toán Dijkstra không hoạt động với cạnh có trọng số âm được không? Mình đang học môn Cấu trúc dữ liệu và Giải thuật, phần đồ thị đang bị bí quá. Theo như mình hiểu thì Dijkstra sử dụng greedy approach, chọn đỉnh có khoảng cách ngắn nhất chưa được xét và cập nhật khoảng cách đến các đỉnh kề. Nhưng tại sao khi có cạnh âm thì thuật toán lại cho kết quả sai? Mình đã thử vẽ đồ thị và chạy thuật toán bằng tay nhưng vẫn chưa hiểu rõ. Có bạn nào có thể cho mình một ví dụ cụ thể về trường hợp Dijkstra fail với cạnh âm không? Và trong trường hợp đó thì mình nên dùng thuật toán nào thay thế? Mình có nghe nói về Bellman-Ford nhưng chưa rõ nó khác Dijkstra như thế nào. Cảm ơn các bạn rất nhiều!',
'[]'::jsonb,
'2026-03-08 09:20:00', '2026-03-08 09:20:00'),

-- Post 9: With video
('550e8400-e29b-41d4-a716-446655440009', '22100008',
'Demo đồ án môn Lập trình di động',
'Video demo ứng dụng quản lý chi tiêu cá nhân của nhóm mình. Sử dụng React Native và Firebase. Mọi người xem và góp ý nhé!',
'[{"type": "VIDEO", "url": "https://res.cloudinary.com/demo/video/upload/v1234567890/posts/mobile_app_demo.mp4"}]'::jsonb,
'2026-03-08 13:40:00', '2026-03-08 13:40:00'),

-- Post 10: With image
('550e8400-e29b-41d4-a716-446655440010', '22100009',
'Setup bàn làm việc cho dân IT',
'Chia sẻ góc làm việc của mình sau khi sắm thêm màn hình phụ. Năng suất làm việc tăng lên rõ rệt. Các bạn có setup nào hay thì share nhé!',
'[{"type": "IMAGE", "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/posts/workspace_setup.jpg"}]'::jsonb,
'2026-03-08 17:00:00', '2026-03-08 17:00:00'),

-- Post 11: Text only
('550e8400-e29b-41d4-a716-446655440011', '22100010',
'Lịch thi cuối kỳ học kỳ 2',
'Nhà trường vừa công bố lịch thi cuối kỳ. Các bạn nhớ check kỹ lịch thi và chuẩn bị ôn tập từ sớm nhé. Chúc mọi người thi tốt!',
'[]'::jsonb,
'2026-03-08 19:15:00', '2026-03-08 19:15:00'),

-- Post 12: With image
('550e8400-e29b-41d4-a716-446655440012', '22100002',
'Buổi workshop về Cloud Computing',
'Hôm nay tham gia workshop về AWS và Azure. Học được rất nhiều kiến thức thực tế về triển khai ứng dụng lên cloud. Cảm ơn các anh chị diễn giả!',
'[{"type": "IMAGE", "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/posts/cloud_workshop.jpg"}]'::jsonb,
'2026-03-08 20:30:00', '2026-03-08 20:30:00'),

-- Post 13: Text only
('550e8400-e29b-41d4-a716-446655440013', '22100003',
'Tài liệu ôn thi môn Cơ sở dữ liệu',
'Mình có tổng hợp tài liệu ôn thi môn CSDL, bao gồm slide bài giảng, bài tập và đề thi các năm. Ai cần thì inbox mình nhé!',
'[]'::jsonb,
'2026-03-08 21:45:00', '2026-03-08 21:45:00'),

-- Post 14: With video
('550e8400-e29b-41d4-a716-446655440014', '22100004',
'Giới thiệu về Git và GitHub',
'Video hướng dẫn sử dụng Git và GitHub cho người mới bắt đầu. Từ cài đặt, tạo repository đến làm việc nhóm. Rất cần thiết cho các bạn làm đồ án!',
'[{"type": "VIDEO", "url": "https://res.cloudinary.com/demo/video/upload/v1234567890/posts/git_tutorial.mp4"}]'::jsonb,
'2026-03-08 22:00:00', '2026-03-08 22:00:00'),

-- Post 15: With image
('550e8400-e29b-41d4-a716-446655440015', '22100005',
'Hoàng hôn trên sân thượng UIT',
'Khoảnh khắc hoàng hôn tuyệt đẹp trên sân thượng tòa nhà A. Sau những giờ học căng thẳng, được ngắm cảnh này thật thư giãn!',
'[{"type": "IMAGE", "url": "https://res.cloudinary.com/demo/image/upload/v1234567890/posts/sunset_uit.jpg"}]'::jsonb,
'2026-03-08 23:30:00', '2026-03-08 23:30:00');
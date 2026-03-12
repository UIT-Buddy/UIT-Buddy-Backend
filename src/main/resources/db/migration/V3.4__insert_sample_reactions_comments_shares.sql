-- Insert sample reactions (likes on posts) using actual post IDs from database
INSERT INTO reactions (id, post_id, mssv, created_at, updated_at) 
SELECT 
    gen_random_uuid(),
    p.id,
    r.mssv,
    r.created_at,
    r.updated_at
FROM (
    SELECT ROW_NUMBER() OVER (ORDER BY created_at) as post_num, id FROM posts
) p
CROSS JOIN (
    VALUES 
    -- Reactions for first few posts
    (1, '22100002', '2026-03-01 08:15:00'::timestamp, '2026-03-01 08:15:00'::timestamp),
    (1, '22100003', '2026-03-01 08:30:00'::timestamp, '2026-03-01 08:30:00'::timestamp),
    (1, '22100004', '2026-03-01 09:00:00'::timestamp, '2026-03-01 09:00:00'::timestamp),
    (1, '22100005', '2026-03-01 10:20:00'::timestamp, '2026-03-01 10:20:00'::timestamp),
    (1, '22100006', '2026-03-01 11:45:00'::timestamp, '2026-03-01 11:45:00'::timestamp),
    
    (2, '22100001', '2026-03-02 09:45:00'::timestamp, '2026-03-02 09:45:00'::timestamp),
    (2, '22100003', '2026-03-02 10:00:00'::timestamp, '2026-03-02 10:00:00'::timestamp),
    (2, '22100007', '2026-03-02 10:30:00'::timestamp, '2026-03-02 10:30:00'::timestamp),
    
    (3, '22100001', '2026-03-03 14:35:00'::timestamp, '2026-03-03 14:35:00'::timestamp),
    (3, '22100002', '2026-03-03 15:00:00'::timestamp, '2026-03-03 15:00:00'::timestamp),
    (3, '22100004', '2026-03-03 16:20:00'::timestamp, '2026-03-03 16:20:00'::timestamp),
    (3, '22100008', '2026-03-03 17:10:00'::timestamp, '2026-03-03 17:10:00'::timestamp),
    
    (4, '22100005', '2026-03-04 10:30:00'::timestamp, '2026-03-04 10:30:00'::timestamp),
    (4, '22100006', '2026-03-04 11:00:00'::timestamp, '2026-03-04 11:00:00'::timestamp),
    
    (5, '22100001', '2026-03-05 17:00:00'::timestamp, '2026-03-05 17:00:00'::timestamp),
    (5, '22100002', '2026-03-05 17:15:00'::timestamp, '2026-03-05 17:15:00'::timestamp),
    (5, '22100003', '2026-03-05 17:30:00'::timestamp, '2026-03-05 17:30:00'::timestamp),
    (5, '22100004', '2026-03-05 18:00:00'::timestamp, '2026-03-05 18:00:00'::timestamp),
    
    (6, '22100002', '2026-03-06 11:30:00'::timestamp, '2026-03-06 11:30:00'::timestamp),
    (6, '22100003', '2026-03-06 12:00:00'::timestamp, '2026-03-06 12:00:00'::timestamp),
    (7, '22100001', '2026-03-07 16:00:00'::timestamp, '2026-03-07 16:00:00'::timestamp),
    (8, '22100009', '2026-03-08 09:45:00'::timestamp, '2026-03-08 09:45:00'::timestamp),
    (9, '22100010', '2026-03-08 14:00:00'::timestamp, '2026-03-08 14:00:00'::timestamp),
    (10, '22100001', '2026-03-08 17:30:00'::timestamp, '2026-03-08 17:30:00'::timestamp)
) r(post_num, mssv, created_at, updated_at)
WHERE p.post_num = r.post_num;

-- Insert sample comments using actual post IDs from database
INSERT INTO comments (id, post_id, mssv, content, parent_comment_id, like_count, reply_count, created_at, updated_at) 
SELECT 
    c.comment_id,
    p.id,
    c.mssv,
    c.content,
    c.parent_comment_id,
    c.like_count,
    c.reply_count,
    c.created_at,
    c.updated_at
FROM (
    SELECT ROW_NUMBER() OVER (ORDER BY created_at) as post_num, id FROM posts
) p
CROSS JOIN (
    VALUES 
    -- Comments on Post 1
    (1, '650e8400-e29b-41d4-a716-446655440001'::uuid, '22100002', 
    'Cảm ơn bạn đã chia sẻ! Mình cũng đang học Spring Boot và thấy làm project thực tế thực sự hiệu quả hơn nhiều so với chỉ đọc lý thuyết.', 
    NULL::uuid, 2, 0, '2026-03-01 08:20:00'::timestamp, '2026-03-01 08:20:00'::timestamp),

    (1, '650e8400-e29b-41d4-a716-446655440002'::uuid, '22100003', 
    'Bạn có thể recommend một số project thực tế để newbie như mình bắt đầu không?', 
    NULL::uuid, 1, 1, '2026-03-01 08:35:00'::timestamp, '2026-03-01 08:35:00'::timestamp),

    -- Reply to comment 2
    (1, '650e8400-e29b-41d4-a716-446655440003'::uuid, '22100001', 
    'Mình suggest bắt đầu với TODO app, rồi blog cá nhân, sau đó là e-commerce đơn giản. Từ từ tăng độ phức tạp nhé!', 
    '650e8400-e29b-41d4-a716-446655440002'::uuid, 3, 0, '2026-03-01 09:10:00'::timestamp, '2026-03-01 09:10:00'::timestamp),

    (1, '650e8400-e29b-41d4-a716-446655440004'::uuid, '22100004', 
    'Documentation thực sự quan trọng! Mình từng chỉ xem tutorial và gặp rất nhiều khó khăn khi debug.', 
    NULL::uuid, 1, 0, '2026-03-01 09:15:00'::timestamp, '2026-03-01 09:15:00'::timestamp),

    -- Comments on Post 2
    (2, '650e8400-e29b-41d4-a716-446655440005'::uuid, '22100001', 
    'Thư viện UIT thực sự là nơi lý tưởng để học tập. Mình thường đến từ 7h sáng để có chỗ ngồi tốt.', 
    NULL::uuid, 1, 0, '2026-03-02 09:50:00'::timestamp, '2026-03-02 09:50:00'::timestamp),

    (2, '650e8400-e29b-41d4-a716-446655440006'::uuid, '22100003', 
    'Bạn thường ngồi tầng mấy? Mình thích tầng 3 vì yên tĩnh và có view đẹp.', 
    NULL::uuid, 0, 0, '2026-03-02 10:05:00'::timestamp, '2026-03-02 10:05:00'::timestamp),

    -- Comments on Post 3
    (3, '650e8400-e29b-41d4-a716-446655440007'::uuid, '22100001', 
    'Video rất hữu ích! Mình đã follow theo và setup được Docker thành công. Cảm ơn bạn nhiều!', 
    NULL::uuid, 4, 0, '2026-03-03 14:40:00'::timestamp, '2026-03-03 14:40:00'::timestamp),

    (3, '650e8400-e29b-41d4-a716-446655440008'::uuid, '22100002', 
    'Bạn có thể làm thêm video về Docker Compose không? Mình đang cần tìm hiểu phần này.', 
    NULL::uuid, 2, 1, '2026-03-03 15:10:00'::timestamp, '2026-03-03 15:10:00'::timestamp),

    -- Reply to comment 8
    (3, '650e8400-e29b-41d4-a716-446655440009'::uuid, '22100003', 
    'Mình cũng đang plan làm video về Docker Compose. Sẽ upload trong tuần tới nhé!', 
    '650e8400-e29b-41d4-a716-446655440008'::uuid, 1, 0, '2026-03-03 16:00:00'::timestamp, '2026-03-03 16:00:00'::timestamp),

    -- Comments on Post 4
    (4, '650e8400-e29b-41d4-a716-446655440010'::uuid, '22100005', 
    'Mình có hứng thú tham gia! Mình có kinh nghiệm về frontend với React. Inbox mình nhé!', 
    NULL::uuid, 2, 0, '2026-03-04 10:35:00'::timestamp, '2026-03-04 10:35:00'::timestamp),

    (4, '650e8400-e29b-41d4-a716-446655440011'::uuid, '22100006', 
    'Count me in! Mình handle được backend với Node.js và database design.', 
    NULL::uuid, 1, 0, '2026-03-04 11:05:00'::timestamp, '2026-03-04 11:05:00'::timestamp),

    -- Comments on Post 8
    (8, '650e8400-e29b-41d4-a716-446655440012'::uuid, '22100009', 
    'Dijkstra fail với cạnh âm vì nó assume rằng một khi đã tìm được đường ngắn nhất đến một đỉnh thì không thể cải thiện thêm được nữa. Nhưng với cạnh âm, có thể có đường đi qua cạnh âm đó lại ngắn hơn.', 
    NULL::uuid, 5, 0, '2026-03-08 09:30:00'::timestamp, '2026-03-08 09:30:00'::timestamp),

    (8, '650e8400-e29b-41d4-a716-446655440013'::uuid, '22100010', 
    'Ví dụ: A->B (cost 1), B->C (cost -3), A->C (cost 2). Dijkstra sẽ chọn A->C trước vì cost thấp hơn A->B, nhưng thực ra A->B->C (cost -2) mới là ngắn nhất.', 
    NULL::uuid, 3, 1, '2026-03-08 09:50:00'::timestamp, '2026-03-08 09:50:00'::timestamp),

    -- Reply to comment 13
    (8, '650e8400-e29b-41d4-a716-446655440014'::uuid, '22100007', 
    'Cảm ơn bạn! Giải thích rất rõ ràng. Vậy trong trường hợp này mình nên dùng Bellman-Ford đúng không?', 
    '650e8400-e29b-41d4-a716-446655440013'::uuid, 2, 0, '2026-03-08 10:15:00'::timestamp, '2026-03-08 10:15:00'::timestamp),

    -- More comments on other posts
    (9, '650e8400-e29b-41d4-a716-446655440015'::uuid, '22100010', 
    'App trông rất professional! UI/UX design rất đẹp. Bạn có plan publish lên store không?', 
    NULL::uuid, 1, 0, '2026-03-08 14:00:00'::timestamp, '2026-03-08 14:00:00'::timestamp),

    (10, '650e8400-e29b-41d4-a716-446655440016'::uuid, '22100001', 
    'Setup workspace rất clean và professional! Bạn dùng monitor nào vậy? Mình cũng đang tìm mua thêm màn hình phụ.', 
    NULL::uuid, 0, 0, '2026-03-08 17:15:00'::timestamp, '2026-03-08 17:15:00'::timestamp)
) c(post_num, comment_id, mssv, content, parent_comment_id, like_count, reply_count, created_at, updated_at)
WHERE p.post_num = c.post_num;

-- Insert sample comment reactions (likes on comments)
INSERT INTO comment_reactions (id, comment_id, mssv, created_at, updated_at) VALUES
-- Reactions on comment 1 (2 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440001', '22100003', '2026-03-01 08:25:00', '2026-03-01 08:25:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440001', '22100004', '2026-03-01 08:40:00', '2026-03-01 08:40:00'),

-- Reactions on comment 2 (1 like)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440002', '22100005', '2026-03-01 08:50:00', '2026-03-01 08:50:00'),

-- Reactions on comment 3 (3 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440003', '22100002', '2026-03-01 09:20:00', '2026-03-01 09:20:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440003', '22100004', '2026-03-01 09:30:00', '2026-03-01 09:30:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440003', '22100005', '2026-03-01 10:00:00', '2026-03-01 10:00:00'),

-- Reactions on comment 4 (1 like)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440004', '22100002', '2026-03-01 09:25:00', '2026-03-01 09:25:00'),

-- Reactions on comment 5 (1 like)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440005', '22100003', '2026-03-02 10:00:00', '2026-03-02 10:00:00'),

-- Reactions on comment 7 (4 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440007', '22100002', '2026-03-03 14:50:00', '2026-03-03 14:50:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440007', '22100004', '2026-03-03 15:20:00', '2026-03-03 15:20:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440007', '22100005', '2026-03-03 16:00:00', '2026-03-03 16:00:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440007', '22100008', '2026-03-03 17:30:00', '2026-03-03 17:30:00'),

-- Reactions on comment 8 (2 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440008', '22100001', '2026-03-03 15:30:00', '2026-03-03 15:30:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440008', '22100004', '2026-03-03 16:15:00', '2026-03-03 16:15:00'),

-- Reactions on comment 9 (1 like)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440009', '22100002', '2026-03-03 16:30:00', '2026-03-03 16:30:00'),

-- Reactions on comment 10 (2 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440010', '22100004', '2026-03-04 10:45:00', '2026-03-04 10:45:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440010', '22100006', '2026-03-04 11:20:00', '2026-03-04 11:20:00'),

-- Reactions on comment 11 (1 like)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440011', '22100005', '2026-03-04 11:15:00', '2026-03-04 11:15:00'),

-- Reactions on comment 12 (5 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440012', '22100001', '2026-03-08 09:40:00', '2026-03-08 09:40:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440012', '22100002', '2026-03-08 10:00:00', '2026-03-08 10:00:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440012', '22100003', '2026-03-08 10:30:00', '2026-03-08 10:30:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440012', '22100007', '2026-03-08 11:00:00', '2026-03-08 11:00:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440012', '22100008', '2026-03-08 11:30:00', '2026-03-08 11:30:00'),

-- Reactions on comment 13 (3 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440013', '22100001', '2026-03-08 10:00:00', '2026-03-08 10:00:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440013', '22100009', '2026-03-08 10:20:00', '2026-03-08 10:20:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440013', '22100011', '2026-03-08 11:00:00', '2026-03-08 11:00:00'),

-- Reactions on comment 14 (2 likes)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440014', '22100009', '2026-03-08 10:30:00', '2026-03-08 10:30:00'),
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440014', '22100010', '2026-03-08 11:15:00', '2026-03-08 11:15:00'),

-- Reactions on comment 15 (1 like)
(gen_random_uuid(), '650e8400-e29b-41d4-a716-446655440015', '22100008', '2026-03-08 14:15:00', '2026-03-08 14:15:00');

-- Insert sample shares using actual post IDs from database
INSERT INTO shares (id, post_id, mssv, created_at, updated_at) 
SELECT 
    gen_random_uuid(),
    p.id,
    s.mssv,
    s.created_at,
    s.updated_at
FROM (
    SELECT ROW_NUMBER() OVER (ORDER BY created_at) as post_num, id FROM posts
) p
CROSS JOIN (
    VALUES 
    -- Shares for popular posts
    (1, '22100002', '2026-03-01 08:25:00'::timestamp, '2026-03-01 08:25:00'::timestamp),
    (1, '22100003', '2026-03-01 09:30:00'::timestamp, '2026-03-01 09:30:00'::timestamp),
    (1, '22100005', '2026-03-01 11:00:00'::timestamp, '2026-03-01 11:00:00'::timestamp),
    
    (3, '22100001', '2026-03-03 15:30:00'::timestamp, '2026-03-03 15:30:00'::timestamp),
    (3, '22100004', '2026-03-03 17:00:00'::timestamp, '2026-03-03 17:00:00'::timestamp),
    
    (6, '22100002', '2026-03-06 12:30:00'::timestamp, '2026-03-06 12:30:00'::timestamp),
    (6, '22100007', '2026-03-06 14:00:00'::timestamp, '2026-03-06 14:00:00'::timestamp),
    
    (7, '22100001', '2026-03-07 16:15:00'::timestamp, '2026-03-07 16:15:00'::timestamp),
    (7, '22100008', '2026-03-07 17:30:00'::timestamp, '2026-03-07 17:30:00'::timestamp),
    (7, '22100009', '2026-03-07 18:00:00'::timestamp, '2026-03-07 18:00:00'::timestamp),
    
    (9, '22100010', '2026-03-08 14:30:00'::timestamp, '2026-03-08 14:30:00'::timestamp),
    (9, '22100002', '2026-03-08 15:00:00'::timestamp, '2026-03-08 15:00:00'::timestamp),
    
    (11, '22100001', '2026-03-08 19:30:00'::timestamp, '2026-03-08 19:30:00'::timestamp),
    (11, '22100003', '2026-03-08 20:00:00'::timestamp, '2026-03-08 20:00:00'::timestamp),
    (11, '22100005', '2026-03-08 20:15:00'::timestamp, '2026-03-08 20:15:00'::timestamp),
    
    (13, '22100004', '2026-03-08 22:00:00'::timestamp, '2026-03-08 22:00:00'::timestamp),
    (13, '22100006', '2026-03-08 22:30:00'::timestamp, '2026-03-08 22:30:00'::timestamp)
) s(post_num, mssv, created_at, updated_at)
WHERE p.post_num = s.post_num;

-- Update post counts based on inserted data
UPDATE posts SET 
    like_count = (SELECT COUNT(*) FROM reactions WHERE reactions.post_id = posts.id),
    comment_count = (SELECT COUNT(*) FROM comments WHERE comments.post_id = posts.id),
    share_count = (SELECT COUNT(*) FROM shares WHERE shares.post_id = posts.id);
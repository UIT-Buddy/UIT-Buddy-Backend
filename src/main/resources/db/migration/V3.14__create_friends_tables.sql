CREATE TABLE friend_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_mssv VARCHAR(12) NOT NULL,
    receiver_mssv VARCHAR(12) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, ACCEPTED, REJECTED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    CONSTRAINT fk_friend_request_sender FOREIGN KEY (sender_mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT fk_friend_request_receiver FOREIGN KEY (receiver_mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE
);

CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user1_mssv VARCHAR(12) NOT NULL,
    user2_mssv VARCHAR(12) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    CONSTRAINT fk_friendship_user1 FOREIGN KEY (user1_mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_user2 FOREIGN KEY (user2_mssv) 
        REFERENCES students(mssv) ON DELETE CASCADE,
    CONSTRAINT uk_friendship UNIQUE (user1_mssv, user2_mssv),
    CONSTRAINT chk_friendship_users CHECK (user1_mssv < user2_mssv)
);

-- Indexes
CREATE INDEX idx_friend_request_sender ON friend_requests(sender_mssv);
CREATE INDEX idx_friend_request_receiver ON friend_requests(receiver_mssv);
CREATE INDEX idx_friend_request_status ON friend_requests(status);

CREATE INDEX idx_friendship_user1 ON friendships(user1_mssv);
CREATE INDEX idx_friendship_user2 ON friendships(user2_mssv);


CREATE INDEX idx_friend_req_receiver_pagination 
ON friend_requests(receiver_mssv, status, created_at DESC, id DESC);
CREATE INDEX idx_friend_req_sender_pagination 
ON friend_requests(sender_mssv, status, created_at DESC, id DESC);

package com.uit.buddy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {
    POST_LIKE("Tương tác mới", "%s đã thích bài viết của bạn.", "POST_LIKE"),
    POST_COMMENT("Bình luận mới", "%s đã bình luận: %s", "POST_COMMENT"),
    POST_SHARE("Chia sẻ mới", "%s đã chia sẻ bài viết của bạn.", "POST_SHARE"),
    FRIEND_REQUEST_RECEIVED("Lời mời kết bạn", "%s đã gửi lời mời kết bạn", "FRIEND_REQUEST"),
    FRIEND_REQUEST_ACCEPTED("Chấp nhận kết bạn", "%s đã chấp nhận lời mời kết bạn của bạn", "FRIEND_ACCEPTED");

    private final String title;
    private final String contentTemplate;
    private final String type;

    public static final String COUNT_EXTRACT_REGEX = " và (\\d+) người khác";
    public static final String MSG_SINGLE = "%s đã %s bài viết của bạn";
    public static final String MSG_MULTIPLE = "%s và %d người khác đã %s bài viết của bạn";

    public String formatContent(Object... args) {
        return String.format(this.contentTemplate, args);
    }
}

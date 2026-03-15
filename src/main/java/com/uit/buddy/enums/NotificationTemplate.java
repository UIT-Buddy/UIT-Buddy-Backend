package com.uit.buddy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {
    POST_LIKE("Tương tác mới", "%s đã thích bài viết của bạn.", "POST_LIKE"),
    POST_COMMENT("Bình luận mới", "%s đã bình luận: %s", "POST_COMMENT"),
    POST_SHARE("Chia sẻ mới", "%s đã chia sẻ bài viết của bạn.", "POST_SHARE");

    private final String title;
    private final String contentTemplate;
    private final String type;

    public String formatContent(Object... args) {
        return String.format(this.contentTemplate, args);
    }
}

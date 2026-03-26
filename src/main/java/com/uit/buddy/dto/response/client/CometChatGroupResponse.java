package com.uit.buddy.dto.response.client;

import java.util.List;

public record CometChatGroupResponse(List<GroupData> data) {
    public record GroupData(String guid, String name, String icon, String type) {
    }
}

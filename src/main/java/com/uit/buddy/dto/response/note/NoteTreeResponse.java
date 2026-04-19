package com.uit.buddy.dto.response.note;

import java.util.List;

public record NoteTreeResponse(List<NoteNodeTreeResponse> nodes, List<NoteSummaryResponse> uncategorizedNotes) {
}

package com.uit.buddy.util;

import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public class CursorUtils {
    private static final String DELIMITER = "|";

    public record CursorContents(LocalDateTime timestamp, UUID id) {
    }

    public static String encode(LocalDateTime timestamp, UUID id) {
        if (timestamp == null || id == null)
            return null;
        String raw = timestamp.toString() + DELIMITER + id.toString();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static CursorContents decode(String cursor) {
        if (cursor == null || cursor.isBlank())
            return null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = decodedString.split("\\" + DELIMITER);
            return new CursorContents(LocalDateTime.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (Exception e) {
            throw new SystemException(SystemErrorCode.INVALID_CURSOR_FORMAT);
        }
    }
}

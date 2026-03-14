package com.uit.buddy.util;

import com.uit.buddy.constant.AppConstants;

public class TextUtils {

    public static String truncate(String text) {

        int SNIPPET_LENGTH = AppConstants.SNIPPET_LENGTH;

        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.length() <= SNIPPET_LENGTH) {
            return text;
        }
        return text.substring(0, SNIPPET_LENGTH) + "...";
    }
}

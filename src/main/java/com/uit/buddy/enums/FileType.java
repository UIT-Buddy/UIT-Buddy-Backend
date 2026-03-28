package com.uit.buddy.enums;

import lombok.Getter;

@Getter
public enum FileType {
    IMAGE, VIDEO, WORD, EXCEL, PPT, OTHER;

    public String getFormattedMaxSize(long sizeInBytes) {
        return (sizeInBytes / (1024 * 1024)) + "MB";
    }
}

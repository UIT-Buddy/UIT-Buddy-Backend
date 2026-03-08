package com.uit.buddy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudinary")
@Getter
@Setter
public class CloudinaryProperties {

    private String defaultAvatarUrl;
    private long maxImageSize;
    private long maxVideoSize;
    private String[] allowedImageTypes;
    private String[] allowedVideoTypes;
    private int avatarSize;
    private int thumbnailSize;
    private int postImageWidth;
    private int postImageHeight;
}

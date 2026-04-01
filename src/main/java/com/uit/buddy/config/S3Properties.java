package com.uit.buddy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Getter
@Setter
public class S3Properties {

    private String bucketName;
    private String region;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String publicBaseUrl;
    private String defaultAvatarUrl;
    private String[] allowedImageTypes;
    private String[] allowedVideoTypes;
}

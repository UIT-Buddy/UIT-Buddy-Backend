package com.uit.buddy.config;

import java.net.http.HttpClient;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestClientConfig {

    @Value("${app.uit.api-url}")
    private String moodleBaseUrl;

    @Value("${app.cometchat.api-url}")
    private String cometChatBaseUrl;

    @Value("${MOODLE_CONNECT_TIMEOUT:5000}")
    private int moodleConnectTimeout;

    @Value("${MOODLE_READ_TIMEOUT:30000}")
    private int moodleReadTimeout;

    @Value("${COMETCHAT_CONNECT_TIMEOUT:2000}")
    private int cometChatConnectTimeout;

    @Value("${COMETCHAT_READ_TIMEOUT:10000}")
    private int cometChatReadTimeout;

    @Value("${DEFAULT_CONNECT_TIMEOUT:5000}")
    private int defaultConnectTimeout;

    @Value("${DEFAULT_READ_TIMEOUT:10000}")
    private int defaultReadTimeout;

    @Bean
    public Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient moodleClient() {
        return RestClient.builder().baseUrl(moodleBaseUrl)
                .requestFactory(createFactory(moodleConnectTimeout, moodleReadTimeout)).build();
    }

    @Bean
    public RestClient cometChatClient() {
        return RestClient.builder().baseUrl(cometChatBaseUrl)
                .requestFactory(createFactory(cometChatConnectTimeout, cometChatReadTimeout)).build();
    }

    @Bean
    @Primary
    public RestClient restClient() {
        return RestClient.builder().requestFactory(createFactory(defaultConnectTimeout, defaultReadTimeout)).build();
    }

    private JdkClientHttpRequestFactory createFactory(int connectTimeout, int readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectTimeout)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return factory;
    }
}

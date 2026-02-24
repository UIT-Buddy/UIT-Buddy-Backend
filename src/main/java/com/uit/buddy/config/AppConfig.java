package com.uit.buddy.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Locale;
import java.util.TimeZone;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public LocaleResolver localResolver(@Value("${app.default-locale:vi}") final String defaultLocale,
            @Value("${app.default-timezone:Asia/Ho_Chi_Minh}") final String defaultTimezone) {
        AcceptHeaderLocaleResolver localResolver = new AcceptHeaderLocaleResolver();
        localResolver.setDefaultLocale(new Locale.Builder().setLanguage(defaultLocale).build());
        TimeZone.setDefault(TimeZone.getTimeZone(defaultTimezone));
        return localResolver;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (body != null && body.length > 0) {
                log.debug("=== [OUTBOUND REQUEST] ===");
                log.debug("Method: {}, URI: {}", request.getMethod(), request.getURI());
                log.debug("Body: {}", new String(body, UTF_8));
                log.debug("==========================");
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}

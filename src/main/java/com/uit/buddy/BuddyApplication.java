package com.uit.buddy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class BuddyApplication {
    @Value("${app.cometchat.app-platform}")
    public static String test;
    static void main(String[] args) {
        System.out.println(test);
        SpringApplication.run(BuddyApplication.class, args);
    }
}

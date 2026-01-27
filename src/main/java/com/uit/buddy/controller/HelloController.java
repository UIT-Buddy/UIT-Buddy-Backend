package com.uit.buddy.controller;

import com.uit.buddy.controller.base.ResponseFactory;
import com.uit.buddy.dto.base.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloController {

    private final ResponseFactory responseFactory;

    @GetMapping("/hello")
    public ResponseEntity<SuccessResponse> hello() {
        return responseFactory.success("Hello from UIT-Buddy Backend!");
    }

    @GetMapping("/health")
    public ResponseEntity<SuccessResponse> health() {
        return responseFactory.success("Application is running!");
    }
}

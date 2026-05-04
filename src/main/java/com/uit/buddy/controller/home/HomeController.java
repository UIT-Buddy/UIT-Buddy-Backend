package com.uit.buddy.controller.home;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.response.home.HomepageResponse;
import com.uit.buddy.service.home.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Home", description = "Homepage data APIs")
public class HomeController extends AbstractBaseController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "Get homepage data", description = "Aggregate student info, classes today, unread notifications, and incoming deadlines")
    public ResponseEntity<SingleResponse<HomepageResponse>> getHomepageData(@AuthenticationPrincipal String mssv,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        log.info("[GET /api/home] Getting homepage data for mssv: {}, page: {}, size: {}", mssv, page, limit);
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        HomepageResponse response = homeService.getHomepageData(mssv, pageable);
        return successSingle(response, "Homepage data retrieved successfully");
    }
}

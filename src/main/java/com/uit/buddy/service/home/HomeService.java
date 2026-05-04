package com.uit.buddy.service.home;

import com.uit.buddy.dto.response.home.HomepageResponse;
import org.springframework.data.domain.Pageable;

public interface HomeService {
    HomepageResponse getHomepageData(String mssv, Pageable pageable);
}

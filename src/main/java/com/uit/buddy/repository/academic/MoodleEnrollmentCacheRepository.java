package com.uit.buddy.repository.academic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.constant.ScheduleConstant;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class MoodleEnrollmentCacheRepository {

    private static final String KEY_PREFIX = "moodle:enrollment:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MoodleEnrollmentCacheRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String buildKey(String mssv) {
        return KEY_PREFIX + mssv;
    }

    public void save(String mssv, Long userId, List<EnrolledCourseResponse> enrolledCourses) {
        try {
            CachedEnrollment cache = new CachedEnrollment(userId, enrolledCourses);
            String json = objectMapper.writeValueAsString(cache);
            redisTemplate.opsForValue().set(buildKey(mssv), json, ScheduleConstant.MOODLE_ENROLLMENT_CACHE_TTL_SECONDS,
                    TimeUnit.SECONDS);
            log.debug("[MoodleEnrollmentCache] Cached enrollment for mssv={}", mssv);
        } catch (JsonProcessingException e) {
            log.warn("[MoodleEnrollmentCache] Failed to serialize enrollment for mssv={}", mssv, e);
        }
    }

    public Optional<CachedEnrollment> findByMssv(String mssv) {
        String key = buildKey(mssv);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            log.debug("[MoodleEnrollmentCache] Cache miss for mssv={}", mssv);
            return Optional.empty();
        }
        try {
            CachedEnrollment cache = objectMapper.readValue(json, CachedEnrollment.class);
            log.debug("[MoodleEnrollmentCache] Cache hit for mssv={}", mssv);
            return Optional.of(cache);
        } catch (JsonProcessingException e) {
            log.warn("[MoodleEnrollmentCache] Failed to deserialize enrollment for mssv={}", mssv, e);
            return Optional.empty();
        }
    }

    public void evict(String mssv) {
        redisTemplate.delete(buildKey(mssv));
        log.debug("[MoodleEnrollmentCache] Evicted cache for mssv={}", mssv);
    }

    public record CachedEnrollment(Long userId, List<EnrolledCourseResponse> enrolledCourses) {
    }
}

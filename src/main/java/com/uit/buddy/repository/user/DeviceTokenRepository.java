package com.uit.buddy.repository.user;

import com.uit.buddy.entity.user.DeviceToken;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findAllByMssv(String mssv);

    @Transactional
    void deleteByFcmToken(String fcmToken);

    @Transactional
    void deleteByMssv(String mssv);

    @Modifying
    @Query(value = """
            INSERT INTO device_tokens (id, mssv, fcm_token, created_at, updated_at)
            VALUES (gen_random_uuid(), :mssv, :token, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (mssv, fcm_token)
            DO UPDATE SET updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void upsertToken(@Param("mssv") String mssv, @Param("token") String token);
}

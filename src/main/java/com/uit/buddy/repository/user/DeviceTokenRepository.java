package com.uit.buddy.repository.user;

import com.uit.buddy.entity.user.DeviceToken;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    @Query("SELECT dt.fcmToken FROM DeviceToken dt WHERE dt.mssv = :mssv")
    List<String> findAllTokensByMssv(@Param("mssv") String mssv);

    @Transactional
    void deleteByFcmToken(String fcmToken);

    @Transactional
    int deleteByFcmTokenAndMssvNot(String fcmToken, String mssv);

    @Transactional
    void deleteByMssv(String mssv);

    @Transactional
    void deleteAllByFcmTokenIn(List<String> tokens);

    @Modifying
    @Query(value = """
            INSERT INTO device_tokens (id, mssv, fcm_token, created_at, updated_at)
            VALUES (gen_random_uuid(), :mssv, :token, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (mssv, fcm_token)
            DO UPDATE SET updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void upsertToken(@Param("mssv") String mssv, @Param("token") String token);
}

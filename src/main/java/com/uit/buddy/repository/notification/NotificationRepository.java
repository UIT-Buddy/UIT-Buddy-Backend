package com.uit.buddy.repository.notification;

import com.uit.buddy.entity.notification.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query(value = """
            SELECT * FROM notifications
            WHERE mssv = :mssv
              AND (CAST(:cursor AS timestamp) IS NULL OR created_at < :cursor)
            ORDER BY created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Notification> findByMssvWithCursor(@Param("mssv") String mssv, @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit);

    long countByStudentMssvAndIsReadFalse(String mssv);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.student.mssv = :mssv")
    void markAsRead(@Param("id") UUID id, @Param("mssv") String mssv);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.student.mssv = :mssv AND n.isRead = false")
    void markAllAsRead(@Param("mssv") String mssv);

    @Query("SELECT n FROM Notification n WHERE n.student.mssv = :mssv AND n.type = :type AND n.dataId = :dataId")
    Notification findByMssvAndTypeAndDataId(@Param("mssv") String mssv, @Param("type") String type,
            @Param("dataId") String dataId);
}

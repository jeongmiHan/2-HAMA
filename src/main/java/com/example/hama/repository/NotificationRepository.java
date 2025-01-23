package com.example.hama.repository;

import com.example.hama.model.Notification;
import com.example.hama.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	 // 읽음 여부로 알림 조회
    List<Notification> findByIsRead(boolean isRead);

    // 특정 사용자별 알림 조회
    List<Notification> findByUser(User user);

    // 특정 사용자별 읽지 않은 알림 조회
    List<Notification> findByUserAndIsRead(User user, boolean isRead);

    // 만료되지 않은 알림 조회
    List<Notification> findByUserAndIsExpiredFalse(User user);

    // 알림 내용으로 중복 확인
    boolean existsByContent(String content);
    
    List<Notification> findByUserAndIsReadAndIsExpiredFalse(User user, boolean isRead);
}

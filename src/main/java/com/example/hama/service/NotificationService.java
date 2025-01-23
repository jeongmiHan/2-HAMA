package com.example.hama.service;

import com.example.hama.model.Notification;
import com.example.hama.model.Events;
import com.example.hama.model.user.User;
import com.example.hama.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // 중복 알림 확인 메서드
    public boolean isNotificationExists(String content) {
        return notificationRepository.existsByContent(content);
    }

    // 사용자별 알림 생성
    public Notification createNotificationForUser(String content, User user, Events event) {
        Notification notification = new Notification(content, event);
        notification.setUser(user);
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotificationsByUser(User user) {
        return notificationRepository.findByUserAndIsExpiredFalse(user);
    }

    // 사용자별 읽지 않은 알림 가져오기
    public List<Notification> getUnreadValidNotificationsByUser(User user) {
        return notificationRepository.findByUserAndIsReadAndIsExpiredFalse(user, false);
    }
    
    // 사용자별 만료되지 않은 알림 가져오기
    public List<Notification> getValidNotificationsByUser(User user) {
        return notificationRepository.findByUserAndIsExpiredFalse(user); // 만료되지 않은 알림만 조회
    }

    // 사용자별 알림 읽음 처리
    public Notification markAsReadByUser(Long id, User user) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (!notification.getUser().getUserId().equals(user.getUserId())) {
                throw new RuntimeException("사용자가 소유하지 않은 알림에 접근하려고 했습니다.");
            }
            notification.setRead(true); // 읽음 처리
            return notificationRepository.save(notification);
        }
        throw new RuntimeException("알림을 찾을 수 없습니다.");
    }

    // 사용자별 알림 삭제
    public void deleteNotificationByUser(Long id, User user) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (!notification.getUser().getUserId().equals(user.getUserId())) {
                throw new RuntimeException("사용자가 소유하지 않은 알림을 삭제하려고 했습니다.");
            }
            notificationRepository.delete(notification);
        } else {
            throw new RuntimeException("알림을 찾을 수 없습니다.");
        }
    }

    // 만료된 알림 상태 업데이트
    public void updateExpiredNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification notification : notifications) {
            if (!notification.isExpired()) {
                // 이벤트 시작 시간을 기준으로 만료 여부 결정
                if (notification.getEvent().getEventDateStart().isBefore(LocalDateTime.now())) {
                    notification.setExpired(true);
                    notificationRepository.save(notification);
                }
            }
        }
    }
}
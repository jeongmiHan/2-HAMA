package com.example.hama.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.hama.model.Events;
import com.example.hama.model.user.User;
import com.example.hama.repository.EventRepository;
import com.example.hama.service.NotificationService;

@Component
public class CalendarNotificationScheduler {

    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    public CalendarNotificationScheduler(EventRepository eventRepository, NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "*/10 * * * * *") // 매 분 0초에 실행
    public void generateNotificationsFromCalendar() {
        System.out.println("스케줄러 실행 중: " + LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveHourLater = now.plusHours(5);

        // 현재 시각과 5시간 후 사이에 시작하는 이벤트 조회
        List<Events> upcomingEvents = eventRepository.findByEventDateStartBetween(now, fiveHourLater);

        if (!upcomingEvents.isEmpty()) {
            for (Events event : upcomingEvents) {
                User user = event.getUser(); // 이벤트와 연결된 사용자
                String content = "다가오는 일정: " + event.getCd_title() + " (" + event.getEventDateStart() + ")";

                // 기존 알림이 있다면 수정, 없으면 새로 생성
                notificationService.createOrUpdateNotificationForUser(content, user, event);

                System.out.println("알림 업데이트 또는 생성: " + content + " for user: " + user.getUserId());
            }
        } else {
            System.out.println("알림 생성할 이벤트가 없습니다.");
        }
    }


    @Scheduled(cron = "0 * * * * *") // 매 분마다 만료된 알림 처리
    public void expireOldNotifications() {
        notificationService.updateExpiredNotifications();
        System.out.println("만료된 알림 업데이트 완료: " + LocalDateTime.now());
    }
}
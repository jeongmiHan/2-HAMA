package com.example.hama.controller.calendar;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.Events;
import com.example.hama.model.Notification;
import com.example.hama.model.user.User;
import com.example.hama.repository.EventRepository;
import com.example.hama.service.NotificationService;
import com.example.hama.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private EventRepository eventRepository; 

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("SecurityContext 인증 객체: {}", authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUser();
            } else if (principal instanceof DefaultOAuth2User) {
                String providerUserId = ((DefaultOAuth2User) principal).getAttribute("sub");
                return userService.findUserByProviderUserId(providerUserId);
            }
        }
        log.warn("SecurityContext에서 인증된 사용자를 찾을 수 없습니다.");
        return null;
    }

    // 읽지 않은 유효한 알림 목록 조회
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadValidNotifications() {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("인증되지 않은 사용자가 읽지 않은 알림을 조회하려고 시도했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("사용자 {}의 읽지 않은 유효한 알림을 조회합니다.", loggedInUser.getUserId());
        List<Notification> unreadNotifications = notificationService.getUnreadValidNotificationsByUser(loggedInUser);
        return ResponseEntity.ok(unreadNotifications);
    }

    // 만료되지 않은 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<Notification>> getValidNotifications() {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("인증되지 않은 사용자가 모든 알림을 조회하려고 시도했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("사용자 {}의 만료되지 않은 알림을 조회합니다.", loggedInUser.getUserId());
        List<Notification> validNotifications = notificationService.getValidNotificationsByUser(loggedInUser);
        return ResponseEntity.ok(validNotifications); // 만료되지 않은 알림만 반환
    }

    @PostMapping
    public ResponseEntity<Notification> createOrUpdateNotification(@RequestParam String content, @RequestParam Long eventId) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("인증되지 않은 사용자가 알림을 생성 또는 수정하려고 시도했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("사용자 {}가 새로운 알림을 생성 또는 업데이트합니다. 내용: {}", loggedInUser.getUserId(), content);

        // eventId에 해당하는 이벤트 찾기 (EventService 없이 EventRepository 직접 사용)
        Events event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            log.warn("eventId {}에 해당하는 이벤트를 찾을 수 없습니다.", eventId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 기존 알림이 있으면 업데이트, 없으면 새로 생성
        Notification createdOrUpdatedNotification = notificationService.createOrUpdateNotificationForUser(content, loggedInUser, event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrUpdatedNotification);
    }

    // 특정 알림 읽음 처리
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable("id") Long id) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("인증되지 않은 사용자가 알림을 읽음 처리하려고 시도했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("사용자 {}가 알림 {}을 읽음 처리합니다.", loggedInUser.getUserId(), id);
        Notification updatedNotification = notificationService.markAsReadByUser(id, loggedInUser);
        return ResponseEntity.ok(updatedNotification);
    }

    // 특정 알림 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("인증되지 않은 사용자가 알림을 삭제하려고 시도했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("사용자 {}가 알림 {}을 삭제합니다.", loggedInUser.getUserId(), id);
        notificationService.deleteNotificationByUser(id, loggedInUser);
        return ResponseEntity.noContent().build();
    }
}

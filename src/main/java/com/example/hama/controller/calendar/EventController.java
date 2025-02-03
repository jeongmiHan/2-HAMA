package com.example.hama.controller.calendar;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.Events;
import com.example.hama.model.Notification;
import com.example.hama.model.user.User;
import com.example.hama.repository.EventRepository;
import com.example.hama.repository.NotificationRepository;
import com.example.hama.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventController {

    @Autowired
    private EventRepository eventRepository;  // 이벤트 저장소

    @Autowired
    private UserService userService; // 사용자 서비스 주입
    
    @Autowired
    private NotificationRepository notificationRepository; // 알림 저장소 추가


    // 현재 인증된 사용자 정보를 가져오는 메서드
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("SecurityContext authentication: {}", authentication);  // 인증 정보 로그 출력
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUser(); // 기본 로그인 사용자 정보 반환
            } else if (principal instanceof DefaultOAuth2User) {
                String providerUserId = ((DefaultOAuth2User) principal).getAttribute("sub");
                return userService.findUserByProviderUserId(providerUserId);  // 소셜 사용자 정보 반환
            }
        }
        log.warn("No authenticated user found in SecurityContext."); // 인증된 사용자 없음
        return null; // 인증되지 않은 경우 null 반환
    }

    @GetMapping // GET 요청을 처리하여 모든 이벤트를 반환
    public ResponseEntity<List<Events>> getAllEvents() {
        User loggedInUser = getAuthenticatedUser(); // 인증된 사용자 가져오기
        if (loggedInUser == null) {
            log.warn("Unauthorized access to getAllEvents. No authenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 인증되지 않은 경우 401 반환
        }

        log.info("Fetching events for user: {}", loggedInUser.getUserId());
        List<Events> userEvents = eventRepository.findByUser(loggedInUser); // 사용자별 이벤트 조회
        return ResponseEntity.ok(userEvents);  // 이벤트 목록 반환
    }


    @PostMapping // POST 요청을 처리하여 새로운 이벤트를 생성
    public ResponseEntity<Events> createEvent(@RequestBody Events events) {
        User loggedInUser = getAuthenticatedUser();  // 인증된 사용자 가져오기
        if (loggedInUser == null) {
            log.warn("Unauthorized attempt to create event. No authenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 인증되지 않은 경우 401 반환
        }
        events.setUser(loggedInUser);  // 이벤트의 소유자를 설정
        log.info("Creating new event for user: {}. Event details: {}", loggedInUser.getUserId(), events);

        try {
            Events savedEvent = eventRepository.save(events);  // 이벤트 저장
            log.info("Event created successfully. Event ID: {}", savedEvent.getCalendar_id());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);  // 저장된 이벤트 반환
        } catch (Exception e) {
            log.error("Error saving event to database", e); // 에러 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 반환
        }
    }

    // 특정 이벤트 수정
    @PutMapping("/{calendar_id}")
    public ResponseEntity<Events> updateEvent(@PathVariable("calendar_id") Long calendar_id, @RequestBody Events updatedEvent) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("Unauthorized attempt to update event. No authenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Events existingEvent = eventRepository.findById(calendar_id).orElse(null);
        if (existingEvent == null) {
            log.warn("Event not found. Event ID: {}", calendar_id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!existingEvent.getUser().getUserId().equals(loggedInUser.getUserId())) {
            log.warn("User {} attempted to update event they do not own. Event ID: {}", loggedInUser.getUserId(), calendar_id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        existingEvent.setCd_title(updatedEvent.getCd_title());
        existingEvent.setEventDateStart(updatedEvent.getEventDateStart());
        existingEvent.setEventDateEnd(updatedEvent.getEventDateEnd());
        existingEvent.setCd_description(updatedEvent.getCd_description());
        existingEvent.setCd_color(updatedEvent.getCd_color());

        try {
            Events savedEvent = eventRepository.save(existingEvent);
            log.info("Event updated successfully. Event ID: {}", calendar_id);
            return ResponseEntity.ok(savedEvent);
        } catch (Exception e) {
            log.error("Error updating event in database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 특정 이벤트 삭제
    @DeleteMapping("/{calendar_id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("calendar_id") Long calendar_id) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("Unauthorized attempt to delete event. No authenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Events existingEvent = eventRepository.findById(calendar_id).orElse(null);
        if (existingEvent == null) {
            log.warn("Event not found. Event ID: {}", calendar_id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!existingEvent.getUser().getUserId().equals(loggedInUser.getUserId())) {
            log.warn("User {} attempted to delete event they do not own. Event ID: {}", loggedInUser.getUserId(), calendar_id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 알림 삭제 로직 추가
        List<Notification> notifications = notificationRepository.findByEvent(existingEvent);
        notificationRepository.deleteAll(notifications);

        try {
            eventRepository.delete(existingEvent);
            log.info("Event deleted successfully. Event ID: {}", calendar_id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting event from database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/dday")
    public ResponseEntity<Map<String, String>> calculateDday(@RequestParam(name = "eventId") Long eventId) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Events event = eventRepository.findById(eventId).orElse(null);
        if (event == null || !event.getUser().getUserId().equals(loggedInUser.getUserId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "이벤트를 찾을 수 없습니다."));
        }

        if (event.getEventDateStart() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "이벤트 시작 날짜가 설정되지 않았습니다."));
        }

        LocalDate today = LocalDate.now();
        LocalDate eventDate = event.getEventDateStart().toLocalDate(); // LocalDateTime -> LocalDate 변환
        long daysBetween = ChronoUnit.DAYS.between(today, eventDate);

        // ++++++ 수정된 로직: 과거는 "+", 미래는 "-", 당일은 "-" ++++++
        String d = "D";
        String sign = daysBetween == 0 ? "-" : (daysBetween > 0 ? "-" : "+");
        String days = daysBetween == 0 ? "Day" : String.valueOf(Math.abs(daysBetween));

        Map<String, String> response = Map.of(
            "d", d,
            "sign", sign,
            "days", days
        );

        return ResponseEntity.ok(response);
    }

}

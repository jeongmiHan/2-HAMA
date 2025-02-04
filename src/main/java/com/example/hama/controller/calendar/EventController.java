package com.example.hama.controller.calendar;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.Events;
import com.example.hama.model.Notification;
import com.example.hama.model.Pet;
import com.example.hama.model.user.User;
import com.example.hama.repository.EventRepository;
import com.example.hama.repository.NotificationRepository;
import com.example.hama.service.PetService;
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
    
    @Autowired
    private PetService petService;


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


    @PostMapping
    public ResponseEntity<Events> createEvent(@RequestBody Map<String, Object> eventData) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // JSON에서 데이터 추출
        String title = (String) eventData.get("cd_title");
        String description = (String) eventData.get("cd_description");
        String color = (String) eventData.get("cd_color");
        LocalDateTime eventStart = LocalDateTime.parse((String) eventData.get("eventDateStart"));
        LocalDateTime eventEnd = LocalDateTime.parse((String) eventData.get("eventDateEnd"));
        
        // petId를 JSON에서 직접 가져오기
        Long petId = eventData.get("petId") != null ? ((Number) eventData.get("petId")).longValue() : null;

        Events event = new Events(title, description, eventStart, eventEnd, color, loggedInUser, null);

        // 반려동물 설정
        if (petId != null) {
            Pet pet = petService.getPetById(petId);
            if (pet != null) {
                event.setPet(pet);
                log.info("petId {}에 해당하는 반려동물: {}", petId, pet.getPetName());
            } else {
                log.warn("petId {}에 해당하는 반려동물이 존재하지 않음", petId);
            }
        }

        Events savedEvent = eventRepository.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
    }

    @PutMapping("/{calendar_id}")
    public ResponseEntity<Events> updateEvent(@PathVariable("calendar_id") Long calendar_id, @RequestBody Map<String, Object> eventData) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Events existingEvent = eventRepository.findById(calendar_id).orElse(null);
        if (existingEvent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!existingEvent.getUser().getUserId().equals(loggedInUser.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 기존 이벤트 시작 시간 저장
        LocalDateTime oldEventStart = existingEvent.getEventDateStart();

        // JSON에서 새로운 데이터 가져오기
        String title = (String) eventData.get("cd_title");
        String description = (String) eventData.get("cd_description");
        String color = (String) eventData.get("cd_color");
        LocalDateTime newEventStart = LocalDateTime.parse((String) eventData.get("eventDateStart"));
        LocalDateTime eventEnd = LocalDateTime.parse((String) eventData.get("eventDateEnd"));

        existingEvent.setCd_title(title);
        existingEvent.setCd_description(description);
        existingEvent.setCd_color(color);
        existingEvent.setEventDateStart(newEventStart);
        existingEvent.setEventDateEnd(eventEnd);

        Events savedEvent = eventRepository.save(existingEvent);

        // 📌 5시간 이내인지 확인
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveHoursLater = now.plusHours(5);

        List<Notification> notifications = notificationRepository.findByEvent(existingEvent);

        for (Notification notification : notifications) {
            if (newEventStart.isAfter(fiveHoursLater)) {
                // 📌 5시간 이후라면 알림만 삭제 (이벤트는 유지됨)
                notificationRepository.delete(notification);
                log.info("이벤트 {}의 알림이 삭제됨 (새로운 일정이 5시간 이후)", calendar_id);
            } else {
                // 📌 5시간 이내면 알림 내용 업데이트
                String newContent = "수정된 일정: " + title + " (" + newEventStart + ")";
                notification.setContent(newContent);
                notificationRepository.save(notification);
                log.info("이벤트 {}의 알림 내용이 수정됨", calendar_id);
            }
        }

        return ResponseEntity.ok(savedEvent);
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

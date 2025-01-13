package com.example.hama.controller;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.Events;
import com.example.hama.model.user.User;
import com.example.hama.repository.EventRepository;
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
@RequestMapping("/api/events")
@Slf4j
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserService userService; // 사용자 서비스 주입

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("SecurityContext authentication: {}", authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUser();
            } else if (principal instanceof DefaultOAuth2User) {
                String providerUserId = ((DefaultOAuth2User) principal).getAttribute("sub");
                return userService.findUserByProviderUserId(providerUserId);
            }
        }
        log.warn("No authenticated user found in SecurityContext.");
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Events>> getAllEvents() {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("Unauthorized access to getAllEvents. No authenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Fetching events for user: {}", loggedInUser.getUserId());
        List<Events> userEvents = eventRepository.findByUser(loggedInUser);
        return ResponseEntity.ok(userEvents);
    }



    // 새로운 이벤트 추가
    @PostMapping
    public ResponseEntity<Events> createEvent(@RequestBody Events events) {
        User loggedInUser = getAuthenticatedUser();
        if (loggedInUser == null) {
            log.warn("Unauthorized attempt to create event. No authenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        events.setUser(loggedInUser);
        log.info("Creating new event for user: {}. Event details: {}", loggedInUser.getUserId(), events);

        try {
            Events savedEvent = eventRepository.save(events);
            log.info("Event created successfully. Event ID: {}", savedEvent.getCalendar_id());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
        } catch (Exception e) {
            log.error("Error saving event to database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
        existingEvent.setCd_event_date_start(updatedEvent.getCd_event_date_start());
        existingEvent.setCd_event_date_end(updatedEvent.getCd_event_date_end());
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

        try {
            eventRepository.delete(existingEvent);
            log.info("Event deleted successfully. Event ID: {}", calendar_id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting event from database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

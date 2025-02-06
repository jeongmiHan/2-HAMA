package com.example.hama.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.Events;
import com.example.hama.model.user.User;
import com.example.hama.model.Pet;

public interface EventRepository extends JpaRepository<Events, Long> {
    List<Events> findByUser(User user); // 기존 메서드 유지
    List<Events> findByUser_UserId(String userId); // User 엔티티의 userId 필드 기준으로 검색
    List<Events> findByEventDateStartBetween(LocalDateTime start, LocalDateTime end);
    List<Events> findByPet(Pet pet); // 반려동물 기반 조회 추가
}

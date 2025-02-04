package com.example.hama.model;

import com.example.hama.model.user.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar")
@Getter
@Setter
@NoArgsConstructor
public class Events {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calendar_id; // Primary Key

    private String cd_title; // 일정 제목

    @Column(name = "cd_event_date_start", columnDefinition = "DATETIME")
    private LocalDateTime eventDateStart; // 시작 시간

    @Column(name = "cd_event_date_end", columnDefinition = "DATETIME")
    private LocalDateTime eventDateEnd; // 종료 시간

    private String cd_color; // 이벤트 색상

    @Column(length = 500)
    private String cd_description; // 일정 설명

    @ManyToOne(fetch = FetchType.EAGER) // 다대일 관계 설정
    @JoinColumn(name = "user_id", nullable = false) // FK 컬럼 이름
    private User user; // 연결된 사용자
    
    
    @ManyToOne(fetch = FetchType.EAGER) // 이벤트와 반려동물 연결
    @JoinColumn(name = "pet_id", nullable = true) // FK 컬럼 (null 가능)
    private Pet pet; // 연결된 반려동물

    // 전체 필드를 포함하는 생성자
    public Events(String cd_title, String cd_description, LocalDateTime eventDateStart,
                  LocalDateTime eventDateEnd, String cd_color, User user, Pet pet) {
        this.cd_title = cd_title;
        this.cd_description = cd_description;
        this.eventDateStart = eventDateStart;
        this.eventDateEnd = eventDateEnd;
        this.cd_color = cd_color;
        this.user = user;
        this.pet = pet; // 반려동물 추가
    }
}

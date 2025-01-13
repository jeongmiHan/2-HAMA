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

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime cd_event_date_start; // 시작 시간

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime cd_event_date_end; // 종료 시간

    private String cd_color; // 이벤트 색상

    @Column(length = 500)
    private String cd_description; // 일정 설명

    @ManyToOne(fetch = FetchType.EAGER) // 다대일 관계 설정
    @JoinColumn(name = "user_id", nullable = false) // FK 컬럼 이름
    private User user; // 연결된 사용자

    // 전체 필드를 포함하는 생성자
    public Events(String cd_title, String cd_description, LocalDateTime cd_event_date_start,
                  LocalDateTime cd_event_date_end, String cd_color, User user) {
        this.cd_title = cd_title;
        this.cd_description = cd_description;
        this.cd_event_date_start = cd_event_date_start;
        this.cd_event_date_end = cd_event_date_end;
        this.cd_color = cd_color;
        this.user = user;
    }
}

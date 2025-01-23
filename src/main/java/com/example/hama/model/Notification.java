package com.example.hama.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.example.hama.model.Events;
import com.example.hama.model.user.User;

@Entity
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Long id;

    @Column(name = "noti_content")
    private String content;

    @Column(name = "noti_isRead")
    private boolean isRead;
    
    @Column(name = "noti_isExpired", nullable = false)
    private boolean isExpired = false; // 기본값: 만료되지 않음

    @Column(name = "noti_createdAt")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER) // 다대일 관계 설정
    @JoinColumn(name = "user_id", nullable = false) // FK 컬럼 이름
    private User user; // 연결된 사용자

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Events event; // 연결된 이벤트

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false; // 기본 읽음 상태는 false
    }

    public Notification(String content, Events event) {
        this.content = content;
        this.isRead = false;
        this.isExpired = false;
        this.createdAt = LocalDateTime.now();
        this.event = event;
    }
}
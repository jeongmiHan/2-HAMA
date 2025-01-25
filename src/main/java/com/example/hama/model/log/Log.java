package com.example.hama.model.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.hama.model.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data@Entity
public class Log {
	
    @Id
    @Column(name = "log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId; // 일지 아이디

    @ManyToOne(fetch = FetchType.EAGER) // 한 명이 여러 개의 일지를 쓰니까
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 사용자

    @Column
    private String logContent; // 내용

    @Column
    private LocalDateTime logCreatedDate; // 작성일

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LogLikes> logLikes = new ArrayList<>();

    @Column
    private int logComments = 0; // 댓글 수

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Log parent; // 부모 로그

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Log> children = new ArrayList<>(); // 자식 로그 목록

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // 부모 객체 직렬화
    private List<Reply> replies = new ArrayList<>(); // 댓글 목록

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // 순환 참조 방지
    private List<LogAttachedFile> logAttachedFiles = new ArrayList<>(); // 첨부파일 목록

    // Log -> LogUpdate 변환 메서드
    public static LogUpdate toLogUpdate(Log log) {
        LogUpdate logUpdate = new LogUpdate();
        logUpdate.setLogId(log.getLogId());
        logUpdate.setUser(log.getUser());
        logUpdate.setLogContent(log.getLogContent());
        logUpdate.setLogCreatedDate(log.getLogCreatedDate());
        logUpdate.setLogLikes(log.getLogLikes()); 
        return logUpdate;
    }

    // 첨부파일 추가 메서드
    public void addAttachedFile(LogAttachedFile logAttachedFile) {
        logAttachedFiles.add(logAttachedFile);
        logAttachedFile.setLog(this);
    }
    
}
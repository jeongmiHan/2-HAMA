package com.example.hama.dto;

import java.time.LocalDateTime;

import com.example.hama.model.log.Log;
import com.example.hama.util.SNSTime;

import lombok.Data;

@Data
public class LogDTO {
    private Long id;           // logId에 매핑
    private String content;    // logContent에 매핑
    private String timeAgo;    // logCreatedDate를 SNS 스타일로 변환하여 매핑
    private int likes;         // logLikes에 매핑
    private int comments;      // logComments에 매핑

    // Log 엔티티를 받아서 DTO로 변환하는 생성자
    public LogDTO(Log log) {
        this.id = log.getLogId();                              // ID
        this.content = log.getLogContent();                   // 내용
        this.timeAgo = SNSTime.getTimeAgo(log.getLogCreatedDate()); // SNS 스타일 시간
        this.likes = log.getLogLikes();                       // 좋아요 수
        this.comments = log.getLogComments();                 // 댓글 수
    }
}



package com.example.hama.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.hama.model.log.LogReply;
import com.example.hama.model.user.User;
import com.example.hama.util.SNSTime;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDTO {
	
    private Long id; // 댓글 ID
    private String logReplyContent; // 댓글 내용
    private Long parentReplyId; // 부모 댓글 ID
    private String author; // 댓글 작성자 이름
    private String timeAgo; // 상대 시간 표시
    private int likeCount;
    @JsonProperty("isLiked") // JSON 응답에서 "isLiked"로 출력
    private boolean isLiked;
    @JsonProperty("isAuthor") // JSON 응답에서 "isAuthor"로 출력
    private boolean isAuthor; // 댓글 작성자인지 여부
    private List<ReplyDTO> childReplies = new ArrayList<>(); // 자식 댓글

    
	@ManyToOne(fetch = FetchType.EAGER) // 한명이 여러개의 일지를 쓰니까
	@JoinColumn(name="user_id", nullable = false)
	private User user;	   //사용자
    

    public ReplyDTO(LogReply logReply) {
        this.id = logReply.getLogReplyId();
        this.logReplyContent = logReply.getLogReplyContent();
        this.author = logReply.getUser().getName();
        this.parentReplyId = (logReply.getParentReply() != null) ? logReply.getParentReply().getLogReplyId() : null;
        this.timeAgo = SNSTime.getTimeAgo(logReply.getLogCreatedTime());
    }
    public ReplyDTO(LogReply logReply, User currentUser) {
        this.id = logReply.getLogReplyId();
        this.logReplyContent = logReply.getLogReplyContent();
        this.author = logReply.getUser() != null ? logReply.getUser().getName() : "익명"; // 작성자 이름 설정
        this.parentReplyId = (logReply.getParentReply() != null) ? logReply.getParentReply().getLogReplyId() : null;
        this.timeAgo = SNSTime.getTimeAgo(logReply.getLogCreatedTime());
        this.isAuthor = currentUser != null && logReply.getUser().getUserId().equals(currentUser.getUserId()); // 현재 로그인한 사용자와 댓글 작성자 비교
    }
    // currentUser를 받아 작성자인지 판단하는 생성자
    public ReplyDTO(LogReply logReply, User currentUser, int likeCount, boolean isLiked) {
        this.id = logReply.getLogReplyId();
        this.logReplyContent = logReply.getLogReplyContent();
        this.author = logReply.getUser() != null ? logReply.getUser().getName() : "익명";
        this.parentReplyId = (logReply.getParentReply() != null) ? logReply.getParentReply().getLogReplyId() : null;
        this.timeAgo = SNSTime.getTimeAgo(logReply.getLogCreatedTime());
        this.isAuthor = currentUser != null && logReply.getUser().getUserId().equals(currentUser.getUserId());
        this.likeCount = likeCount;
        this.isLiked = isLiked; // 사용자의 좋아요 여부 추가
    }
    
}

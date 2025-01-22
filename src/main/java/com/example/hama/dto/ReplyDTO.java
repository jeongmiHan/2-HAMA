package com.example.hama.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.hama.model.log.Reply;
import com.example.hama.model.user.User;
import com.example.hama.util.SNSTime;

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
    private List<ReplyDTO> childReplies = new ArrayList<>(); // 자식 댓글

    
	@ManyToOne(fetch = FetchType.EAGER) // 한명이 여러개의 일지를 쓰니까
	@JoinColumn(name="user_id", nullable = false)
	private User user;	   //사용자
    

    public ReplyDTO(Reply reply) {
        this.id = reply.getLogReplyId();
        this.logReplyContent = reply.getLogReplyContent();
        this.author = reply.getUser().getName();
        this.parentReplyId = (reply.getParentReply() != null) ? reply.getParentReply().getLogReplyId() : null;
        this.timeAgo = SNSTime.getTimeAgo(reply.getLogCreatedTime());
    }
}

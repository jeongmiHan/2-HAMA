package com.example.hama.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.hama.model.log.Reply;

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
    private List<ReplyDTO> childReplies = new ArrayList<>(); // 자식 댓글

    public ReplyDTO(Reply reply) {
        this.id = reply.getLogReplyId();
        this.logReplyContent = reply.getLogReplyContent();
        this.parentReplyId = (reply.getParentReply() != null) ? reply.getParentReply().getLogReplyId() : null;
    }
}

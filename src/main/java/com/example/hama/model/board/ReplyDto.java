package com.example.hama.model.board;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ReplyDto {
    private Long replyId;
    private String rpContent;
    private String author;
    private String userId; // 작성자의 ID
    private LocalDateTime rpCreatedTime; 
    private List<ReplyDto> childReplies;
    private int likeCount;
    private boolean isLiked;
    private boolean isSecret; // 비밀댓글 여부 추가
    private boolean isAccessible; // 접근 가능 여부 추가

    public ReplyDto(Long replyId, String rpContent, String author, String userId, int likeCount, boolean isLiked, 
                    List<ReplyDto> childReplies, LocalDateTime rpCreatedTime, boolean isSecret, boolean isAccessible) {
        this.replyId = replyId;
        this.rpContent = rpContent;
        this.author = author;
        this.userId = userId;
        this.rpCreatedTime = rpCreatedTime;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.childReplies = childReplies;
        this.isSecret = isSecret;
        this.isAccessible = isAccessible;
    }
}



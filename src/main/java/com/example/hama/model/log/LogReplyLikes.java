package com.example.hama.model.log;

import com.example.hama.model.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class LogReplyLikes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne
    @JoinColumn(name = "log_reply_id", nullable = false)
    private LogReply logReply;  // 좋아요가 연결된 댓글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 좋아요를 누른 사용자

    public LogReplyLikes(User user, LogReply logReply) {
        this.user = user;
        this.logReply = logReply;
    }
}

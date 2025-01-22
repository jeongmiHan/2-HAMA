package com.example.hama.model.log;

import com.example.hama.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class LogLikes {
	   @Id
       @Column(name="like_id")
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long likeId;

       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "reply_id")
       private Reply reply;  // 댓글에 좋아요를 연결

       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "user_id")
       private User user;  // 좋아요를 누른 회원
       
       @ManyToOne
       @JoinColumn(name = "log_id", nullable = false)
       private Log log; // 좋아요가 눌린 로그
       
       private boolean liked;
}

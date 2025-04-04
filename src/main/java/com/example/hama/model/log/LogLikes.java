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
       @JoinColumn(name = "user_id", nullable = false)
       private User user;  // 좋아요를 누른 회원
       
       @ManyToOne
       @JoinColumn(name = "log_id", nullable = true)
       private Log log; // 좋아요가 눌린 로그
       
       private boolean liked;
       
       // 기본 생성자
       public LogLikes() {
       }
       
       public LogLikes(User user, Log log) {
    	    this.user = user;
    	    this.log = log;
    	    this.liked = true;
       }

}

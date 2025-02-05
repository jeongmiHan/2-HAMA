package com.example.hama.model.board;

import java.time.LocalDateTime;

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
public class likes {
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

	    private LocalDateTime createdLikeTime = LocalDateTime.now(); // 좋아요 생성 시간
}

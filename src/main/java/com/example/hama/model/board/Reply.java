package com.example.hama.model.board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.hama.model.user.User;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "replyId") // 순환 참조 처리
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long replyId;

    @ManyToOne
    @JoinColumn(name = "board_id")
    @JsonIgnore // 순환 참조 방지
    private Board board; // 게시글

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "roles", "provider", "providerId"}) // 순환 참조 방지 및 필요 없는 필드 제외
    private User user; // 사용자 정보

    @Column(length = 1000, nullable = false)
    private String rpContent; // 댓글 내용

    private LocalDateTime rpCreatedTime; // 작성 시간

    // 부모 댓글
    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore // 순환 참조 방지
    private Reply parentReply;

    // 대댓글 목록
    @OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> childReplies = new ArrayList<>();

    // 좋아요 목록
    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<likes> likes = new ArrayList<>();

    // 좋아요 개수 반환
    public int getLikeCount() {
        return likes.size();
    }

    
    @Column(name = "is_secret", nullable = false)
    private boolean isSecret = false; // 기본값은 false로 설정

    public boolean isLikedByUser(String currentUserId) {
        if (currentUserId == null) {
            return false; // 로그인하지 않은 사용자 처리
        }

        // `likes` 리스트에서 현재 사용자가 좋아요를 눌렀는지 확인
        return likes.stream()
                .anyMatch(like -> like.getUser().getUserId().equals(currentUserId));
    }


}



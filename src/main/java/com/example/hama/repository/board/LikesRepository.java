package com.example.hama.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hama.model.board.likes;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<likes, Long> {
    // 특정 사용자가 특정 댓글에 좋아요를 눌렀는지 확인
    Optional<likes> findByUser_UserIdAndReply_ReplyId(String userId, Long replyId);

    // 특정 댓글의 좋아요 개수 조회
    int countByReply_ReplyId(Long replyId);

    // 특정 댓글의 모든 좋아요 삭제
    void deleteByReply_ReplyId(Long replyId);

}



package com.example.hama.repository.board;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hama.model.board.Reply;
import com.example.hama.model.board.likes;
import com.example.hama.model.user.User;

@Repository
public interface LikesRepository extends JpaRepository<likes, Long> {
    Optional<likes> findByUserAndReply(User user, Reply reply); // ✅ 특정 댓글에 대한 좋아요 여부 조회
       int countByReply_ReplyId(Long replyId); // ✅ 특정 댓글의 좋아요 개수 조회
}



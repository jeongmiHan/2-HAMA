package com.example.hama.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hama.model.log.LogReplyLikes;
import com.example.hama.model.log.LogReply;
import com.example.hama.model.user.User;

@Repository
public interface LogReplyLikeRepository extends JpaRepository<LogReplyLikes, Long> {
    Optional<LogReplyLikes> findByUserAndLogReply(User user, LogReply logReply);

    int countByLogReply(LogReply logReply);
}

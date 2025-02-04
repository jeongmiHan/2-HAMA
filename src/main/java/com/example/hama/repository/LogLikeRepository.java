package com.example.hama.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogLikes;
import com.example.hama.model.log.Reply;
import com.example.hama.model.user.User;

@Repository
public interface LogLikeRepository extends JpaRepository<LogLikes, Long> {
	// 특정 로그와 사용자에 해당하는 좋아요 검색
    Optional<LogLikes> findByUserAndLog(User user, Log log);
    
    // 특정 로그의 좋아요 개수 계산
    int countByLog(Log log);
}

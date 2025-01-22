package com.example.hama.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogLikes;
import com.example.hama.model.user.User;

@Repository
public interface LogLikeRepository extends JpaRepository<LogLikes, Long> {
    Optional<LogLikes> findByUserAndLog(User user, Log log);
}

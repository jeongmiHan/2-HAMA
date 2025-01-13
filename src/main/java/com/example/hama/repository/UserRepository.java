package com.example.hama.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.user.User;

public interface UserRepository extends JpaRepository<User, String> {

    // 이름으로 사용자 조회
    User findByName(String name);

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 소셜 로그인 사용자 조회 (providerUserId를 기준으로 조회)
    Optional<User> findByProviderUserId(String providerUserId);
}

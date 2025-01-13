package com.example.hama.model.user;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId; // Primary Key

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = true)
    private String password; // 기본 로그인 사용자만 사용

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "provider", nullable = true)
    private String provider; // 소셜 로그인 플랫폼 이름 (google, naver)

    @Column(name = "provider_user_id", nullable = true)
    private String providerUserId; // 소셜 로그인 사용자 고유 ID

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER; // 기본 역할 (사용자)

    @PrePersist
    public void prePersist() {
        if (this.joinDate == null) {
            this.joinDate = LocalDate.now();
        }
        if (this.password == null) {
            this.password = ""; // 비밀번호 기본값 설정
        }
        if (this.role == null) {
        	this.role = Role.USER;
        }
    }

    // 소셜 로그인 여부 확인
    public boolean isSocialLogin() {
        return provider != null && providerUserId != null;
    }

    // 소셜 로그인 유저 ID 생성
    public static String generateUserId(String provider, String providerUserId) {
        return provider + "_" + providerUserId;
    }

    // 비밀번호 체크 메소드 (기본 로그인 사용자용)
    public boolean checkPassword(String rawPassword, BCryptPasswordEncoder passwordEncoder) {
        return password != null && passwordEncoder.matches(rawPassword, this.password);
    }

    // 기본 로그인 사용자 저장 시 비밀번호 암호화
    public void encodePassword(BCryptPasswordEncoder passwordEncoder) {
        if (this.password != null && !this.password.isEmpty()) {
            this.password = passwordEncoder.encode(this.password);
        }
    }

    // JWT Payload에 담을 사용자 정보 반환
    public Map<String, Object> getJwtClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", this.userId);
        claims.put("email", this.email);
        claims.put("role", this.role.name()); // Enum을 문자열로 변환
        return claims;
    }
}

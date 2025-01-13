package com.example.hama.service;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.user.Role;
import com.example.hama.model.user.User;
import com.example.hama.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 기본 로그인 사용자 로드
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("Attempting to load user by userId: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found for userId: {}", userId);
            return new UsernameNotFoundException("User not found: " + userId);
        });
        log.info("User loaded successfully: {}", user);
        return new CustomUserDetails(user);
    }

    /**
     * 사용자 저장
     */
    public void saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
        log.info("User saved successfully: {}", user);
    }

    /**
     * 사용자 ID로 조회
     */
    public User findUserById(String userId) {
        log.debug("Finding user by userId: {}", userId);
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 이름으로 사용자 조회
     */
    public User findUserByName(String name) {
        log.debug("Finding user by name: {}", name);
        return userRepository.findByName(name);
    }

    /**
     * providerUserId로 사용자 조회 (소셜 로그인용)
     */
    public User findUserByProviderUserId(String providerUserId) {
        log.debug("Finding user by providerUserId: {}", providerUserId);
        return userRepository.findByProviderUserId(providerUserId).orElse(null);
    }

    /**
     * 이메일로 사용자 조회
     */
    public User findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 소셜 로그인 처리
     */
    public User handleSocialLogin(String provider, String providerUserId, String email, String name) {
        log.info("Handling social login for provider={}, providerUserId={}", provider, providerUserId);
        User user = userRepository.findByProviderUserId(providerUserId).orElseGet(() -> {
            User newUser = new User();
            newUser.setUserId(User.generateUserId(provider, providerUserId));
            newUser.setProvider(provider);
            newUser.setProviderUserId(providerUserId);
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole(Role.USER);
            saveUser(newUser);
            log.info("New social login user created: {}", newUser);
            return newUser;
        });

        // SecurityContext에 인증 객체 설정
        setAuthentication(user);
        log.info("Social login processed successfully for user: {}", user);
        return user;
    }

    /**
     * SecurityContext에 인증 객체 설정
     */
    private void setAuthentication(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("SecurityContextHolder updated with authentication for user: {}", user.getUserId());
    }

    /**
     * 비밀번호 검증
     */
    public boolean checkPassword(User user, String rawPassword) {
        log.debug("Checking password for userId: {}", user.getUserId());
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * 비밀번호 확인
     */
    public boolean validatePassword(String userId, String rawPassword) {
        log.debug("Validating password for userId: {}", userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for userId: {}", userId);
            return false;
        }
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        log.info("Password validation result: {}", matches);
        return matches;
    }
}

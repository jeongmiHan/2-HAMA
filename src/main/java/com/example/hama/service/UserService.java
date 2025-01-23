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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;

    /**
     * 기본 로그인 사용자 로드
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("Attempting to load user by userId: {}", userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for userId: {}", userId);
            throw new UsernameNotFoundException("User not found for userId: " + userId);
        }
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
     * 이메일로 사용자 조회
     */
    public User findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 소셜 로그인 사용자 제외: 이메일로 사용자 ID 목록 조회
     */
    public List<String> findUserIdByEmailExcludingSocial(String email) {
        log.info("Fetching user IDs for email (excluding social): {}", email);

        List<User> users = userRepository.findByEmailAndProviderIsNull(email);
        if (users.isEmpty()) {
            log.warn("가입된 사용자가 없습니다: email={}", email);
            return List.of(); // 빈 리스트 반환
        }

        // 사용자 ID 추출
        return users.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
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
        String role = "ROLE_" + user.getRole(); // DB에 저장된 Role을 권한으로 변환
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
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


    /**
     * 임시 비밀번호 생성
     */
    private String generateTemporaryPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        return random.ints(8, 0, characters.length())
                .mapToObj(characters::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    /**
     * 이메일로 사용자 조회 (아이디 찾기)
     */
    public String findUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null; // 사용자 없으면 null 반환
        }
        return user.getUserId();
    }

    /**
     * providerUserId로 사용자 조회 (소셜 로그인용)
     */
    public User findUserByProviderUserId(String providerUserId) {
        log.debug("Finding user by providerUserId: {}", providerUserId);
        return userRepository.findByProviderUserId(providerUserId).orElse(null);
    }
    public String resetPasswordWithVerification(String userId, String email) {
        User user = validateUserForPasswordReset(userId, email);

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);

        return temporaryPassword;
    }

    private User validateUserForPasswordReset(String userId, String email) {
        return userRepository.findByUserIdAndEmailAndProviderIsNull(userId, email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 아이디 또는 이메일이 일치하지 않습니다."));
    }


}

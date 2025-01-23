package com.example.hama.service;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.user.Role;
import com.example.hama.model.user.User;
import com.example.hama.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 소셜 로그인 제공자와 사용자 정보 추출
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");

        // "google_" 접두사와 8자리 난수 생성
        String randomUsername = generateRandomUsername(provider);

        log.info("OAuth2 로그인 시도: provider={}, providerUserId={}", provider, providerUserId);

        // 사용자 처리: DB에서 사용자 조회 또는 생성
        User user = userRepository.findByProviderUserId(providerUserId).orElseGet(() -> {
            log.info("DB에 사용자 정보 없음. 새 사용자 생성: providerUserId={}", providerUserId);
            User newUser = new User();
            newUser.setUserId(User.generateUserId(provider, providerUserId));
            newUser.setProvider(provider);
            newUser.setProviderUserId(providerUserId);
            newUser.setEmail(email);
            newUser.setName(randomUsername); // "google_8자리" 형식으로 사용자 이름 설정
            newUser.setRole(Role.USER);
            newUser.setJoinDate(LocalDate.now());
            userRepository.save(newUser);
            return newUser;
        });

        log.info("OAuth2 로그인 처리된 사용자: {}", user);

        // SecurityContext에 사용자 정보 저장
        setAuthentication(user);

        // DefaultOAuth2User 반환 (OAuth2User 타입 유지)
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                oAuth2User.getAttributes(),
                "name" // 기본 필드 이름 매핑
        );
    }

    /**
     * "google_8자리" 형식으로 난수 생성
     */
    private String generateRandomUsername(String provider) {
        Random random = new Random();
        int number = random.nextInt(100000000); // 8자리 숫자 생성
        return provider + "_" + String.format("%08d", number); // "google_00000000" 형식
    }

    /**
     * SecurityContext에 인증 객체 설정
     */
    private void setAuthentication(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("SecurityContext 업데이트: 인증 객체={}", authentication);
    }
}

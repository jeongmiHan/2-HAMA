package com.example.hama.config;

import com.example.hama.service.CustomOAuth2UserService;
import com.example.hama.service.UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Config {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Lazy
    public Config(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    /**
     * 비밀번호 인코더
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.info("Creating BCryptPasswordEncoder Bean");
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain");

        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스 및 공개 URL 허용
                .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**").permitAll()
                .requestMatchers("/", "/user/register", "/user/login", "/user/social-login").permitAll()
                // 인증된 사용자만 접근 허용
                .requestMatchers("/user/mypage", "/user/logout", "/calendar", "/api/events/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/user/login") // 기본 로그인 페이지
                .defaultSuccessUrl("/calendar", true) // 로그인 성공 후 이동할 URL
                .failureUrl("/user/login?error=true") // 로그인 실패 후 이동할 URL
                .usernameParameter("userId") // 사용자 ID 파라미터
                .passwordParameter("password") // 비밀번호 파라미터
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout") // 로그아웃 URL
                .logoutSuccessUrl("/") // 로그아웃 성공 후 이동할 URL
                .invalidateHttpSession(true) // 세션 무효화
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/user/login") // 소셜 로그인 페이지
                .defaultSuccessUrl("/calendar", true) // 소셜 로그인 성공 후 이동할 URL
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // Custom OAuth2 User Service 설정
                )
            );

        log.info("Security Filter Chain configuration completed");
        return http.build();
    }

    /**
     * Authentication Provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        log.info("DaoAuthenticationProvider configured with UserService and PasswordEncoder");
        return authProvider;
    }

    /**
     * Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        log.info("Configuring Authentication Manager");
        return authenticationConfiguration.getAuthenticationManager();
    }
}

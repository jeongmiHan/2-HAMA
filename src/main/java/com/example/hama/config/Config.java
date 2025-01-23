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
        http
            .csrf(csrf -> csrf.disable()) // 필요에 따라 활성화 가능
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/user/register", "/user/login", "/oauth2/**", // Google OAuth2 경로
                    "/assets/**", "/images/**", "/webfonts/**", "/static/**",
                    "/api/user/validate-password", "/api/user/check-id",
                    "/api/user/check-name", "/api/email/verify-email", "/api/user/find-id",
                    "/api/email/verify-code", "/user/find-id", "/user/reset-password", "/api/user/reset-password"
                ).permitAll() // 공개 URL
                .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 페이지
                .anyRequest().authenticated() // 나머지 요청 인증 필요
            )
            .formLogin(form -> form
                .loginPage("/user/login") // 사용자 로그인 페이지
                .defaultSuccessUrl("/calendar", true) // 로그인 성공 후 리다이렉트
                .failureUrl("/user/login?error=true") // 로그인 실패
                .usernameParameter("userId") // 사용자 ID
                .passwordParameter("password") // 비밀번호
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout") // 로그아웃 URL
                .logoutSuccessUrl("/") // 로그아웃 성공 후 리다이렉트
                .invalidateHttpSession(true) // 세션 무효화
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/user/login") // OAuth2 로그인 페이지
                .defaultSuccessUrl("/calendar", true) // 성공 후 리다이렉트
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
            );

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

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
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/user/register", "/user/login", "/user/social-login",
                    "/assets/**", "/images/**", "/webfonts/**", "/static/**",
                    "/api/user/validate-password","/api/user/check-id",
                    "/api/user/check-name","/api/email/verify-email",
                    "/api/email/verify-code"
                ).permitAll() // 공개 URL
                .requestMatchers("/user/mypage", "/calendar", "/api/events/**").authenticated() // 인증 필요 URL
                .anyRequest().authenticated() // 나머지 모든 요청 인증 필요
            )
            .formLogin(form -> form
                .loginPage("/user/login")
                .defaultSuccessUrl("/calendar", true)
                .failureUrl("/user/login?error=true")
                .usernameParameter("userId")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/user/login")
                .defaultSuccessUrl("/calendar", true)
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

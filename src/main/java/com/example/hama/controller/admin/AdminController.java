package com.example.hama.controller.admin;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @GetMapping("/adminpage")
    public String adminPage() {
        // Spring Security 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Unauthorized access attempt detected. Redirecting to login page.");
            return "redirect:/user/login"; // 인증되지 않은 사용자 처리
        }

        log.info("User authenticated: {}", authentication.getName());

        // 인증된 사용자 권한 확인
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            log.warn("User {} attempted to access admin page without admin privileges.", authentication.getName());
            return "redirect:/error/403"; // 권한이 없는 경우 처리
        }

        log.info("Admin access granted for user: {}", authentication.getName());
        // 인증 및 권한 검증 후 관리자 페이지 반환
        return "admin/adminpage"; // HTML 템플릿 반환
    }
}

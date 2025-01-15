package com.example.hama;
                            
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class LoginInterceptor implements HandlerInterceptor  {
    @Override
       public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
           // Spring Security의 인증 정보 가져오기
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

           // 인증되지 않은 사용자 처리
           if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
               response.sendRedirect("/"); // 최상위 경로로 리다이렉트
               return false; // 요청 처리 중단
           }

           return true; // 인증된 경우 요청 계속 진행
       }
    
    
}


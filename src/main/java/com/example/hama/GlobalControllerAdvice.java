package com.example.hama;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute("nickname")
       public String getAuthenticatedUserNickname() {
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

           if (authentication != null && authentication.isAuthenticated()) {
               Object principal = authentication.getPrincipal();

               // 기본 로그인 사용자 처리
               if (principal instanceof UserDetails) {
                   return ((UserDetails) principal).getUsername();
               }

               // 소셜 로그인 사용자 처리
               if (principal instanceof DefaultOAuth2User) {
                   return (String) ((DefaultOAuth2User) principal).getAttributes().get("name"); // 사용자 이름 추출
               }
           }
           return "익명"; // 기본값
       }
}

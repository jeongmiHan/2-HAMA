package com.example.hama.controller.calendar;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.user.User;
import com.example.hama.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final UserService userService;

    @GetMapping("/calendar")
    public String getCalendarPage(Model model) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("SecurityContext 인증 객체: {}", authentication);

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            User user = null;

            // 기본 로그인 사용자 처리
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                user = userDetails.getUser();
                log.info("기본 로그인 사용자: {}", user);
            }
            // 소셜 로그인 사용자 처리
            else if (principal instanceof DefaultOAuth2User) {
                DefaultOAuth2User oAuth2User = (DefaultOAuth2User) principal;
                String providerUserId = (String) oAuth2User.getAttributes().get("sub");
                user = userService.findUserByProviderUserId(providerUserId);
                log.info("소셜 로그인 사용자: {}", user);
            }

            // 사용자 정보가 있으면 모델에 추가
            if (user != null) {
                model.addAttribute("nickname", user.getName());
                log.info("모델에 추가된 사용자 이름: {}", user.getName());
                return "calendar"; // 캘린더 뷰로 이동
            }
        }

        // 인증되지 않은 경우 로그인 페이지로 리디렉션
        log.warn("인증되지 않은 접근. 로그인 페이지로 리디렉션.");
        return "redirect:/user/login";
    }
}

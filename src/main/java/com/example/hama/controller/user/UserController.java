package com.example.hama.controller.user;

import com.example.hama.model.user.User;
import com.example.hama.model.user.UserJoin;
import com.example.hama.model.user.UserLogin;
import com.example.hama.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 폼 페이지
     */
    @GetMapping("register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserJoin());
        return "user/register";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("register")
    public String register(@Validated @ModelAttribute("user") UserJoin userJoin,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            log.info("회원가입 폼에서 유효성 검사 오류 발생: {}", result.getAllErrors());
            return "user/register";
        }

        User user = UserJoin.toUser(userJoin);

        // ID 중복체크
        if (userService.findUserById(user.getUserId()) != null) {
            log.warn("중복된 ID로 회원가입 시도: {}", user.getUserId());
            result.rejectValue("userId", "duplicatedId", "해당 ID로 이미 가입한 회원이 존재합니다.");
            return "user/register";
        }

        // 새 사용자 저장
        userService.saveUser(user);
        log.info("회원가입 성공: {}", user.getUserId());

        return "redirect:/calendar";
    }

    /**
     * 로그인 폼 페이지
     */
    @GetMapping("login")
    public String loginForm(Model model) {
        model.addAttribute("userLogin", new UserLogin());
        return "redirect:/";
    }

    /**
     * 기본 로그인 처리
     */
    @PostMapping("login")
    public String login(@Validated @ModelAttribute UserLogin userLogin,
                        BindingResult result,
                        HttpServletRequest request) {
        if (result.hasErrors()) {
            log.info("로그인 폼에서 유효성 검사 오류 발생: {}", result.getAllErrors());
            return "user/login";
        }

        User findUser = userService.findUserById(userLogin.getUserId());
        if (findUser == null) {
            log.warn("존재하지 않는 ID로 로그인 시도");
            result.reject("NotFoundId", "존재하지 않는 ID입니다. 회원가입을 하십시오.");
            return "user/login";
        }

        if (!userService.checkPassword(findUser, userLogin.getPassword())) {
            log.warn("비밀번호 불일치: 사용자 ID={}", userLogin.getUserId());
            result.reject("NotFoundPw", "비밀번호가 틀립니다.");
            return "user/login";
        }

        // 세션에 사용자 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", findUser);
        session.setMaxInactiveInterval(30 * 60); // 30분 후 세션 만료
        log.info("로그인 성공. 세션에 저장된 사용자: {}", findUser);

        return "redirect:/calendar";
    }

    /**
     * 소셜 로그인 처리
     */
    @GetMapping("social-login")
    public String socialLogin(@RequestParam String provider,
                              @RequestParam String providerUserId,
                              @RequestParam String email,
                              @RequestParam String name,
                              HttpServletRequest request) {
        log.info("소셜 로그인 시도: provider={}, providerUserId={}", provider, providerUserId);

        // 소셜 로그인 사용자 처리
        User user = userService.handleSocialLogin(provider, providerUserId, email, name);

        // 세션에 로그인 사용자 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", user);
        log.info("소셜 로그인 성공: 세션 ID: {}, 사용자: {}", session.getId(), user);

        return "redirect:/calendar";
    }

    /**
     * 세션 확인
     */
    @GetMapping("/check-session")
    public String checkSession(HttpSession session) {
        Object user = session.getAttribute("loginUser");
        if (user != null) {
            log.info("세션 사용자 정보 확인: {}", user);
        } else {
            log.warn("세션에 사용자 정보가 없습니다.");
        }
        return "session-info";
    }

    /**
     * 로그아웃 처리
     */
    @GetMapping("logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("로그아웃 처리. 세션 무효화: {}", session.getId());
            session.invalidate();
        }
        return "redirect:/user/login";
    }
    /**
     * 아이디 찾기 폼 페이지
     */
    @GetMapping("find-id")
    public String findIdForm() {
        return "user/find-id"; // 아이디 찾기 HTML 템플릿
    }

    @GetMapping("reset-password")
    public String resetPasswordForm() {
        return "user/reset-password"; // 비밀번호 찾기 HTML 템플릿
    }

}

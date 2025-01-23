package com.example.hama.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hama.service.UserService;
import com.example.hama.service.VerificationCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserApiController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    /**
     * 아이디 중복 확인
     */
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkUserId(@RequestParam("userId") String userId) {
        boolean exists = userService.findUserById(userId) != null;
        return ResponseEntity.ok(exists);
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkUserName(@RequestParam("name") String name) {
        boolean exists = userService.findUserByName(name) != null;
        return ResponseEntity.ok(exists);
    }

    /**
     * 비밀번호 유효성 확인
     */
    @PostMapping("/validate-password")
    public ResponseEntity<Map<String, Boolean>> validatePassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String password = request.get("password");
        boolean isValid = userService.validatePassword(userId, password);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", isValid);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일로 아이디 찾기
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findIdByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이메일은 필수 입력값입니다."));
        }

        try {
            List<String> userIds = userService.findUserIdByEmailExcludingSocial(email);
            if (userIds.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "해당 이메일로 가입된 아이디를 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(Map.of("success", true, "userIds", userIds));
        } catch (Exception e) {
            log.error("아이디 찾기 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 재설정
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String email = request.get("email");

        try {
            String temporaryPassword = userService.resetPasswordWithVerification(userId, email);
            return ResponseEntity.ok(Map.of("success", true, "temporaryPassword", temporaryPassword));
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 역할(Role) 확인
     */
    @GetMapping("/check-role")
    public ResponseEntity<?> checkRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증되지 않은 사용자 요청");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "사용자가 인증되지 않았습니다."));
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        log.info("사용자 역할 확인: role={}", role);
        return ResponseEntity.ok(Map.of("success", true, "role", role));
    }
}

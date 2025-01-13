package com.example.hama.controller.user;

import com.example.hama.model.GlobalResponse;
import com.example.hama.model.user.UserEmailRequestDto;
import com.example.hama.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
@Slf4j // SLF4J 로깅 지원
public class EmailVerificationController {

    private final VerificationCodeService verificationCodeService;

    // 이메일 인증 코드 요청
    @PostMapping("/verify-email")
    public ResponseEntity<GlobalResponse<Void>> requestVerificationCode(@Valid @RequestBody UserEmailRequestDto requestDto) {
        try {
            verificationCodeService.createAndSendVerificationCode(requestDto.getEmail());
            log.info("인증 코드가 {}으로 전송되었습니다.", requestDto.getEmail());
            return ResponseEntity.ok(GlobalResponse.of("200", "인증 코드가 이메일로 전송되었습니다.", null));
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(500).body(GlobalResponse.of("500", "이메일 전송에 실패했습니다.", null));
        }
    }

    // 인증 코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<GlobalResponse<Void>> verifyCode(
            @RequestParam(name = "email") String email, 
            @RequestParam(name = "code") String code) {
        try {
            boolean isVerified = verificationCodeService.verifyCode(email, code);
            if (isVerified) {
                log.info("인증 코드가 성공적으로 확인되었습니다: {}", code);
                return ResponseEntity.ok(GlobalResponse.of("200", "인증 코드가 성공적으로 확인되었습니다.", null));
            } else {
                log.warn("인증 코드가 유효하지 않거나 이미 사용되었습니다: {}", code);
                return ResponseEntity.status(400).body(GlobalResponse.of("400", "인증 코드가 유효하지 않거나 이미 사용되었습니다.", null));
            }
        } catch (Exception e) {
            log.error("인증 코드 검증 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body(GlobalResponse.of("500", "인증 코드 검증에 실패했습니다.", null));
        }
    }
}

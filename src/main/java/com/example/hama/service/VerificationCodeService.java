package com.example.hama.service;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.example.hama.model.user.VerificationCode;
import com.example.hama.repository.VerificationCodeRepository;

@Slf4j // SLF4J 로깅 지원
@Service
public class VerificationCodeService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final String EMAIL_SUBJECT = "이메일 인증 코드";
    private static final String EMAIL_FROM = "mohupj@gmail.com"; // 발신자 이메일 설정

    // 6자리 랜덤 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // 6자리 랜덤 코드 생성
    }

    // 이메일 인증 코드 생성 및 전송
    public void createAndSendVerificationCode(String email) {
        // 기존 인증 코드 조회
        VerificationCode existingCode = verificationCodeRepository.findByEmail(email);

        // 기존 인증 코드가 존재하는 경우 삭제
        if (existingCode != null) {
            verificationCodeRepository.delete(existingCode); // 기존 코드 삭제
            log.info("기존 인증 코드가 삭제되었습니다: {}", existingCode.getCode());
        }

        // 새로운 인증 코드 생성
        String code = generateVerificationCode();
        sendCodeToEmail(email, code);

        // VerificationCode 엔티티 생성 및 저장
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setUsed(false); // 사용 여부 기본값 설정
        verificationCodeRepository.save(verificationCode); // DB에 저장
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email);
        
        if (verificationCode == null) {
            log.info("해당 이메일로 저장된 인증 코드가 없습니다.");
            return false; // 인증 코드가 존재하지 않음
        }

        log.info("DB에 저장된 코드: {}", verificationCode.getCode());
        log.info("사용자가 입력한 코드: {}", code);
        
        if (verificationCode.getCode().equals(code) && !verificationCode.isUsed()) {
            verificationCode.setUsed(true); // 사용 처리
            verificationCodeRepository.save(verificationCode); // DB에 저장
            return true; // 인증 성공
        }
        
        log.info("인증 코드가 유효하지 않거나 이미 사용되었습니다.");
        return false; // 인증 실패
    }

    // 이메일로 인증 코드 전송
    private void sendCodeToEmail(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(EMAIL_SUBJECT);
            message.setText("인증 코드는 다음과 같습니다: " + code);
            message.setFrom(EMAIL_FROM);
            mailSender.send(message);
            log.info("인증 코드가 이메일로 발송되었습니다: {}", code);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
        }
    }
}

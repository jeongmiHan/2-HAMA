package com.example.hama.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.hama.model.user.VerificationCode;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findByEmail(String email);
}

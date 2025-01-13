package com.example.hama.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserEmailRequestDto {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 주소를 입력하세요.")
    private String email;
}

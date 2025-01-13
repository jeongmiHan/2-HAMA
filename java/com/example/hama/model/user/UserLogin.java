package com.example.hama.model.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserLogin {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4 ~ 20자 사이로 입력해 주세요.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이로 입력해 주세요.")
    private String password;

    // User 객체로 변환하는 login 메서드
    public static User login(UserLogin userLogin) {
        User user = new User();
        user.setUserId(userLogin.getUserId()); // userId 매핑
        user.setPassword(userLogin.getPassword()); // 비밀번호 매핑
        return user;
    }
}

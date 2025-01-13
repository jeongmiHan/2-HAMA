package com.example.hama.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GlobalResponse<T> {
    private String code;
    private String message;
    private T data;

    // 생성자
    public GlobalResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 정적 팩토리 메서드
    public static <T> GlobalResponse<T> of(String code, String message, T data) {
        return new GlobalResponse<>(code, message, data);
    }
}

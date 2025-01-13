package com.example.hama.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString @Getter @Setter
public class GlobalResponse<T> {
    private String code;
    private String message;
    private T data;

    // 생성자, getter, setter 등 필요에 따라 추가
    
    public static <T> GlobalResponse<T> of(String code, String message, T data) {
        GlobalResponse<T> response = new GlobalResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
}


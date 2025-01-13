package com.example.hama.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hama.model.user.UserLogin;
import com.example.hama.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    // 아이디 중복 확인
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkUserId(@RequestParam("userId") String userId) {
        boolean exists = userService.findUserById(userId) != null;
        return ResponseEntity.ok(exists);
    }

    // 닉네임 중복 확인
    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkUserName(@RequestParam("name") String name) {
        boolean exists = userService.findUserByName(name) != null;
        return ResponseEntity.ok(exists);
    }
    @PostMapping("/validate-password")
    public ResponseEntity<Map<String, Boolean>> validatePassword(@RequestBody UserLogin userLogin) {
        boolean isValid = userService.validatePassword(userLogin.getUserId(), userLogin.getPassword());
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", isValid);
        return ResponseEntity.ok(response);
    }

}

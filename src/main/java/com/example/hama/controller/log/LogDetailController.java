package com.example.hama.controller.log;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hama.model.log.Log;
import com.example.hama.repository.LogFileRepository;
import com.example.hama.service.LogService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LogDetailController {

    private final LogService logService;
    private final LogFileRepository logFileRepository;

    @GetMapping("/detail/{logId}")
    public String getLogDetail(@PathVariable("logId") Long logId, Model model) {
        // ID를 통해 로그 정보 조회
        Log log = logService.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid log ID: " + logId));

        // 모델에 데이터 추가
        model.addAttribute("log", log);

        // logDetail.html로 이동
        return "log/logDetail";
    }
    @DeleteMapping("/log/{logId}/delete")
    public ResponseEntity<?> deleteLog(@PathVariable("logId") Long logId) {
        try {
            logService.deleteLog(logId); // 삭제 서비스 호출
            return ResponseEntity.ok("일기가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("일기 삭제 중 오류 발생: " + e.getMessage());
        }
    }
    @GetMapping("/edit/{logId}")
    public String editLog(@PathVariable("logId") Long logId, Model model) {
        // ID를 통해 기존 일기 조회
        Log log = logService.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        // 모델에 일기 데이터 추가
        model.addAttribute("log", log);

        // 작성 페이지로 이동
        return "log/editLog";
    }
    @PutMapping("/log/{logId}/update")
    public ResponseEntity<?> updateLog(@PathVariable("logId") Long logId, @RequestBody Log updatedLog) {
        try {
            logService.updateLog(logId, updatedLog); // 업데이트 로직 호출
            return ResponseEntity.ok().body("일기가 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("일기 수정 중 오류 발생: " + e.getMessage());
        }
    }
}


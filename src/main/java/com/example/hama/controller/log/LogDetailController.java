package com.example.hama.controller.log;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.model.log.Log;
import com.example.hama.model.user.User;
import com.example.hama.repository.LogFileRepository;
import com.example.hama.service.LogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LogDetailController {

    private final LogService logService;
    private final LogFileRepository logFileRepository;
    
    @GetMapping("/detail/{logId}")
    public String getLogDetail(@PathVariable("logId") Long logId
    						   , Model model
    						   , @RequestParam("name") String nickname) {
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
    @DeleteMapping("/log/{logId}/file/delete")
    public ResponseEntity<?> deleteLogFile(
        @PathVariable("logId") Long logId,
        @RequestBody Map<String, String> request) {
        String filename = request.get("filename");

        try {
            logService.deleteLogFile(logId, filename); // 서비스 호출
            return ResponseEntity.ok("파일이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("파일 삭제 중 오류 발생: " + e.getMessage());
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
    public ResponseEntity<?> updateLog(
        @PathVariable("logId") Long logId,
        @RequestParam("content") String content,
        @RequestParam(value = "logFiles", required = false) List<MultipartFile> logFiles,
        @RequestParam(value = "deletedFiles", required = false) String deletedFilesJson) {
        try {
            // JSON 문자열을 List<String>으로 변환
            List<String> deletedFiles = new ObjectMapper().readValue(deletedFilesJson, new TypeReference<List<String>>() {});

            // 서비스 호출로 일기와 첨부파일 업데이트 및 삭제 처리
            logService.updateLog(logId, content, logFiles, deletedFiles);
            return ResponseEntity.ok("일기가 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("일기 수정 중 오류 발생: " + e.getMessage());
        }
    }

    



}


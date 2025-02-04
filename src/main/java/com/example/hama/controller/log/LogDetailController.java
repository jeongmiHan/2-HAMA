package com.example.hama.controller.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
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

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogAttachedFile;
import com.example.hama.model.user.User;
import com.example.hama.repository.LogFileRepository;
import com.example.hama.repository.LogLikeRepository;
import com.example.hama.repository.LogRepository;
import com.example.hama.repository.UserRepository;
import com.example.hama.service.LogService;
import com.example.hama.service.UserService;
import com.example.hama.util.SNSTime;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LogDetailController {

    private final LogService logService;
    private final UserService userService;
    private final LogFileRepository logFileRepository;
    private final LogRepository logRepository;
    private final UserRepository userRepository;
    private final LogLikeRepository logLikeRepository;
    
    @GetMapping("/detail/{logId}")
    public String getLogDetail(
        @PathVariable("logId") Long logId,
        Model model,
        @RequestParam(name = "name", required = false) String nickname
    ) {
        Log log = logService.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid log ID: " + logId));
        

    	User user = getAuthenticatedUser();
        if(user == null) {
           return "redirect:/user/login";
        }
        model.addAttribute("nickname", user.getName());
        model.addAttribute("log", log);
        
        return "log/logDetail"; // logDetail.html 반환
    }


    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getUser();
            } else if (principal instanceof DefaultOAuth2User oAuth2User) {
                String providerUserId = (String) oAuth2User.getAttributes().get("sub");
                return userService.findUserByProviderUserId(providerUserId);
            }
        }
        return null;
    }
    @DeleteMapping("/log/{logId}/delete")
    public ResponseEntity<?> deleteLog(@PathVariable("logId") Long logId) {
    	User currentUser = getAuthenticatedUser();
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Log not found"));

        if (!log.getUser().getUserId().equals(currentUser.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        }

        logService.deleteLog(logId);
        return ResponseEntity.ok("일기가 삭제되었습니다.");
    }
    
    @DeleteMapping("/log/{logId}/file/delete")
    public ResponseEntity<?> deleteLogFile(@PathVariable("logId") Long logId
    									 , @RequestBody Map<String, String> request) {
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
    public String editLog(@PathVariable("logId") Long logId
    					, Model model) {
        // ID를 통해 기존 일기 조회
        Log log = logService.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        // 모델에 일기 데이터 추가
        model.addAttribute("log", log);

        // 작성 페이지로 이동
        return "log/editLog";
    }
    
    @PutMapping("/log/{logId}/update")
    public ResponseEntity<?> updateLog(@PathVariable("logId") Long logId
								     , @RequestParam("content") String content
								     , @RequestParam(value = "logFiles", required = false) List<MultipartFile> logFiles
								     , @RequestParam(value = "deletedFiles", required = false) String deletedFilesJson) {
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
    @GetMapping("/log/{logId}")
    public ResponseEntity<?> getLogById(@PathVariable("logId") Long logId,
                                        @RequestParam(name = "name", required = false) String nickname) {
        try {
            Log log = logRepository.findById(logId)
                    .orElseThrow(() -> new IllegalArgumentException("Log not found"));

            User authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            // 인증된 사용자를 명시적으로 로드
            User currentUser = userRepository.findById(authenticatedUser.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            boolean isLiked = logLikeRepository.findByUserAndLog(currentUser, log).isPresent();
            boolean isBookmarked = log.getBookmarkedUsers().contains(currentUser);
            boolean isAuthor = log.getUser().getUserId().equals(currentUser.getUserId());

            // 상대 시간 계산 추가
            String timeAgo = SNSTime.getTimeAgo(log.getLogCreatedDate());

            Map<String, Object> logData = new HashMap<>();
            User logUser = log.getUser(); // 로그 작성자
            logData.put("author", logUser != null ? logUser.getName() : "익명");
            logData.put("isAuthor", isAuthor);
            logData.put("id", log.getLogId());
            logData.put("content", log.getLogContent());
            logData.put("time", log.getLogCreatedDate());
            logData.put("timeAgo", timeAgo); // 상대 시간 추가
            logData.put("images", log.getLogAttachedFiles().stream()
                    .map(LogAttachedFile::getLog_saved_filename)
                    .toList());

            // 댓글 개수가 0이면 빈 문자열 반환
            int commentCount = log.getLogComments();
            logData.put("comments", commentCount > 0 ? commentCount : "");

            logData.put("likes", log.getLogLikes().size());
            logData.put("bookmarks", log.getBookmarkedUsers().size());
            logData.put("isLiked", isLiked);
            logData.put("isBookmarked", isBookmarked);

            return ResponseEntity.ok(logData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error retrieving log details: " + e.getMessage());
        }
    }


    



}


package com.example.hama.controller.log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.dto.LogDTO;
import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogAttachedFile;
import com.example.hama.model.log.LogLikes;
import com.example.hama.model.log.LogWrite;
import com.example.hama.model.user.User;
import com.example.hama.repository.LogFileRepository;
import com.example.hama.repository.LogLikeRepository;
import com.example.hama.repository.LogRepository;
import com.example.hama.repository.UserRepository;
import com.example.hama.repository.LogReplyRepository;
import com.example.hama.service.LogFileService;
import com.example.hama.service.LogService;
import com.example.hama.service.UserService;
import com.example.hama.util.SNSTime;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("log")
public class LogController {
	
	@Value("${file.upload.path}")
	private String uploadPath;

	private final UserService userService;
    private final LogService logService;
    private final LogFileService logFileService;
    private final UserRepository userRepository;
	private final LogRepository logRepository;
	private final LogLikeRepository logLikeRepository;
	private final LogReplyRepository logReplyRepository;
	private final LogFileRepository logFileRepository;
	
	    @GetMapping("indexLog")
	    public String showIndexLog(Model model
	    							, @RequestParam(name = "name", required = false) String nickname) {
	    	User user = getAuthenticatedUser();
	        if(user == null) {
	           return "redirect:/user/login";
	        }
	        model.addAttribute("nickname", user.getName());
	        return "log/indexLog"; // src/main/resources/templates/log/indexLog.html
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
		// 일기 등록
		@PostMapping("add")
		public ResponseEntity<?> write( @Validated @ModelAttribute LogWrite logWrite
									  , @RequestParam(value = "parentId", required = false) Long parentId
									  , @RequestParam(value = "logFiles", required = false) List<MultipartFile> logFiles
									  , @RequestParam(name = "name", required = false) String nickname
		) throws IOException {

		    Log log = LogWrite.toLog(logWrite);
		    
		    User user = getAuthenticatedUser();
		      if(user == null) {
		    	  return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		      }
		      log.setUser(user);
		      
		    if (parentId != null) {
		        Log parentLog = logRepository.findById(parentId)
		                .orElseThrow(() -> new IllegalArgumentException("Invalid parent ID"));
		        log.setParent(parentLog);
		    }
		    
		    log = logRepository.save(log);

		    // 여러 파일 저장 처리
		    List<LogAttachedFile> attachedFiles = logFileService.logSaveFiles(logFiles);
		    for (LogAttachedFile file : attachedFiles) {
		        file.setLog(log); // Log와 연결
		        logFileRepository.save(file); // 명시적으로 데이터베이스에 저장
		        System.out.println("Attached file: " + file.getLog_saved_filename());
		    }

		    logService.saveLog(log, logFiles);

		    Map<String, Object> response = new HashMap<>();
		    response.put("id", log.getLogId());
		    response.put("author", user.getName());
		    response.put("content", log.getLogContent());
		    response.put("likes", log.getLogLikes().size());
		    response.put("bookmarks", log.getBookmarkedUsers().size());
		    response.put("images", attachedFiles.stream()
		            .map(LogAttachedFile::getLog_saved_filename)
		            .toList());
		    System.out.println("Response: " + response);
		    return ResponseEntity.ok(response);
		}

		@GetMapping("/images/{filename}")
		public ResponseEntity<?> serveFile(@PathVariable("filename") String filename) {
		    Path filePath = Paths.get(uploadPath).resolve(filename).normalize();
		    try {
		        Resource resource = new UrlResource(filePath.toUri());
		        if (!resource.exists() || !resource.isReadable()) {
		            // 파일이 없어도 아무 응답 없이 처리
		            return ResponseEntity.ok().build();
		        }
		        return ResponseEntity.ok()
		                .contentType(MediaType.IMAGE_JPEG)
		                .body(resource);
		    } catch (Exception e) {
		        // 모든 예외를 무시하고 빈 응답 반환
		        return ResponseEntity.ok().build();
		    }
		}


		
		@GetMapping("/list/tree")
		public ResponseEntity<?> getTreeLogs() {
		    List<LogDTO> logs = logService.getLogTree();
		    return ResponseEntity.ok(logs);
		}
		@GetMapping("/list")
		public ResponseEntity<?> getAllLogs() {
		    try {
		        User user = getAuthenticatedUser();
		        if (user == null) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		        }

		        List<Log> logs = logRepository.findAll();
		        List<Map<String, Object>> response = logs.stream().map(log -> {
		            Map<String, Object> logData = new HashMap<>();
		            User logUser = log.getUser(); // 로그 작성자
		            logData.put("author", logUser != null ? logUser.getName() : "익명");

		            logData.put("id", log.getLogId());
		            logData.put("content", log.getLogContent());

		            // 절대 시간
		            LocalDateTime createdDate = log.getLogCreatedDate();
		            logData.put("time", createdDate);

		            // 상대 시간 추가
		            String timeAgo = SNSTime.getTimeAgo(createdDate);
		            logData.put("timeAgo", timeAgo);

		            // 좋아요 수 계산
		            int likeCount = log.getLogLikes().size();
		            logData.put("likes", likeCount);

		            // 댓글 수
		            logData.put("comments", log.getLogComments());
		            
		            // 즐겨찾기 수
		            int bookmarkCount = log.getBookmarkedUsers().size(); // Lazy 로딩 발생
		            logData.put("bookmarks", bookmarkCount);
		            
		            // 첨부된 이미지 목록
		            logData.put("images", log.getLogAttachedFiles().stream()
		                    .map(LogAttachedFile::getLog_saved_filename)
		                    .toList());
		            // 본인 여부 확인
		            logData.put("isAuthor", log.getUser().equals(user)); 

		            return logData;
		        }).toList();

		        return ResponseEntity.ok(response);
		    } catch (Exception e) {
		        e.printStackTrace();
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving logs");
		    }
		}

		@PostMapping("/{logId}/like")
		public ResponseEntity<?> toggleLike(@PathVariable("logId") Long logId) {
		    try {
		        Log log = logRepository.findById(logId)
		                .orElseThrow(() -> new IllegalArgumentException("Log not found"));

		        User user = getAuthenticatedUser();
		        if (user == null) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		        }

		        // 좋아요 상태 확인 및 토글
		        Optional<LogLikes> existingLikeOpt = logLikeRepository.findByUserAndLog(user, log);
		        boolean isLiked;

		        if (existingLikeOpt.isPresent()) {
		            logLikeRepository.delete(existingLikeOpt.get()); // 명시적 삭제
		            isLiked = false;
		        } else {
		            LogLikes newLike = new LogLikes(user, log, true);
		            logLikeRepository.save(newLike); // 새 좋아요 저장
		            isLiked = true;
		        }

		        int totalLikes = logLikeRepository.countByLog(log);

		        return ResponseEntity.ok(Map.of(
		            "isLiked", isLiked,
		            "totalLikes", totalLikes
		        ));
		    } catch (Exception e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error toggling like: " + e.getMessage());
		    }
		}
		@PostMapping("/{logId}/bookmark")
		public ResponseEntity<?> toggleBookmark(@PathVariable("logId") Long logId) {
		    try {
		        Log log = logRepository.findById(logId)
		                .orElseThrow(() -> new IllegalArgumentException("Log not found"));

		        User user = getAuthenticatedUser();
		        if (user == null) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		        }

		        boolean isBookmarked;
		        if (log.getBookmarkedUsers().contains(user)) {
		            log.getBookmarkedUsers().remove(user); // 즐겨찾기 취소
		            isBookmarked = false;
		        } else {
		            log.getBookmarkedUsers().add(user); // 즐겨찾기 추가
		            isBookmarked = true;
		        }

		        logRepository.save(log);

		        int totalBookmarks = log.getBookmarkedUsers().size();

		        return ResponseEntity.ok(Map.of(
		            "isBookmarked", isBookmarked,
		            "totalBookmarks", totalBookmarks
		        ));
		    } catch (Exception e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error toggling bookmark: " + e.getMessage());
		    }
		}











}

package com.example.hama.controller.log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.dto.LogDTO;
import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogAttachedFile;
import com.example.hama.model.log.LogWrite;
import com.example.hama.model.user.User;
import com.example.hama.repository.LogFileRepository;
import com.example.hama.repository.LogRepository;
import com.example.hama.repository.ReplyRepository;
import com.example.hama.service.LogFileService;
import com.example.hama.service.LogService;
import com.example.hama.util.SNSTime;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("log")
public class LogController {
	
	@Value("${file.upload.path}")
	private String uploadPath;

    private final LogService logService;
	private final LogFileService logFileService;
	private final LogRepository logRepository;
	private final ReplyRepository replyRepository;
	private final LogFileRepository logFileRepository;
	
	    @GetMapping("indexLog")
	    public String showIndexLog() {
	        return "log/indexLog"; // src/main/resources/templates/log/indexLog.html
	    }
	    @GetMapping("/log")
	    public String indexLog(Model model, @AuthenticationPrincipal UserDetails user) {
	        String nickname = user.getUsername(); // 로그인된 사용자 닉네임 가져오기
	        model.addAttribute("nickname", nickname);
	        return "indexLog"; // indexLog.html로 이동
	    }
		// 일기 등록
		@PostMapping("add")
		public ResponseEntity<?> write(
		    @Validated @ModelAttribute LogWrite logWrite,
		    @RequestParam(value = "parentId", required = false) Long parentId,
		    @RequestParam(value = "logFiles", required = false) List<MultipartFile> logFiles
		) throws IOException {

		    Log log = LogWrite.toLog(logWrite);
		    
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
		    response.put("content", log.getLogContent());
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
		        List<Log> logs = logRepository.findAll();
		        List<Map<String, Object>> response = logs.stream().map(log -> {
		            Map<String, Object> logData = new HashMap<>();
		            
		            logData.put("id", log.getLogId());
		            logData.put("content", log.getLogContent());
		            logData.put("author", "익명");
		            // 절대 시간
		            LocalDateTime createdDate = log.getLogCreatedDate();
		            logData.put("time", createdDate); 

		            // 상대 시간 추가
		            String timeAgo = SNSTime.getTimeAgo(createdDate); // 상대 시간 계산
		            logData.put("timeAgo", timeAgo); // timeAgo 필드 추가
		            logData.put("likes", log.getLogLikes());
		            logData.put("comments", log.getLogComments());
		            logData.put("images", log.getLogAttachedFiles().stream()
		            	    .map(LogAttachedFile::getLog_saved_filename) // 파일명만 반환
		            	    .toList());
		            return logData;
		        }).toList();

		        return ResponseEntity.ok(response);
		    } catch (Exception e) {
		        e.printStackTrace();
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving logs");
		    }
		}

		// 좋아요 처리
		@PostMapping("/{logId}/like")
		public ResponseEntity<?> addLike(@PathVariable("logId") Long logId) {
		    try {
		        // 로그를 조회
		        Log log = logRepository.findById(logId).orElseThrow(() -> new IllegalArgumentException("Log not found"));

		        // 좋아요 수 증가
		        log.setLogLikes(log.getLogLikes() + 1);
		        logRepository.save(log);

		        return ResponseEntity.ok("Like added successfully");
		    } catch (Exception e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding like: " + e.getMessage());
		    }
		}

		@GetMapping("/{logId}")
		public ResponseEntity<?> getLogById(@PathVariable("logId") Long logId) {
		    try {
		        Log log = logRepository.findById(logId)
		            .orElseThrow(() -> new IllegalArgumentException("Log not found"));

		        // 상대 시간 계산 추가
		        String timeAgo = SNSTime.getTimeAgo(log.getLogCreatedDate());

		        Map<String, Object> logData = new HashMap<>();
		        logData.put("id", log.getLogId());
		        logData.put("content", log.getLogContent());
		        logData.put("author", "익명");
		        logData.put("time", log.getLogCreatedDate());
		        logData.put("timeAgo", timeAgo); // 상대 시간 추가
		        logData.put("images", log.getLogAttachedFiles().stream()
		            .map(LogAttachedFile::getLog_saved_filename)
		            .toList());

		        return ResponseEntity.ok(logData);
		    } catch (Exception e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                             .body("Error retrieving log details: " + e.getMessage());
		    }
		}





}

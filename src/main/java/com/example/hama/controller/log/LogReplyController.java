package com.example.hama.controller.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.dto.ReplyDTO;
import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogLikes;
import com.example.hama.model.log.LogReplyLikes;
import com.example.hama.model.log.LogReply;
import com.example.hama.model.user.User;
import com.example.hama.repository.LogRepository;
import com.example.hama.repository.UserRepository;
import com.example.hama.repository.LogLikeRepository;
import com.example.hama.repository.LogReplyLikeRepository;
import com.example.hama.repository.LogReplyRepository;
import com.example.hama.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("reply")
@RequiredArgsConstructor
public class LogReplyController {
	
	private final LogRepository logRepository;
	private final LogLikeRepository logLikeRepository;
 	private final LogReplyRepository logReplyRepository;
 	private final LogReplyLikeRepository logReplyLikeRepository;
 	private final UserService userService;
 	private final UserRepository userRepository;
	
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
	@PostMapping("/log/{logId}/reply")
	public ResponseEntity<?> addReply(@PathVariable("logId") Long logId
									, @RequestBody Map<String, Object> request
									, @RequestParam(name = "name", required = false) String nickname) {
	    try {
	        // 댓글 내용 및 부모 ID 추출
	        String logReplyContent = (String) request.get("logReplyContent");
	        Long parentReplyId = request.containsKey("parentReplyId") && request.get("parentReplyId") != null
	                ? Long.parseLong(request.get("parentReplyId").toString()) : null;

	        if (logReplyContent == null || logReplyContent.trim().isEmpty()) {
	            throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
	        }
		    User currentUser = getAuthenticatedUser();
		      if(currentUser == null) {
		    	  return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		      }
	        Log log = logRepository.findById(logId)
	                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

	        // 댓글 객체 생성
	        LogReply logReply = new LogReply();
	        logReply.setUser(currentUser);
	        logReply.setLog(log);
	        logReply.setLogReplyContent(logReplyContent);
	        logReply.setLogCreatedTime(LocalDateTime.now());
	        logReply.setAuthor(currentUser.getUserId().equals(logReply.getUser().getUserId())); // 댓글 작성자 여부 체크

	        // 부모 댓글 검증 및 설정
	        if (parentReplyId != null) {
	            LogReply parentReply = logReplyRepository.findById(parentReplyId)
	                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
	            logReply.setParentReply(parentReply);
	        }

	        LogReply savedReply = logReplyRepository.save(logReply);

	        return ResponseEntity.ok(Map.of(
	                "status", "success",
	                "replyId", savedReply.getLogReplyId()
	        ));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("댓글 추가 중 오류 발생: " + e.getMessage());
	    }
	}
	// 댓글 삭제
	@DeleteMapping("/log/{replyId}/del")
	public ResponseEntity<?> deleteReply(@PathVariable("replyId") Long replyId) {
	    try {
	        if (replyId == null) {
	            throw new IllegalArgumentException("댓글 ID가 유효하지 않습니다.");
	        }

	        // 댓글 조회
	        LogReply logReply = logReplyRepository.findById(replyId)
	                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

	        // 댓글 삭제 상태로 변경 (isDeleted 추가 가정)
	        logReply.setDeleted(true); // 삭제 상태 플래그 설정
	        logReply.setLogReplyContent("댓글이 삭제되었습니다."); // 내용 변경
	        logReplyRepository.save(logReply); // DB 저장

	        // 댓글 수 동기화
	        Log log = logReply.getLog();
	        log.setLogComments(logReplyRepository.countRepliesByLogId(log.getLogId()).intValue());
	        logRepository.save(log);

	        return ResponseEntity.ok("댓글이 삭제 처리되었습니다.");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("댓글 삭제 실패: " + e.getMessage());
	    }
	}
	// 댓글 좋아요

	@PostMapping("/log/{replyId}/like")
	public ResponseEntity<?> toggleReplyLike(@PathVariable("replyId") Long replyId) {
	    try {
	        if (replyId == null) {
	            throw new IllegalArgumentException("댓글 ID가 유효하지 않습니다.");
	        }

	        LogReply logReply = logReplyRepository.findById(replyId)
	                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

	        User user = getAuthenticatedUser();
	        if (user == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
	        }

	        Optional<LogReplyLikes> existingLikeOpt = logReplyLikeRepository.findByUserAndLogReply(user, logReply);
	        boolean isLiked;

	        if (existingLikeOpt.isPresent()) {
	            logReplyLikeRepository.delete(existingLikeOpt.get());
	            isLiked = false;
	        } else {
	            LogReplyLikes newLike = new LogReplyLikes(user, logReply);
	            logReplyLikeRepository.save(newLike);
	            isLiked = true;
	        }

	        int totalLikes = logReplyLikeRepository.countByLogReply(logReply);

	        return ResponseEntity.ok(Map.of(
	                "isLiked", isLiked,
	                "totalLikes", totalLikes
	        ));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("댓글 좋아요 처리 중 오류 발생: " + e.getMessage());
	    }
	}



	// 댓글 개수 동기화 
	@GetMapping("/log/{postId}/count")
	public ResponseEntity<?> getReplyCount(@PathVariable("postId") Long postId) {
		try {
	        Long count = logReplyRepository.countRepliesByLogId(postId);
	        
	        if (count == null) { // null 값 검증 추가
	            count = 0L; // 기본값 설정
	        }
	        return ResponseEntity.ok(Map.of("count", count)); // JSON 형식 통일
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 수 조회 중 오류 발생");
	    }
	}
	// 댓글 목록 조회
	@GetMapping("/log/{logId}/replies")
	public ResponseEntity<?> getReplies(@PathVariable("logId") Long logId) {
	    try {
	        Log log = logRepository.findById(logId)
	                .orElseThrow(() -> new IllegalArgumentException("Invalid log ID"));
		    User currentUser = getAuthenticatedUser();
	        if(currentUser == null) {
	    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
	        }
	        List<LogReply> logReplies = logReplyRepository.findByLog(log);
	        List<ReplyDTO> replyHierarchy = buildReplyHierarchy(logReplies, currentUser);
	        return ResponseEntity.ok(Map.of("status", "success", "replies", replyHierarchy));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", "error", "message", "댓글 조회 실패" + e.getMessage()));
	    }
	}
    // 댓글 ID를 기준으로 매핑
	private List<ReplyDTO> buildReplyHierarchy(List<LogReply> logReplies, User currentUser) {
	    Map<Long, ReplyDTO> replyMap = new HashMap<>();
	    List<ReplyDTO> roots = new ArrayList<>();

	    for (LogReply logReply : logReplies) {
	        int likeCount = logReplyLikeRepository.countByLogReply(logReply);
	        ReplyDTO dto = new ReplyDTO(logReply, currentUser, likeCount);
	        replyMap.put(dto.getId(), dto);
	    }

	    for (ReplyDTO dto : replyMap.values()) {
	        if (dto.getParentReplyId() == null) {
	            roots.add(dto);
	        } else {
	            ReplyDTO parent = replyMap.get(dto.getParentReplyId());
	            if (parent != null) {
	                parent.getChildReplies().add(dto);
	            } else {
	                roots.add(dto);
	            }
	        }
	    }
	    return roots;
	}



	@PutMapping("/log/{replyId}/edit")
	public ResponseEntity<?> editReply(@PathVariable("replyId") Long replyId, @RequestBody Map<String, String> request) {
	    try {
	        // 댓글 조회
	        LogReply logReply = logReplyRepository.findById(replyId)
	                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

	        // 댓글 내용 수정
	        String newContent = request.get("logReplyContent");
	        if (newContent == null || newContent.trim().isEmpty()) {
	            throw new IllegalArgumentException("수정할 내용이 비어있습니다.");
	        }
	        logReply.setLogReplyContent(newContent);
	        logReplyRepository.save(logReply); // 저장

	        return ResponseEntity.ok(Map.of("status", "success", "message", "댓글 수정 완료"));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("status", "error", "message", e.getMessage()));
	    }
	}




}

package com.example.hama.controller.board;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.*;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.board.Board;
import com.example.hama.model.board.Reply;
import com.example.hama.model.board.ReplyDto;
import com.example.hama.model.user.User;
import com.example.hama.repository.board.BoardRepository;
import com.example.hama.repository.board.ReplyRepository;
import com.example.hama.service.UserService;
import com.example.hama.service.board.ReplyService;

import ch.qos.logback.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("reply")
@RequiredArgsConstructor
@Slf4j
public class ReplyController {
	
	private final UserService userService;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final ReplyService replyService;

    // **1. 댓글 등록**
    @PostMapping("{boardId}")
    public ResponseEntity<String> writeReply(
            @PathVariable("boardId") Long boardId,
            @RequestBody Map<String, Object> request
    ) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        String rpContent = request.get("rpContent").toString();
        boolean isSecret = Boolean.parseBoolean(request.get("isSecret").toString());

        if (rpContent == null || rpContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("댓글 내용을 입력하세요.");
        }

        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        Reply reply = new Reply();
        reply.setBoard(board);
        reply.setRpContent(rpContent);
        reply.setRpCreatedTime(LocalDateTime.now());
        reply.setUser(user);
        reply.setSecret(isSecret); // 비밀댓글 설정
        replyRepository.save(reply);

        return ResponseEntity.ok("댓글 등록 성공");
    }


    // **2. 대댓글 등록**
    @PostMapping("child")
    public ResponseEntity<?> writeChildReply(@RequestBody Map<String, Object> request) {
        log.info("대댓글 내용: {}", request.get("rpContent"));
        log.info("부모 댓글 ID: {}", request.get("parentReplyId"));
     // 현재 로그인된 사용자 가져오기
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 부모 댓글 ID 검증
        Long parentReplyId = Long.valueOf(request.get("parentReplyId").toString());
        Reply parentReply = replyRepository.findById(parentReplyId).orElse(null);

        if (parentReply == null) {
            return ResponseEntity.badRequest().body("존재하지 않는 부모 댓글입니다.");
        }
        
        boolean isSecret = Boolean.parseBoolean(request.get("isSecret").toString());

        // 대댓글 저장
        Reply childReply = new Reply();
        childReply.setRpContent(request.get("rpContent").toString());
        childReply.setRpCreatedTime(LocalDateTime.now());
        childReply.setParentReply(parentReply);
        childReply.setBoard(parentReply.getBoard());
        childReply.setUser(user); // 작성자 설정
        childReply.setSecret(isSecret); // 비밀댓글 여부 설정

        replyRepository.save(childReply);
        return ResponseEntity.ok("대댓글 등록 성공");
    }


    // **3. 댓글 및 대댓글 조회 (재귀 구조)**
    @GetMapping("{boardId}")
    public ResponseEntity<List<ReplyDto>> getReplies(@PathVariable("boardId") Long boardId) {
        User currentUser = getAuthenticatedUser(); // 현재 로그인 사용자
        if (currentUser == null) {
            return ResponseEntity.status(401).build(); // 비로그인 사용자 접근 방지
        }

        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 부모 댓글만 조회
        List<Reply> parentReplies = replyRepository.findByBoardAndParentReplyIsNull(board);

        // Reply를 ReplyDto로 변환
        List<ReplyDto> replyDtos = parentReplies.stream()
            .map(reply -> mapToReplyDto(reply, currentUser))
            .collect(Collectors.toList());

        return ResponseEntity.ok(replyDtos);
    }



    // **4. 댓글 수정**
    @PutMapping("/{replyId}")
    public ResponseEntity<?> updateReply(@PathVariable("replyId") Long replyId, @RequestBody Reply request) {
        log.info("댓글 수정 요청 ID: {}", replyId);

        // 댓글 조회 및 수정
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        reply.setRpContent(request.getRpContent());
        replyRepository.save(reply);

        return ResponseEntity.ok(reply);
    }

    // **5. 대댓글 수정**
    @PutMapping("/child/{replyId}")
    public ResponseEntity<?> updateChildReply(@PathVariable("replyId") Long replyId, @RequestBody Reply request) {
        log.info("대댓글 수정 요청 ID: {}", replyId);

        // 대댓글 조회 및 수정
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다."));
        reply.setRpContent(request.getRpContent());
        replyRepository.save(reply);

        return ResponseEntity.ok(reply);
    }

    // **6. 댓글 삭제**
    @DeleteMapping("/{replyId}")
    public ResponseEntity<String> removeReply(@PathVariable("replyId") Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 댓글 삭제
        replyRepository.delete(reply);
        return ResponseEntity.ok("댓글 삭제 성공");
    }

    // **7. 대댓글 삭제**
    @DeleteMapping("/child/{replyId}")
    public ResponseEntity<String> removeChildReply(@PathVariable("replyId") Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다."));

        // 대댓글 삭제
        replyRepository.delete(reply);
        return ResponseEntity.ok("대댓글 삭제 성공");
    }
    
    
    // 댓글 개수 반환 API
    @GetMapping("/count")
    public ResponseEntity<Integer> getReplyCount(@RequestParam("boardId") Long boardId) {
        int count = replyService.countRepliesByBoardId(boardId);
        return ResponseEntity.ok(count);
    }
    
    
 // 좋아요 추가
    @PostMapping("{replyId}/like")
    public ResponseEntity<String> addLike(@PathVariable("replyId") Long replyId) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        String result = replyService.addLike(replyId, currentUser);
        return ResponseEntity.ok(result);
    }

    // 좋아요 제거
    @DeleteMapping("{replyId}/like")
    public ResponseEntity<String> removeLike(@PathVariable("replyId") Long replyId) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        String result = replyService.removeLike(replyId, currentUser);
        return ResponseEntity.ok(result);
    }

    // 좋아요 개수 조회
    @GetMapping("{replyId}/like-count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable("replyId") Long replyId) {
        int likeCount = replyService.getLikeCount(replyId);
        return ResponseEntity.ok(likeCount);
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
    
    private ReplyDto mapToReplyDto(Reply reply, User currentUser) {
        boolean isAccessible = isAccessibleReply(reply, currentUser);

        // 비밀댓글이면 접근 가능 여부에 따라 내용 처리
        String content = isAccessible ? reply.getRpContent() : "비밀댓글입니다.";

        boolean isLiked = reply.getLikes().stream()
            .anyMatch(like -> like.getUser().getUserId().equals(currentUser.getUserId()));

        List<ReplyDto> childReplyDtos = reply.getChildReplies().stream()
            .map(childReply -> mapToReplyDto(childReply, currentUser))
            .collect(Collectors.toList());

        return new ReplyDto(
                reply.getReplyId(),
                content,
                reply.getUser().getName(),
                reply.getUser().getUserId(),
                reply.getLikes().size(),
                isLiked,
                childReplyDtos,
                reply.getRpCreatedTime(),
                reply.isSecret(), // 비밀댓글 여부
                isAccessible // 접근 가능 여부
        );
    }

    private boolean isAccessibleReply(Reply reply, User currentUser) {
        if (!reply.isSecret()) {
            return true; // 공개 댓글은 접근 가능
        }
        if (currentUser == null) {
            return false; // 비로그인 사용자는 비밀댓글 접근 불가
        }
        return reply.getUser().getUserId().equals(currentUser.getUserId()) || // 댓글 작성자
               reply.getBoard().getUser().getUserId().equals(currentUser.getUserId()); // 게시글 작성자
    }




  
    }
    
    

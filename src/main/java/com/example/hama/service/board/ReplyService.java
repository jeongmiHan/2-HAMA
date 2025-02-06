package com.example.hama.service.board;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.hama.model.board.Board;
import com.example.hama.model.board.Reply;
import com.example.hama.model.board.ReplyDto;
import com.example.hama.model.board.likes;
import com.example.hama.model.user.User;
import com.example.hama.repository.UserRepository;
import com.example.hama.repository.board.BoardRepository;
import com.example.hama.repository.board.LikesRepository;
import com.example.hama.repository.board.ReplyRepository;


@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;

    public ReplyService(ReplyRepository replyRepository, BoardRepository boardRepository
          , UserRepository userRepository, LikesRepository likesRepository) {
        this.replyRepository = replyRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.likesRepository = likesRepository;
    }

    // 댓글 추가
    public void addReply(Long boardId, String userId, String rpContent) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다: " + boardId));

        User user = null; // 기본값을 null로 설정
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));
        }

        Reply reply = new Reply();
        reply.setBoard(board);
        reply.setUser(user);
        reply.setRpContent(rpContent);
        reply.setRpCreatedTime(LocalDateTime.now());
        replyRepository.save(reply);
    }

    

    // 특정 게시글의 댓글 목록 가져오기
    public List<Reply> getRepliesByBoardId(Long boardId) {
       List<Reply> replies = replyRepository.findByBoardBoardId(boardId);
        
        // 각 댓글에 대해 대댓글을 조회
        for (Reply reply : replies) {
            List<Reply> childReplies = replyRepository.findByParentReply(reply);
            reply.setChildReplies(childReplies);  // 대댓글 추가
        }

        return replies;
    }

    

    // 댓글 삭제
    public void deleteReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다: " + replyId));
        replyRepository.delete(reply);
    }

    // 댓글 개수 조회
    public int countRepliesByBoardId(Long boardId) {
        return replyRepository.countByBoard_BoardId(boardId); // 댓글 개수 조회
    }
    
    
    // 게시글 ID 리스트를 기반으로 댓글 수 조회
    public Map<Long, Integer> getReplyCountsByBoardIds(List<Board> boards) {
        Map<Long, Integer> replyCountMap = new HashMap<>();
        for (Board board : boards) {
            // Null 체크
            if (board.getBoardId() != null) {
                int count = replyRepository.countByBoard_BoardId(board.getBoardId());
                replyCountMap.put(board.getBoardId(), count);
            } else {
                replyCountMap.put(0L, 0); // 기본값 처리
            }
        }
        return replyCountMap;
    }


    public Map<String, Object> toggleLike(Long replyId, User user) {
        Map<String, Object> response = new HashMap<>();

        // ✅ 현재 댓글 가져오기
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. ID: " + replyId));

        // ✅ 좋아요 여부 확인
        Optional<likes> existingLike = likesRepository.findByUserAndReply(user, reply);

        if (existingLike.isPresent()) {
            // ✅ 좋아요 취소
            likesRepository.delete(existingLike.get());
            response.put("isLiked", false); // 🤍
        } else {
            // ✅ 좋아요 추가
            likes newLike = new likes();
            newLike.setReply(reply);
            newLike.setUser(user);
            likesRepository.save(newLike);

            response.put("isLiked", true); // ❤️
        }

        // ✅ 최신 좋아요 개수 조회
        int likeCount = likesRepository.countByReply_ReplyId(replyId);
        response.put("likeCount", likeCount);

        return response;
    }
    
    
    public boolean isLikedByUser(Long replyId, User currentUser) {
        if (currentUser == null) {
            return false; // 로그인하지 않은 사용자는 기본적으로 좋아요 X
        }

        // ✅ 댓글 정보 가져오기
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. ID: " + replyId));

        // ✅ 좋아요 여부 확인
        return likesRepository.findByUserAndReply(currentUser, reply).isPresent();
    }




}
    

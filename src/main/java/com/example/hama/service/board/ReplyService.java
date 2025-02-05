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


    public String addLike(Long replyId, User user) {
        try {
            Optional<likes> existingLike = likesRepository.findByUser_UserIdAndReply_ReplyId(user.getUserId(), replyId);

            if (existingLike.isPresent()) {
                return "이미 좋아요를 눌렀습니다.";
            }

            Reply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. ID: " + replyId));

            likes like = new likes();
            like.setReply(reply);
            like.setUser(user);

            likesRepository.save(like);

            return "좋아요를 추가했습니다.";
        } catch (Exception e) {
            System.err.println("좋아요 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 자세한 스택 추적 출력
            throw new RuntimeException("좋아요 처리 중 오류 발생: " + e.getMessage());
        }
    }


    // 좋아요 제거
    public String removeLike(Long replyId, User user) {
        Optional<likes> existingLike = likesRepository.findByUser_UserIdAndReply_ReplyId(user.getUserId(), replyId);

        if (existingLike.isEmpty()) {
            return "좋아요를 누르지 않았습니다.";
        }

        likesRepository.delete(existingLike.get());

        return "좋아요를 취소했습니다.";
    }

    // 특정 댓글의 좋아요 개수 조회
    public int getLikeCount(Long replyId) {
        return likesRepository.countByReply_ReplyId(replyId);
    }
    
    public List<ReplyDto> getReplies(Long boardId, String currentUserId) {
        List<Reply> replies = replyRepository.findByBoardBoardId(boardId);

        return replies.stream().map(reply -> {
            boolean isAccessible = isAccessibleReply(reply, currentUserId);
            return new ReplyDto(
                reply.getReplyId(),
                isAccessible ? reply.getRpContent() : "비밀댓글입니다.",
                reply.getUser().getName(),
                reply.getUser().getUserId(),
                reply.getLikeCount(),
                reply.isLikedByUser(currentUserId),
                getChildReplies(reply, currentUserId),
                reply.getRpCreatedTime(),
                reply.isSecret(),  // 비밀댓글 여부
                isAccessible       // 접근 가능 여부
            );
        }).collect(Collectors.toList());
    }

    private boolean isAccessibleReply(Reply reply, String currentUserId) {
        return reply.isSecret()
            ? reply.getUser().getUserId().equals(currentUserId) ||
              reply.getBoard().getUser().getUserId().equals(currentUserId)
            : true;
    }

    private List<ReplyDto> getChildReplies(Reply parentReply, String currentUserId) {
        return parentReply.getChildReplies().stream()
            .map(childReply -> {
                boolean isAccessible = isAccessibleReply(childReply, currentUserId);
                return new ReplyDto(
                    childReply.getReplyId(),
                    isAccessible ? childReply.getRpContent() : "비밀댓글입니다.",
                    childReply.getUser().getName(),
                    childReply.getUser().getUserId(),
                    childReply.getLikeCount(),
                    childReply.isLikedByUser(currentUserId),
                    getChildReplies(childReply, currentUserId),
                    childReply.getRpCreatedTime(),
                    childReply.isSecret(),  // 비밀댓글 여부
                    isAccessible            // 접근 가능 여부
                );
            }).collect(Collectors.toList());
    }


}
    

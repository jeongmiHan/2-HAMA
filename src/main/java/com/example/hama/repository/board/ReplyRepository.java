package com.example.hama.repository.board;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hama.model.board.Board;
import com.example.hama.model.board.Reply;


public interface ReplyRepository extends JpaRepository<Reply, Long> {

    // 특정 게시글의 모든 댓글 가져오기
    @Query("SELECT r FROM Reply r WHERE r.board = :board")
    List<Reply> findAllRepliesByBoard(@Param("board") Board board);

    // 부모 댓글이 없는 최상위 댓글 조회 (대댓글 제외)
    @Query("SELECT r FROM Reply r WHERE r.board = :board AND r.parentReply IS NULL")
    List<Reply> findTopLevelRepliesByBoard(@Param("board") Board board);

    // 부모 댓글과 자식 댓글을 한 번에 조회 (EntityGraph 사용)
    @EntityGraph(attributePaths = {"childReplies"})
    @Query("SELECT r FROM Reply r WHERE r.board = :board AND r.parentReply IS NULL")
    List<Reply> findRepliesWithChildren(@Param("board") Board board);

    // 특정 부모 댓글의 자식 대댓글 조회
    List<Reply> findByParentReply(Reply parentReply);

    // 특정 게시글의 댓글 ID로 조회
    List<Reply> findByBoardBoardId(Long boardId);

    // 특정 게시글 ID로 부모 댓글이 없는 최상위 댓글 조회
    List<Reply> findByBoardBoardIdAndParentReplyIsNull(Long boardId);

	List<Reply> findByBoardAndParentReplyIsNull(Board board);

	int countByBoard_BoardId(Long boardId);

	List<Reply> findByBoard_BoardId(Long boardId);
}



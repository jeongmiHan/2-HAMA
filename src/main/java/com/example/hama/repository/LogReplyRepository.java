package com.example.hama.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogReply;


public interface LogReplyRepository extends JpaRepository<LogReply, Long> {

	List<LogReply> findByLog(Log log);
	List<LogReply> findByParentReply(LogReply parentReply);

	
	// 댓글 총 개수 조회
	@Query("SELECT COUNT(r) FROM LogReply r WHERE r.log.logId = :logId")
	Long countRepliesByLogId(@Param("logId") Long logId);
	
	// 자식 대댓글 리스트
	@Query("SELECT r FROM LogReply r LEFT JOIN FETCH r.childReplies WHERE r.parentReply.id = :parentId")
	List<LogReply> findChildReplies(@Param("parentId") Long parentId);
}

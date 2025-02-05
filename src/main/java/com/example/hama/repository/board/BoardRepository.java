package com.example.hama.repository.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.board.Board;



public interface BoardRepository extends JpaRepository<Board, Long>{
	
	//검색 결과
	
	Page<Board> findAll(Pageable pageable);
	//게시물 등록
	//save(board);

	Page<Board> findByBdTitleContaining(String titleKeyword, Pageable pageable);
    Page<Board> findByBdContentContaining(String contentKeyword, Pageable pageable);
    Page<Board> findByBdTitleContainingOrBdContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);
	
	//게시물 검색
	//findById(id);
	
	//게시물 수정
	//save(board);
	
	//게시물 삭제
	//deleteById(id);
	
	//게시글 전체 목록
	//findAll(); 	
}

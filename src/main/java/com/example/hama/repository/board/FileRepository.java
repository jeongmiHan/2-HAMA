package com.example.hama.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.board.AttachedFile;
import com.example.hama.model.board.Board;




public interface FileRepository extends JpaRepository<AttachedFile, Long> {

	AttachedFile findByBoard(Board board);


	
	
}

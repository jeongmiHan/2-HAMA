package com.example.hama.model.board;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoardWrite {
	@NotBlank
	private String bdTitle;				//제목
	@NotBlank
	private String bdContent;            //내용
	
	public static Board toBoard(BoardWrite boardWrite) {
		Board board = new Board();
		
		board.setBdTitle(boardWrite.getBdTitle());
		board.setBdContent(boardWrite.getBdContent());
		board.setHit(0L);
		board.setBdCreatedDate(LocalDateTime.now());
		
		
		return board;
		
	}
	
}


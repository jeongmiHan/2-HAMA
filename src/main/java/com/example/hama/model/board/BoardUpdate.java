package com.example.hama.model.board;

import java.time.LocalDateTime;

import com.example.hama.model.user.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoardUpdate {
    private Long boardId;
    
    @NotBlank
    private String bdTitle;  // 제목
    
    @NotBlank
    private String bdContent;  // 내용
    
    private User user;
    private Long hit;
    private LocalDateTime bdCreatedDate;
    
    // 파일 삭제 여부
    private boolean fileRemoved;


    // BoardUpdate 객체를 Board 객체로 변환하는 메서드
    public static Board toBoard(BoardUpdate boardUpdate) {
        Board board = new Board();
        
        board.setBoardId(boardUpdate.getBoardId());
        board.setBdTitle(boardUpdate.getBdTitle());
        board.setBdContent(boardUpdate.getBdContent());
        board.setUser(boardUpdate.getUser());
        board.setHit(boardUpdate.getHit());
        board.setBdCreatedDate(boardUpdate.getBdCreatedDate()); // 수정된 bdCreatedDate 설정
        
        
        
        return board;
    }
}

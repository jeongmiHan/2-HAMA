package com.example.hama.model.board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.hama.model.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Board {
	@Id
	@Column(name="board_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long boardId;				//게시물 아이디 
	
	
	@Column(name="bd_title", length = 1000, nullable = false)
	private String bdTitle;				//제목
	
	@Column(name="bd_content",length = 4000, nullable = false)
	@Lob
	private String bdContent;            //내용
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="user_id", nullable= false)
	private User user;				//작성자
	
	@Column(name="bd_hit")
	private Long hit;					//조회수
	
	private LocalDateTime bdCreatedDate;	//작성일
	
	// 댓글과의 관계 설정 (부모-자식 관계)
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();
	
	
	//조회수 1증가
	public void addHit() {
		this.hit++;
	}
	

	public static BoardUpdate toBoardUpdate(Board board) {
		BoardUpdate boardUpdate = new BoardUpdate();
		boardUpdate.setBoardId(board.getBoardId());
		boardUpdate.setBdTitle(board.getBdTitle());
		boardUpdate.setBdContent(board.getBdContent());
		boardUpdate.setUser(board.getUser());
		boardUpdate.setHit(board.getHit());
		boardUpdate.setBdCreatedDate(board.getBdCreatedDate());
		
		return boardUpdate;
	}
	
	
}


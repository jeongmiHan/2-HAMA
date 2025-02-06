package com.example.hama.model.board;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter @ToString
@NoArgsConstructor
public class AttachedFile {
	
	@Id
	@Column(name="attachedFile_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long attachedFileId;  //첨부파일 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="board_id")
	private Board board;          //어떤 글에 첨부파일? => board
	
	private String original_filename; //원본 파일 이름
	private String saved_filename;    //저장할 파일 이름
	private Long file_size;           //파일 용량
	
	public AttachedFile(String original_filename, String saved_filename, Long file_size) {
		
		this.original_filename = original_filename;
		this.saved_filename = saved_filename;
		this.file_size = file_size;
	}
	
}

package com.example.hama.model.log;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
public class LogAttachedFile {
	@Id
	@Column(name="logAttachedFile_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logAttachedFileId; // 첨부파일 아이디
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="log_id")
	@JsonBackReference // 순환 참조 방지
	private Log log;// 어떤 글에 첨부파일? => board 
	
	private String log_original_filename; // 원본 파일 이름
	private String log_saved_filename; // 저장할 파일 이름
	private Long log_file_size;// 파일 용량
	
	public LogAttachedFile(String log_original_filename, String saved_filename, Long log_file_size)  {
		this.log_original_filename = log_original_filename;
		this.log_saved_filename = saved_filename;
		this.log_file_size = log_file_size;
		
		
	}

	
 }

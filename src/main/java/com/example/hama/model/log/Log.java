package com.example.hama.model.log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.hama.model.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Log {
	
	@Id
	@Column(name="log_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logId;	//일지 아이디
	
	@ManyToOne // 한명이 여러개의 일지를 쓰니까
	@JoinColumn(name="user_id")
	private User user;	   //사용자
	
	@Column
	private String logContent;	//내용
	
	@Column
	private LocalDateTime logCreatedDate;	//작성일
	private Long logHit;	            	//조회수
	@Column
    private int logLikes = 0;               //좋아요 수
    
    @Column
    private int logComments = 0;		    //댓글 수 
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Log parent;
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Log> children = new ArrayList<>();
    
	// 조회수 1증가
	public void addHit() {
		this.logHit++;
	}
	@OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference // 부모 객체 직렬화
	private List<Reply> replies = new ArrayList<>();
	
    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // 순환 참조 방지
    private List<LogAttachedFile> logAttachedFiles = new ArrayList<>();
	
	public static LogUpdate toLogUpdate(Log log) {
		LogUpdate logUpdate = new LogUpdate();
		
		logUpdate.setLogId(log.getLogId());
		logUpdate.setUser(log.getUser());
		logUpdate.setLogContent(log.getLogContent());
		logUpdate.setLogCreatedDate(log.getLogCreatedDate());
		logUpdate.setLogHit(log.getLogHit());
		
		return logUpdate;
	}
	
    // 첨부파일 추가 메서드
    public void addAttachedFile(LogAttachedFile logAttachedFile) {
        logAttachedFiles.add(logAttachedFile);
        logAttachedFile.setLog(this);
    }
    
    
}






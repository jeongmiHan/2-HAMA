package com.example.hama.model.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
public class Reply {
	
	@Id
	@Column(name="log_reply_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long logReplyId;
	
	@ManyToOne 
	@JoinColumn(name="log_id")
	@JsonIgnore // 부모 댓글에서 참조 방지
	private Log log;

	@Column
	private String logReplyContent;
	
	private LocalDateTime logCreatedTime;
	
	@ManyToOne
	@JoinColumn(name = "parent_reply_id")
	@JsonBackReference // 부모 댓글 참조 시 직렬화 제외
	private Reply parentReply;

	@OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference // 자식 댓글 직렬화 포함
	private List<Reply> childReplies = new ArrayList<>();
	
	// 권한이 있으면 true
	@Transient
	private boolean writer;
	
	private boolean isDeleted = false;
	
}















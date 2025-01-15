package com.example.hama.model.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.hama.model.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
	
	@ManyToOne(fetch = FetchType.EAGER) // 한명이 여러개의 일지를 쓰니까
	@JoinColumn(name="user_id")
	private User user;	   //사용자
	
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















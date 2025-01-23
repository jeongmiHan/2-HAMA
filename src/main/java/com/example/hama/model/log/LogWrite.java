package com.example.hama.model.log;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.example.hama.model.user.User;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LogWrite {
	
	private Long logId;
	private User user;
	@NotBlank
	private String logContent;	
    
	
	public static Log toLog(LogWrite logWrite) {
		Log log = new Log();
		
		log.setLogId(logWrite.getLogId());
		log.setUser(logWrite.getUser());
		log.setLogContent(logWrite.getLogContent());
		log.setLogCreatedDate(LocalDateTime.now());
		
		return log;
	}

	
}





package com.example.hama.model.log;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LogWrite {
	
	private Long logId;
	@NotBlank
	private String logContent;	
    
	
	public static Log toLog(LogWrite logWrite) {
		Log log = new Log();
		
		log.setLogId(logWrite.getLogId());
		log.setLogContent(logWrite.getLogContent());
		log.setLogCreatedDate(LocalDateTime.now());
		log.setLogHit(0L);
		
		return log;
	}

	
}





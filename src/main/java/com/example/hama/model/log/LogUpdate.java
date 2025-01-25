package com.example.hama.model.log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.hama.model.user.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class LogUpdate {
	
	private Long logId;	  
	private User user;
	private String logContent;
	private List<LogLikes> logLikes;  
	private LocalDateTime logCreatedDate;
	
	public static Log toLog(LogUpdate logUpdate) {
		Log log = new Log();
		
		log.setLogId(log.getLogId());
		log.setUser(log.getUser());
		log.setLogContent(log.getLogContent());
		log.setLogCreatedDate(LocalDateTime.now());
		log.setLogLikes(log.getLogLikes());
		
		return log;
	}
}





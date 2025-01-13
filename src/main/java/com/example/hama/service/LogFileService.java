package com.example.hama.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.model.log.LogAttachedFile;

@Component
public class LogFileService {
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	public List<LogAttachedFile> logSaveFiles(List<MultipartFile> logFiles) {
	    List<LogAttachedFile> savedFiles = new ArrayList<>();
	    if (logFiles == null || logFiles.isEmpty()) {
	        System.out.println("No files to save.");
	        return savedFiles;
	    }

	    for (MultipartFile logFile : logFiles) {
	        try {
	            if (!logFile.isEmpty()) {
	                String originalFilename = logFile.getOriginalFilename();
	                Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
	                if (!Files.exists(uploadDir)) {
	                    Files.createDirectories(uploadDir);
	                }

	                String savedFilename = UUID.randomUUID() + "_" + originalFilename;
	                Path filePath = uploadDir.resolve(savedFilename);

	                logFile.transferTo(filePath.toFile()); // 파일을 즉시 저장
	                System.out.println("File saved at: " + filePath);

	                savedFiles.add(new LogAttachedFile(originalFilename, savedFilename, logFile.getSize()));
	            }
	        } catch (IOException e) {
	            // 예외 처리
	            System.err.println("Error saving file: " + e.getMessage());
	        }
	    }
	    return savedFiles;
	}

	public LogAttachedFile logSaveFile(MultipartFile logFile) {
	    if (logFile == null || logFile.isEmpty()) {
	        return null;
	    }

	    try {
	        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
	        if (!Files.exists(uploadDir)) {
	            Files.createDirectories(uploadDir); // 디렉토리 생성
	        }

	        String logOriginalFilename = logFile.getOriginalFilename();
	        String ext = logOriginalFilename.substring(logOriginalFilename.lastIndexOf("."));
	        String logSavedFilename = UUID.randomUUID().toString() + ext;

	        Path filePath = uploadDir.resolve(logSavedFilename);
	        logFile.transferTo(filePath.toFile()); // 파일 저장

	        return new LogAttachedFile(logOriginalFilename, logSavedFilename, logFile.getSize());
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}


	
	public boolean logDeleteFile(String fullpath) {
		// 파일 삭제 여부를 리턴할 변수
		boolean result = false;
		// 전달된 전체 경로로 File 객체 생성
		File delFile = new File(fullpath);
		
		// 해당 파일이 존재하면 삭제
		if(delFile.isFile()) {
			delFile.delete();
			return true;
		}
		return result;
	}

	
		
}

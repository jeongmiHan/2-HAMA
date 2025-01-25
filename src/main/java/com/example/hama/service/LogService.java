package com.example.hama.service;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.dto.LogDTO;
import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogAttachedFile;
import com.example.hama.repository.LogFileRepository;
import com.example.hama.repository.LogLikeRepository;
import com.example.hama.repository.LogRepository;
import com.example.hama.repository.LogReplyRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LogService {
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
    private final LogRepository logRepository;
    private final LogFileService logFileService;
    private final LogFileRepository logFileRepository;
    private final LogReplyRepository logReplyRepository;
    private final LogLikeRepository logLikeRepository;

 // 일기 등록 및 수정
    public void saveLog(Log log, List<MultipartFile> logFiles) throws IOException {
        log = logRepository.save(log);
        System.out.println("Log saved: " + log.getLogId());
        if (logFiles != null && !logFiles.isEmpty()) {
            List<LogAttachedFile> attachedFiles = logFileService.logSaveFiles(logFiles);
            for (LogAttachedFile file : attachedFiles) {
                file.setLog(log);
                logFileRepository.save(file); // 파일 저장
                
            }
        }
    }

    public void updateLog(Long logId, String content, List<MultipartFile> logFiles, List<String> deletedFiles) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        log.setLogContent(content);

        // 삭제 예정 파일 처리
        if (deletedFiles != null && !deletedFiles.isEmpty()) {
            for (String filename : deletedFiles) {
                deleteLogFile(logId, filename);
            }
        }

        // 새 파일 저장
        if (logFiles != null && !logFiles.isEmpty()) {
            List<LogAttachedFile> attachedFiles = logFileService.logSaveFiles(logFiles);
            for (LogAttachedFile file : attachedFiles) {
                file.setLog(log);
                logFileRepository.save(file);
            }
        }

        logRepository.save(log);
    }

    
    public void deleteLogFile(Long logId, String filename) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        LogAttachedFile fileToDelete = log.getLogAttachedFiles().stream()
                .filter(file -> file.getLog_saved_filename().equalsIgnoreCase(filename.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

        // 파일 삭제
        Path filePath = Paths.get(uploadPath).resolve(filename).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + filename, e);
        }

        // 데이터베이스에서 파일 삭제
        logFileRepository.delete(fileToDelete);

        // 로그 객체에서 파일 제거
        log.getLogAttachedFiles().remove(fileToDelete);
        logRepository.save(log);
    }
	    
	// ID로 로그 조회
	public Optional<Log> findById(Long id) {
	    return logRepository.findById(id);
	}
	
	//일기 전체 조회
	public List<Log> findLogs() {
		List<Log> log = logRepository.findAll();
		return log;
	}
	
	//일기 불러오기 
    public List<Log> getAllLogs() {
        return logRepository.findAll(); // JpaRepository를 통한 데이터 조회
    }


	//일기 한권 조회
	public Log findLog(Long id) {
		Optional<Log> log = logRepository.findById(id);
		return log.orElse(null);
	}

    public void deleteLog(Long logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));
        // 연관된 데이터 삭제
        logRepository.delete(log);
    }
    
    
	
    // 모든 로그를 조회하여 DTO로 변환
	public List<LogDTO> getLog() {
	    List<Log> log = logRepository.findAll();
	    return log.stream()
	               .map(LogDTO::new) // Log 엔티티를 LogDTO로 변환
	               .collect(Collectors.toList());
	}
	
	public LogAttachedFile findFileByLogId(Log log) {
		LogAttachedFile logFile = logFileRepository.findByLog(log);
		return logFile;
	}

	public LogAttachedFile findFileByAttachedFileId(Long id) {
		Optional<LogAttachedFile> logAttachedFile = logFileRepository.findById(id);
		return logAttachedFile.orElse(null);
	}
	
	public List<LogDTO> getLogTree() {
		// TODO Auto-generated method stub
		return null;
	}
}

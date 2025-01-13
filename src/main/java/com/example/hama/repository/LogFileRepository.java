package com.example.hama.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.log.Log;
import com.example.hama.model.log.LogAttachedFile;


public interface LogFileRepository extends JpaRepository<LogAttachedFile, Long>{
	LogAttachedFile findByLog(Log log);
}

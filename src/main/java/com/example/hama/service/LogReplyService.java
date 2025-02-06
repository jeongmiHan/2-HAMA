package com.example.hama.service;
import org.springframework.stereotype.Service;

import com.example.hama.repository.LogReplyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogReplyService {
	
    private final LogReplyRepository logReplyRepository;

    // 댓글 수 가져오기
    public Long getLogReplyCount(Long logId) {
        return logReplyRepository.countRepliesByLogId(logId);
    }
}
package com.example.hama.service;
import org.springframework.stereotype.Service;

import com.example.hama.repository.ReplyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReplyService {
	
    private final ReplyRepository replyRepository;

    // 댓글 수 가져오기
    public Long getReplyCount(Long logId) {
        return replyRepository.countRepliesByLogId(logId);
    }
}
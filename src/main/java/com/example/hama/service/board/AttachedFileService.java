package com.example.hama.service.board;


import org.springframework.stereotype.Service;

import com.example.hama.model.board.AttachedFile;
import com.example.hama.repository.board.FileRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AttachedFileService {
	
	private final FileRepository fileRepository;
	
	public void saveAttachedFile(AttachedFile attachedFile) {
		fileRepository.save(attachedFile);
	}
	


}

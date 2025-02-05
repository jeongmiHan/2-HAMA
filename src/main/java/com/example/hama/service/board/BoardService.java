package com.example.hama.service.board;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.model.board.AttachedFile;
import com.example.hama.model.board.Board;
import com.example.hama.repository.board.BoardRepository;
import com.example.hama.repository.board.FileRepository;
import com.example.hama.util.FileService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {
	@Value("${file.upload.path}")
	private String uploadPath;
	private final BoardRepository boardrepository;
	private final FileService fileService;
	private final FileRepository fileRepository; 
	
	//게시글 등록
	public void saveBoard(Board board, AttachedFile attachedFile) {
		//첨부파일 있으면
		if(attachedFile != null) {
			boardrepository.save(board);
			fileRepository.save(attachedFile);
		}
		else {
			boardrepository.save(board);
		}
		
	
	}

	public Page<Board> findAll(Pageable pageable) {
		Page<Board> list = boardrepository.findAll(pageable);
		return list;
	}

	public Board findBoard(Long id) {
		Optional<Board> board = boardrepository.findById(id);
		return board.orElse(null);
//		Board board = boardrepository.findById(id).get();
//		return board;
	}

	public void updateBoard(Board updateBoard, boolean isFileRemoved, MultipartFile file) {
		Board findBoard = findBoard(updateBoard.getBoardId());
		
		findBoard.setBdTitle(updateBoard.getBdTitle());
		findBoard.setBdContent(updateBoard.getBdContent());
		findBoard.setHit(updateBoard.getHit());
		
		//첨부파일 체크 
		AttachedFile attachedFile = findFileByBoardId(findBoard);
		if(attachedFile != null && (isFileRemoved || (file != null && file.getSize() > 0))) {
			//첨부파일 삭제(서버에서도 지우고 DB에서도 지우고)
			//파일 삭제를 요청했거나 새로운 파일이 업로드 되면 기존 파일을 삭제
			removeFile(attachedFile);
		}
		
		//새로 저장할 파일이 있으면 
		if(file != null && file.getSize() > 0) {
			//첨부파일을 서버(로컬)에 저장
			AttachedFile savedFile = fileService.saveFile(file);
			//데이터베이스에 저장될 보드 세팅
			savedFile.setBoard(findBoard);
			//첨부파일 내용을 데이터베이스에 저장
			saveBoard(findBoard, savedFile);
		}
		
		boardrepository.save(findBoard); //PK가 있는 상황 => Update 쿼리
	}
	
	//첨부파일 삭제 
	private void removeFile(AttachedFile attachedFile) {
		//DB에서 삭제
		fileRepository.deleteById(attachedFile.getAttachedFileId());
		//서버(로컬)에서 삭제
		String fullPath = uploadPath + "/" + attachedFile.getSaved_filename();
		fileService.deleteFile(fullPath);
	}

	public void removeBoard(Board board) {
		//첨부파일 체크
		AttachedFile attachedFile = findFileByBoardId(board);
		if(attachedFile != null) {
			removeFile(attachedFile);
		}
		boardrepository.deleteById(board.getBoardId());
	}

	public AttachedFile findFileByBoardId(Board board) {
		AttachedFile file = fileRepository.findByBoard(board);
		return file;
	}
	public AttachedFile findFileByAttachedFileId(Long id) {
	Optional<AttachedFile> attachedFile = fileRepository.findById(id);
		return attachedFile.orElse(null);
	}

	public Page<Board> findSearch(String searchText, String searchType, Pageable pageable) {
	    if ("title".equals(searchType)) {
	        return boardrepository.findByBdTitleContaining(searchText, pageable);
	    } else if ("content".equals(searchType)) {
	        return boardrepository.findByBdContentContaining(searchText, pageable);
	    } else { // both (제목 + 내용)
	        return boardrepository.findByBdTitleContainingOrBdContentContaining(searchText, searchText, pageable);
	    }
	}



	public Board findById(Long id) {
	    // id 값으로 게시글 조회
	    return boardrepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
	}
  
	

	
}  

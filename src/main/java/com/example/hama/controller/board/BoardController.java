package com.example.hama.controller.board;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.board.AttachedFile;
import com.example.hama.model.board.Board;
import com.example.hama.model.board.BoardUpdate;
import com.example.hama.model.board.BoardWrite;
import com.example.hama.model.user.User;
import com.example.hama.service.UserService;
import com.example.hama.service.board.BoardService;
import com.example.hama.service.board.ReplyService;
import com.example.hama.util.FileService;
import com.example.hama.util.PageNavigator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("board")
@RequiredArgsConstructor
public class BoardController {
	
	 
	private final UserService userService;
	
	@Value("${file.upload.path}")
	private String uploadPath;

	
	//페이징 처리를 위한 상수값
	private final int countPerPage = 10;
	private final int pagePerGroup = 5;
	
	private final BoardService boardService;
	private final FileService fileService;
	private final ReplyService replyService;
	
	
//	//메인 페이지 이동
//	@GetMapping("/") //최상위경로로 요청이 들어오면
//	public String home() {
//		log.info("home() 실행");
//		return "/index";
//	}
//	

	//게시글 작성 페이지 이동
	@GetMapping("write")
	public String writeForm(Model model) {
		log.info("글쓰기폼 페이지");

		User user = getAuthenticatedUser();
	      if(user == null) {
	         return "redirect:/user/login";
	      }
	    
	    model.addAttribute("nickname", user.getName());
		model.addAttribute("boardWrite",new BoardWrite());
		return "board/boardWrite";
	}
	
	
	//게시글 등록
	@PostMapping("write")
	public String write(@Validated @ModelAttribute BoardWrite boardWrite,
	                    BindingResult result,
	                    @RequestParam(name = "file", required = false) MultipartFile file) {
		
		
	      
	    if (result.hasErrors()) {
	        return "board/boardWrite";
	    }

	    // Spring Security에서 현재 인증된 사용자 정보 가져오기
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication == null || !authentication.isAuthenticated() || 
	        "anonymousUser".equals(authentication.getPrincipal())) {
	        throw new IllegalStateException("로그인된 사용자 정보가 없습니다.");
	    }

	    User loginUser = null;
	    Object principal = authentication.getPrincipal();

	    // 기본 로그인 사용자 처리
	    if (principal instanceof CustomUserDetails) {
	        loginUser = ((CustomUserDetails) principal).getUser();
	    } 
	    // 소셜 로그인 사용자 처리
	    else if (principal instanceof DefaultOAuth2User) {
	        String providerUserId = (String) ((DefaultOAuth2User) principal).getAttributes().get("sub");
	        loginUser = userService.findUserByProviderUserId(providerUserId);
	    }

	    // 로그인된 사용자 정보를 설정하지 못한 경우 예외 처리
	    if (loginUser == null) {
	        throw new IllegalStateException("로그인된 사용자 정보를 가져올 수 없습니다.");
	    }

	    // 게시글 생성 및 사용자 정보 설정
	    Board board = BoardWrite.toBoard(boardWrite);
	    board.setUser(loginUser);

	    // 첨부파일 처리
	    if (file != null && !file.isEmpty()) {
	        // 첨부파일 서버에 저장
	        AttachedFile attachedFile = fileService.saveFile(file);
	        attachedFile.setBoard(board);

	        // 게시글과 첨부파일 저장
	        boardService.saveBoard(board, attachedFile);

	        return "redirect:/board/list";
	    }

	    // 첨부파일이 없는 경우 게시글만 저장
	    boardService.saveBoard(board, null);

	    return "redirect:/board/list";
	}

	
	
		@GetMapping("list")
		public String listForm(
				@RequestParam(name = "page", defaultValue = "1") int page,
		        @RequestParam(name = "searchText", defaultValue = "") String searchText,
	            @RequestParam(value = "searchType", defaultValue = "both") String searchType,
		        @RequestParam(name = "sort", defaultValue = "bdCreatedDate") String sort,
		        @RequestParam(name = "direction", defaultValue = "DESC") String direction,
		        Model model) {
			
			User user = getAuthenticatedUser();
		      if(user == null) {
		         return "redirect:/user/login";
		      }
		      
		    // 정렬 설정
		    Pageable pageable = PageRequest.of(page - 1, countPerPage, org.springframework.data.domain.Sort.by(
		            Direction.fromString(direction), sort));

		    // 검색어가 있는 경우
		    if (!searchText.isEmpty()) {
		    	Page<Board> searchList = boardService.findSearch(searchText, searchType, pageable);

		        // 검색 결과가 없는 경우 처리
		        if (searchList.isEmpty()) {
		            model.addAttribute("emptyMsg", "검색 결과가 없습니다.");
		            model.addAttribute("replyCounts", new HashMap<>()); // 빈 Map 추가
		            return "/board/boardList";
		        }

		        // 댓글 개수 조회
		        Map<Long, Integer> replyCounts = replyService.getReplyCountsByBoardIds(searchList.getContent());
		        model.addAttribute("replyCounts", replyCounts);

		        // 페이지 네비게이터 설정
		        PageNavigator navi = new PageNavigator(countPerPage, pagePerGroup, page,
		                                               (int) searchList.getTotalElements(),
		                                               searchList.getTotalPages());
		        
				model.addAttribute("content", "board/boardList :: content");
		        model.addAttribute("list", searchList);
		        model.addAttribute("navi", navi);
		        model.addAttribute("searchText", searchText);
		        model.addAttribute("sort", sort);
		        model.addAttribute("direction", direction);
		        return "/board/boardList";
		    }

		    // 검색어가 없는 경우
		    Page<Board> list = boardService.findAll(pageable);

		    // 댓글 수 조회 추가
		    Map<Long, Integer> replyCounts = replyService.getReplyCountsByBoardIds(list.getContent());
		    model.addAttribute("replyCounts", replyCounts);

		    // 페이지 네비게이터 설정
		    PageNavigator navi = new PageNavigator(countPerPage, pagePerGroup, page,
		                                           (int) list.getTotalElements(),
		                                           list.getTotalPages());
		    
		    model.addAttribute("nickname", user.getName());
		    model.addAttribute("list", list);
		    model.addAttribute("navi", navi);
		    model.addAttribute("sort", sort);
		    model.addAttribute("direction", direction);

		    return "board/boardList";
		}


	@GetMapping("read")
	public String read(@RequestParam("id") Long id
					, Model model) {
		log.info("보드아이디:{}",id);
		
		User user = getAuthenticatedUser();
	      if(user == null) {
	         return "redirect:/user/login";
	      }

		// 현재 로그인 사용자 ID를 모델에 추가
		model.addAttribute("currentUserId", user.getUserId());
		
		//요청할 때 날아온 쿼리 파라미터로 repository에 있는 보드 객체 하나 가져오기 
		Board board = boardService.findBoard(id);
		
		// 게시글 존재 여부 검증
	    if (board == null) {
	        throw new IllegalArgumentException("게시글이 존재하지 않습니다. ID=" + id);
	    }
	    
	    // 작성자와 로그인 유저가 같은지 확인
	    boolean isWriter = board.getUser().getUserId().equals(user.getUserId());
	    model.addAttribute("isWriter", isWriter); // 작성자 여부를 모델에 담기
	    
	    
		//조회수 1증가
		board.addHit();
		//DB업데이트
		boardService.updateBoard(board, false, null);
		//모델에 담기
		model.addAttribute("board",board);
		
		//모델에 담기
		 model.addAttribute("nickname", user.getName());
				model.addAttribute("board",board);
				
				//첨부파일이 있는지 찾기
				AttachedFile attachedFile = boardService.findFileByBoardId(board);
//				log.info("attachedFile:{}",attachedFile);
				
				//첨부파일이 있다면 
				if(attachedFile != null) {
					model.addAttribute("file", attachedFile);
				}
				
		
		return "board/boardRead";
	}
	
	@GetMapping("update")
	public String updateForm(@RequestParam(name = "id", required=false)Long id
										, Model model) {
		
		User user = getAuthenticatedUser();
	      if(user == null) {
	         return "redirect:/user/login";
	      }
		
		
		//boardId에 해당하는 board 찾기
		Board board = boardService.findBoard(id);
		if(board == null) {
			return "reirect:/board/list";
		}
		BoardUpdate boardUpdate = Board.toBoardUpdate(board);
		
		model.addAttribute("nickname", user.getName());
		model.addAttribute("boardUpdate",boardUpdate);
		
		//첨부파일 체크
				AttachedFile attachedFile = boardService.findFileByBoardId(board);
				
				if(attachedFile != null) {
					model.addAttribute("file", attachedFile);
				}
		
		return "board/boardUpdate";
	}
	
	@PostMapping("update")
	public String update(@Validated @ModelAttribute BoardUpdate boardUpdate,
	                     BindingResult result,
	                     @RequestParam(name="file", required = false) MultipartFile file) {
	    
	    // 유효성 검사
	    if (result.hasErrors()) {
	        return "boardUpdate"; // 유효성 검사 실패 시 수정 페이지로 다시 이동
	    }

	    // 게시글 수정 처리
	    Board updateBoard = BoardUpdate.toBoard(boardUpdate);
	    boardService.updateBoard(updateBoard, boardUpdate.isFileRemoved(), file);

	    // 수정 완료 후 게시글 읽기 페이지로 리다이렉트
	    return "redirect:read/" + boardUpdate.getBoardId();
	}

	@GetMapping("read/{id}")
	public String readUpdatedBoard(@PathVariable("id") Long id, Model model) {
	    Board board = boardService.findById(id); // 게시글 조회
	    model.addAttribute("board", board);
	    return "board/boardRead"; // boardRead.html로 이동
	}

	
	
	//게시글 삭제
		@GetMapping("delete")
		public String remove(@RequestParam(name = "id", required = false) Long id) {
			
			log.info("삭제할 글ID:{}",id);
			
			
			Board findBoard = boardService.findBoard(id);
			//글이 존재하는지
			if(findBoard == null) {
				return "redirect:/board/list";
			}
			
			boardService.removeBoard(findBoard);
			
			return "redirect:/board/list";
		
		}
		
	
		@PostMapping("/uploadImage")
		public ResponseEntity<String> uploadImage(@RequestParam("imageFile") MultipartFile file) {
		    try {
		        // 파일 저장
		        String imageUrl = saveFile(file);
		        return ResponseEntity.ok(imageUrl); // 업로드된 이미지 URL 반환
		    } catch (IOException e) {
		        e.printStackTrace();
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 실패");
		    }
		}

		private String saveFile(MultipartFile file) throws IOException {
		    Path uploadDir = Paths.get(uploadPath);
		    if (!Files.exists(uploadDir)) {
		        Files.createDirectories(uploadDir); // 디렉토리가 없으면 생성
		    }

		    String originalFilename = file.getOriginalFilename();
		    String uniqueFilename = UUID.randomUUID() + "_" + originalFilename; // 고유한 이름 생성
		    Path path = uploadDir.resolve(uniqueFilename);
		    Files.write(path, file.getBytes());

		    return "/uploadPath/" + uniqueFilename; // 업로드된 이미지 URL 반환
		}


		
		@GetMapping("/read/")
		public String readBoard(@RequestParam(value = "id", required = false) Long id, Model model) {
		    if (id == null) {
		        return "redirect:/board/list"; // id 없을 시 목록으로 리다이렉트
		    }
		    Board board = boardService.findById(id);
		    model.addAttribute("board", board);
		    return "board/boardRead";
		}
		
		@GetMapping("download/{id}")
		public ResponseEntity<Resource> download(@PathVariable("id")Long id) throws MalformedURLException{
			//첨부파일 아이디로 첨부파일 정보를 가져온다. 
			AttachedFile attachedFile = boardService.findFileByAttachedFileId(id);
			//다운로드 하려는 파일의 절대경로 값을 만든다. 
			String fullPath = uploadPath + "/" + attachedFile.getSaved_filename();
			
			UrlResource resource = new UrlResource("file:" + fullPath);
			
			//한글 파일명이 깨지지 않도록 UTF-8fh 파일명을 인코딩
			String encodingFileName = UriUtils.encode(attachedFile.getOriginal_filename()
											, StandardCharsets.UTF_8);
			
			//응답 헤더에 담을 Content Disposition 값을 생성한다.
			String contentDisposition = "attachment; filename=\"" + encodingFileName + "\"";
			
			
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
					.body(resource);
		}
		

		
		
		private User getAuthenticatedUser() {
		       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		       if (authentication != null && authentication.isAuthenticated()) {
		           Object principal = authentication.getPrincipal();
		           if (principal instanceof CustomUserDetails userDetails) {
		               return userDetails.getUser();
		           } else if (principal instanceof DefaultOAuth2User oAuth2User) {
		               String providerUserId = (String) oAuth2User.getAttributes().get("sub");
		               return userService.findUserByProviderUserId(providerUserId);
		           }
		       }
		       return null;
		   }
	}

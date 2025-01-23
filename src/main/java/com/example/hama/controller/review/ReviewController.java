package com.example.hama.controller.review;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.location.Location;
import com.example.hama.model.review.Review;
import com.example.hama.model.review.ReviewUpdate;
import com.example.hama.model.review.ReviewWrite;
import com.example.hama.model.user.User;
import com.example.hama.service.LocationService;
import com.example.hama.service.ReviewService;
import com.example.hama.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("review")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;
	private final LocationService locationService;
	private final UserService userService;
	
	// 리뷰 작성 페이지 이동
	@GetMapping("reviewwrite")
	public String writeForm(@RequestParam("locationId") Long locationId
							,Model model) {

		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		Location location = locationService.findLocation(locationId);
		
		model.addAttribute("locationName", location.getLocationName());
		model.addAttribute("locationId", location.getLocationId());
		model.addAttribute("reviewWrite", new ReviewWrite());
		return "review/reviewwrite";
		}
	
	// 리뷰 작성 
	@PostMapping("reviewwrite")
	public String write(@Validated @ModelAttribute ReviewWrite reviewWrite
						, @RequestParam(name="reviewImagePaths", required = false) List<MultipartFile> reviewImagePaths
						, BindingResult result
						, Model model
						, @RequestParam(name = "locationId") Long locationId
						) throws IOException {
		
		if(result.hasErrors()) {
			model.addAttribute("locationId",locationId);
			return "review/reviewwrite";
		}
		//review 객체로 변환
		Review review = ReviewWrite.toReview(reviewWrite);
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		review.setUser(user);
		
		Location location = locationService.findLocation(locationId);
		review.setLocation(location);
		
		// 파일 저장 처리
		List<String> filePaths = new ArrayList<>();
		String uploadDir = System.getProperty("user.dir") + "/uploads/";

		// 업로드 폴더 생성
		Files.createDirectories(Paths.get(uploadDir));

		if (reviewImagePaths != null) {
		    for (MultipartFile file : reviewImagePaths) {
		        if (!file.isEmpty()) {
		            // 고유 파일명 생성
		            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		            Path filePath = Paths.get(uploadDir + fileName);
		            Files.write(filePath, file.getBytes());

		            // 파일 경로 저장
		            filePaths.add(fileName); // 파일명만 저장
		        }
		    }
		}

		// 콤마로 구분된 파일 경로 저장
		review.setReviewImagePath(String.join(",", filePaths));
		//DB등록
		reviewService.saveReview(review);

		// 성공 알림 및 팝업 종료
	    model.addAttribute("successScript", "<script>alert('리뷰가 성공적으로 등록되었습니다.'); window.opener.location.reload(); window.close();</script>");
	    return "review/reviewwrite";
//		return "redirect:/location/locationRead?id=" + locationId;
		}
	
	// 리뷰 수정하기 페이지 이동
	@GetMapping("reviewUpdate")
	public String updateForm(@RequestParam("reviewId") Long reviewId
							, Model model) {

		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
	
	//reviewId에 해당하는 review 찾기
	Review review = reviewService.findReview(reviewId);

	ReviewUpdate reviewUpdate = Review.toReviewUpdate(review);
	model.addAttribute("locationName", review.getLocation().getLocationName());
    model.addAttribute("locationId", review.getLocation().getLocationId());
	model.addAttribute("reviewUpdate", reviewUpdate);
	
	return "review/reviewUpdate";
	}
	
	//리뷰 수정하기
	@PostMapping("reviewUpdate")
	public String UpdateReview(@Validated @ModelAttribute ReviewUpdate reviewUpdate
								, BindingResult result
								, Model model
								, @RequestParam(name = "reviewId") Long reviewId
		                        , @RequestParam(name = "locationId") Long locationId
		                        , @RequestParam(name = "reviewImagePaths", required = false) List<MultipartFile> reviewImagePaths) throws IOException {
		if(result.hasErrors()) {
			return "review/reviewUpdate";
		}
		
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		//수정작업을 위한 엔티티 변환
		Review updateReview = ReviewUpdate.toReview(reviewUpdate);
		
		// 첨부파일 처리
	    String uploadDir = System.getProperty("user.dir") + "/uploads/";
	    Files.createDirectories(Paths.get(uploadDir)); // 업로드 폴더 생성

	    List<String> filePaths = new ArrayList<>();
	    if (reviewImagePaths != null && !reviewImagePaths.isEmpty()) {
	        for (MultipartFile file : reviewImagePaths) {
	            if (!file.isEmpty()) {
	                // 고유 파일명 생성 및 저장
	                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
	                Path filePath = Paths.get(uploadDir + fileName);
	                Files.write(filePath, file.getBytes());

	                // 파일 경로 추가
	                filePaths.add(fileName);
	            }
	        }

	        // 새로운 파일 경로 업데이트
	        updateReview.setReviewImagePath(String.join(",", filePaths));
	    }
		
		
		//서비스에 있는 업데이트보드로 던지기
		reviewService.updateReview(updateReview);
		
		model.addAttribute("successScript", "<script>alert('리뷰가 성공적으로 수정되었습니다.'); window.opener.location.reload(); window.close();</script>");
		return "review/reviewUpdate"; // 수정 후 팝업 닫기
//		return "redirect:/location/locationRead?id=" + locationId;
		}
	
	// 리뷰 삭제하기
	@GetMapping("reviewDelete")
	public String RemoveReview(@RequestParam(name = "reviewId") Long reviewId
								, @RequestParam(name="locationId") Long locationId
								) {
		
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		Review findReivew = reviewService.findReview(reviewId);
		
		reviewService.removeReview(reviewId);
		
		return "redirect:/location/locationRead?id=" + locationId;
		}
	
	// 인증된 사용자 가져오기 메소드
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
		
	private void addAuthenticatedUserToModel(Model model) {
	    User user = getAuthenticatedUser();
	    if (user != null) {
	        model.addAttribute("sessionUser", user);
	    }
	}
}

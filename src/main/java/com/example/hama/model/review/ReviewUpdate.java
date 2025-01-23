package com.example.hama.model.review;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.example.hama.model.location.Location;
import com.example.hama.model.user.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewUpdate {
	private Long reviewId;
	
	private Location location;
	
	private User user;
	
	@NotBlank(message = "리뷰 내용을 입력해주세요")
	private String reviewContent;
	
	@NotNull(message = "평점을 입력해주세요")
	private Long reviewRating;
	
	@NotNull(message = "방문 날짜를 입력해주세요")
	private LocalDate reviewDate;
	
	private String reviewImagePath;		// 업로드 이미지
	
	public static Review toReview(ReviewUpdate reviewUpdate) {
		Review review = new Review();
		
		review.setReviewId(reviewUpdate.getReviewId());
		review.setLocation(reviewUpdate.getLocation());
		review.setUser(reviewUpdate.getUser());
		review.setReviewContent(reviewUpdate.getReviewContent());
		review.setReviewRating(reviewUpdate.getReviewRating());
		review.setReviewDate(reviewUpdate.getReviewDate());
		review.setReviewImagePath(reviewUpdate.getReviewImagePath());
		return review;
	}
}

package com.example.hama.model.review;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.example.hama.model.location.Location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewWrite {
	private Location location;
	
	@NotBlank(message = "리뷰 내용을 입력해주세요")
	private String reviewContent;
	
	@NotNull(message = "평점을 입력해주세요")
	private Long reviewRating;	
	
	@NotNull(message = "방문 날짜를 입력해주세요")
	private LocalDate reviewDate;	
	
	private String reviewImagePath;		// 업로드 이미지	
	
	public static Review toReview(ReviewWrite reviewWrite) {
		Review review = new Review();
		review.setLocation(reviewWrite.getLocation());
		review.setReviewContent(reviewWrite.getReviewContent());
		review.setReviewRating(reviewWrite.getReviewRating());
		review.setReviewDate(reviewWrite.getReviewDate());
		review.setReviewImagePath(reviewWrite.getReviewImagePath());
		return review;
	}
}

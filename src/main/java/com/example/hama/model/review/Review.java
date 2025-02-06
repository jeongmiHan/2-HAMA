package com.example.hama.model.review;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.example.hama.model.location.Location;
import com.example.hama.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
@Entity
public class Review {
	
	@Id
	@Column(name="review_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reviewId;				//리뷰ID
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="location_id", nullable = false)
	private Location location;			//장소ID
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(name="review_content")
	@NotBlank(message = "리뷰 내용을 입력해주세요")
	private String reviewContent; 		//리뷰내용
	
	@Column(name="review_rating", nullable = false)
	@NotNull(message = "평점을 입력해주세요")
	private Long reviewRating;			//리뷰평점
	
	@PastOrPresent
	@Column(name="review_date", nullable = false)
	@NotNull(message = "방문 날짜를 입력해주세요")
	private LocalDate reviewDate;		//방문날짜
	
	@Column(name="review_image_path",nullable = true)  // 이미지 경로 저장
	private String reviewImagePath;		// 리뷰 이미지 경로 (파일이 아닌 경로만 저장)
	
	public static ReviewUpdate toReviewUpdate(Review review) {
		ReviewUpdate reviewUpdate = new ReviewUpdate();
		
		reviewUpdate.setReviewId(review.getReviewId());
		reviewUpdate.setLocation(review.getLocation());
		reviewUpdate.setUser(review.getUser());
		reviewUpdate.setReviewContent(review.getReviewContent());
		reviewUpdate.setReviewRating(review.getReviewRating());
		reviewUpdate.setReviewDate(review.getReviewDate());
		reviewUpdate.setReviewImagePath(review.getReviewImagePath());
		
		return reviewUpdate;
	}
}

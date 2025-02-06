package com.example.hama.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.hama.model.review.Review;
import com.example.hama.repository.ReviewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final LocationService locationService;
	
	// 리뷰 등록
	public void saveReview(Review review) {
		reviewRepository.save(review);
	}
	
	// 장소추천글 아이디로 해당 리뷰 찾기 (장소추천글에 해당 리뷰들 리스트 불러와야되니까)
	public List<Review> findByLocationLocationId(Long locationId){
		return reviewRepository.findByLocationLocationId(locationId);
	}
	
	// 단일 리뷰 가져오기 (수정, 삭제용)
	public Review findReview(Long reviewId) {
		Optional<Review> review = reviewRepository.findById(reviewId);
		return review.orElse(null);
	}
	
	// 장소추천글 아이디에 대한 리뷰 개수 반환 메서드
	public long countByLocationId(Long locationId) {
		return reviewRepository.countByLocationLocationId(locationId);
	}
	
	// 리뷰 평점 평균 계산
	public Float calculateAverageRating(Long locationId) {
		// 리뷰 목록 가져오고
		List<Review> listR = reviewRepository.findByLocationLocationId(locationId);
		//리뷰가 없으면 평균평점 0.0
		if(listR.isEmpty()) {
			return 0.0f;
		}
		//리뷰 평점 합산 후 평균 계산
		double average = listR.stream()
				.mapToLong(Review::getReviewRating)
				.average()
				.orElse(0.0);
		
		return (float) average;
	}
	
	//리뷰 수정
	public void updateReview(Review updateReview) {
		Review findReview = findReview(updateReview.getReviewId());
		
		findReview.setReviewContent(updateReview.getReviewContent());
		findReview.setReviewRating(updateReview.getReviewRating());
		findReview.setReviewDate(updateReview.getReviewDate());
		findReview.setReviewImagePath(updateReview.getReviewImagePath());
		 
		 reviewRepository.save(findReview);
				
	}
	
	//리뷰 삭제
	public void removeReview(Long reviewId) {
		reviewRepository.deleteById(reviewId);
	}
	
	
}

package com.example.hama.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.review.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>{
	// location의 id를 기준으로 조회
	List<Review> findByLocationLocationId(Long locationId);
	long countByLocationLocationId(Long locationId);
}

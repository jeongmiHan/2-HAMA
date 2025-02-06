package com.example.hama.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;


import com.example.hama.model.location.CategoryType;
import com.example.hama.model.location.Location;


public interface LocationRepository extends JpaRepository<Location, Long> {
	// 리뷰 많은 순
    List<Location> findAllByOrderByReviewCountDesc();

    // 평점 높은 순
    List<Location> findAllByOrderByLocationRatingDesc();
    
    //특정 회원이 작성한 글 조회
    List<Location> findByUserUserId(String userId);
    
    // 카테고리별 조회
    List<Location> findByLocationCategory(CategoryType category);
    
    //동일한 location게시글 막기
    boolean existsByLocationNameAndLocationAddress(String locationName, String locationAddress);
}

package com.example.hama.model.location;

import com.example.hama.model.user.User;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdate {
	
	private Long locationId; 			//장소ID
	
	@NotBlank(message = "장소명을 입력해주세요")
	private String locationName;		// 장소명이자 제목
	
	@NotBlank(message = "주소를 입력해주세요")
	private String locationAddress; 	// 장소주소
	
	private  User user;
	
	@NotNull(message = "카테고리를 입력해주세요")
	private CategoryType locationCategory; 	// 장소카테고리
	
	private Float locationRating;		// 사용자 평점
	
	private Long reviewCount; 			// 리뷰수
	
	private Double locationLatitude;		// 장소 위도
	
	private Double locationLongitude;		// 장소 경도

	private Double locationDistance = 0.0; // 사용자와의 거리(km)
	
	public static Location toLocation(LocationUpdate locationUpdate) {
		Location location = new Location();
		
		location.setLocationId(locationUpdate.getLocationId());
		location.setLocationName(locationUpdate.getLocationName());
		location.setLocationAddress(locationUpdate.getLocationAddress());
		location.setLocationCategory(locationUpdate.getLocationCategory());
		location.setLocationRating(locationUpdate.getLocationRating());
		location.setReviewCount(locationUpdate.getReviewCount());
		location.setUser(locationUpdate.getUser());
		location.setLocationLatitude(locationUpdate.getLocationLatitude());
		location.setLocationLongitude(locationUpdate.getLocationLongitude());
		location.setLocationDistance(locationUpdate.getLocationDistance());
		
		return location;
	}
}

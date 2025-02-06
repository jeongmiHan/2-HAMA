package com.example.hama.model.location;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.example.hama.model.location.LocationWrite;

@Data
public class LocationWrite {
	
	@NotBlank(message = "장소명을 입력해주세요")
	private String locationName;		// 장소명이자 제목
	
	@NotBlank(message = "주소를 입력해주세요")
	private String locationAddress; 	// 장소주소
	
	@NotNull(message = "카테고리를 입력해주세요")
	private CategoryType locationCategory; 	// 장소카테고리
	
	private Float locationRating;		// 사용자 평점
	
	private Double locationLatitude;		// 장소 위도
	
	private Double locationLongitude;		// 장소 경도

	private Double locationDistance = 0.0; // 사용자와의 거리(km)
	
	public static Location toLocation(LocationWrite locationWrite) {
		Location location = new Location();
		location.setLocationName(locationWrite.getLocationName());
		location.setLocationAddress(locationWrite.getLocationAddress());
		location.setLocationCategory(locationWrite.getLocationCategory());
		location.setLocationRating(locationWrite.getLocationRating());
		location.setLocationLatitude(locationWrite.getLocationLatitude());
		location.setLocationLongitude(locationWrite.getLocationLongitude());
		location.setLocationDistance(locationWrite.getLocationDistance());
		
		return location;
	}
}

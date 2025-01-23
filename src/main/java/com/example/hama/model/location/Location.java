package com.example.hama.model.location;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.example.hama.model.review.Review;
import com.example.hama.model.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class Location {
	
	@Id
	@Column(name="location_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long locationId; // 장소 id
	
	@Column(name="location_name", nullable = false)
	@NotBlank(message = "장소명을 입력해주세요")
	private String locationName;		// 장소명
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="user_id")
	private User user;	// 작성자(유저)
	
	@Column(name="location_address", nullable = false)
	@NotBlank(message = "주소를 입력해주세요")
	private String locationAddress; 	// 장소주소
	
	@Enumerated(EnumType.STRING)
	@Column(name="location_category", nullable = false)
	@NotNull(message = "카테고리를 선택해주세요")
	private CategoryType locationCategory; 	// 장소카테고리
	
	@Column(name="location_rating", nullable=false)
	private Float locationRating = 0.0f;		// 사용자 평점
	
	@Column(name="review_count", nullable = false)
	private Long reviewCount = 0L; 			// 리뷰수
	
	@Column(name="location_latitude", nullable = false)
	@NotNull(message = "위도를 입력해주세요")
	private Double locationLatitude;		// 장소 위도
	
	@Column(name="location_longitude", nullable = false)
	@NotNull(message = "경도를 입력해주세요")
	private Double locationLongitude;		// 장소 경도
	
	@Column(name = "location_distance", nullable = false)
	private Double locationDistance = 0.0; // 사용자와의 거리(km)
	
	//location 삭제 시 연관된 review삭제
	@OneToMany(mappedBy = "location", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Review> reviews = new ArrayList<>();
	
	//사용자평점 소수점 첫 번째 자리까지만 
	@PrePersist
	@PreUpdate
	public void roundLocationRating() {
		if(locationRating != null) {
			locationRating = BigDecimal.valueOf(locationRating)
										.setScale(1, RoundingMode.HALF_UP)
										.floatValue();
		}
	}
	
	public static LocationUpdate toLocationUpdate(Location location) {
		LocationUpdate locationUpdate = new LocationUpdate();
		
		locationUpdate.setLocationId(location.getLocationId());
		locationUpdate.setLocationName(location.getLocationName());
		locationUpdate.setUser(location.getUser());
		locationUpdate.setLocationAddress(location.getLocationAddress());
		locationUpdate.setLocationCategory(location.getLocationCategory());
		locationUpdate.setLocationRating(location.getLocationRating());
		locationUpdate.setReviewCount(location.getReviewCount());
		locationUpdate.setLocationLatitude(location.getLocationLatitude());
		locationUpdate.setLocationLongitude(location.getLocationLongitude());
		locationUpdate.setLocationDistance(location.getLocationDistance());
		
		return locationUpdate;
	}
}

package com.example.hama.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.hama.model.location.CategoryType;
import com.example.hama.model.location.Location;
import com.example.hama.repository.LocationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationService {
	private final LocationRepository locationRepository;
	
	//장소(게시글) 저장
	public void saveLocation(Location location) {
		if(location.getLocationRating() == null) {
			location.setLocationRating(0.0f);
		}
		if(location.getLocationDistance() == null) {
			location.setLocationDistance(0.0);
		}
		locationRepository.save(location);
	} 
	
	// 장소 가져오기 게시글 출력
	public List<Location> findAll(){
		List<Location> list = locationRepository.findAll();
		return list;
	}
	
	//읽을 때 가져오기
	public Location findLocation(Long locationId) {
		return locationRepository.findById(locationId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid location ID: " + locationId));
	}
	
	
	//리뷰 수 업데이트
	public void updateReviewCount(Long locationId, long reviewCount) {
		Location location = findLocation(locationId);
		location.setReviewCount(reviewCount);
		locationRepository.save(location);
	}
	
	//리뷰 평균 업데이트
	public void updateLocationRating(Long locationId, float locationRating) {
		Location location = findLocation(locationId);
		location.setLocationRating(locationRating);
		locationRepository.save(location);
	}
	
	// location 게시글 수정
	public void updateLocation(Location updateLocation) {
		Location findLocation = findLocation(updateLocation.getLocationId());
		
		findLocation.setLocationName(updateLocation.getLocationName());
		findLocation.setLocationAddress(updateLocation.getLocationAddress());
		findLocation.setLocationCategory(updateLocation.getLocationCategory());
		
		locationRepository.save(findLocation);
	}
	
	// location 게시글 삭제
	public void removeLocation(Long LocationId) {
		
		locationRepository.deleteById(LocationId);
	}
	
	// 리뷰 많은 순
	public List<Location> findAllByOrderByReviewCountDesc() {
	    return locationRepository.findAllByOrderByReviewCountDesc();
	}

	// 평점 높은 순
	public List<Location> findAllByOrderByLocationRatingDesc() {
	    return locationRepository.findAllByOrderByLocationRatingDesc();
	}
	
	//카테고리별 조회
	public List<Location> findByCategory(CategoryType category){
		return locationRepository.findByLocationCategory(category);
	}
	
	//내가 쓴 글 조회
	public List<Location> findByUser(String userId){
		return locationRepository.findByUserUserId(userId);
	}
	
	// 지역 주소로 조회
	public List<Location> findByRegion(String region){
		if("ALL".equals(region)) {
			return locationRepository.findAll();
		}
		Map<String, String> regionMap = Map.ofEntries(
				Map.entry("SEOUL", "서울"),
		        Map.entry("BUSAN", "부산"),
		        Map.entry("DAEGU", "대구"),
		        Map.entry("INCHEON", "인천"),
		        Map.entry("GWANGJU", "광주"),
		        Map.entry("DAEJEON", "대전"),
		        Map.entry("ULSAN", "울산"),
		        Map.entry("SEJONG", "세종"),
		        Map.entry("GYEONGGI", "경기"),
		        Map.entry("GANGWON", "강원"),
		        Map.entry("CHUNGBUK", "충북"),
		        Map.entry("CHUNGNAM", "충남"),
		        Map.entry("JEONBUK", "전북"),
		        Map.entry("JEONNAM", "전남"),
		        Map.entry("GYEONGBUK", "경북"),
		        Map.entry("GYEONGNAM", "경남"),
		        Map.entry("JEJU", "제주")
				);
		
		String koreanRegion = regionMap.getOrDefault(region, region);
		
		return locationRepository.findAll().stream().filter(location -> location.getLocationAddress().contains(koreanRegion)).toList();
	}
	
	//거리 계산
	public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371; // 지구 반지름 (km)
		
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + 
					Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
					Math.sin(dLon/2) * Math.sin(dLon/2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		return R * c; //거리 (km)
	}
	
	//유저 거리 업데이트
	@Transactional
	public void updateDistances(Double userLat, Double userLng) {
	    List<Location> locations = findAll();

	    locations.forEach(location -> {
	        double distance = calculateDistance(
	            userLat, userLng, location.getLocationLatitude(), location.getLocationLongitude()
	        );
	        location.setLocationDistance(distance);
	        locationRepository.save(location);
	    });
	}
	
	// 동일한 장소가 존재하는지 확인
    public boolean isLocationExists(String locationName, String locationAddress) {
        return locationRepository.existsByLocationNameAndLocationAddress(locationName, locationAddress);
    }

}

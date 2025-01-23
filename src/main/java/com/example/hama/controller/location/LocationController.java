package com.example.hama.controller.location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.location.Location;
import com.example.hama.model.location.LocationUpdate;
import com.example.hama.model.location.LocationWrite;
import com.example.hama.model.review.Review;
import com.example.hama.model.user.User;
import com.example.hama.model.user.UserLogin;
import com.example.hama.repository.UserRepository;
import com.example.hama.service.LocationService;
import com.example.hama.service.ReviewService;
import com.example.hama.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("location")
@RequiredArgsConstructor
public class LocationController {
	private final LocationService locationService;
	private final ReviewService reviewService;
	private final UserService userService;
	
	// 장소 추천 페이지 이동
	@GetMapping("/locationList")
	public String listLocation(Model model
								, @RequestParam(name="region", required = false, defaultValue = "ALL") String region 
								, @RequestParam(name = "filter", required = false, defaultValue = "default") String filter
								, @RequestParam(name="category", required = false, defaultValue = "ALL") String category
								, @RequestParam(name="search", required = false, defaultValue = "") String search
								, @RequestParam(name= "userLat", required = false) Double userLat
								, @RequestParam(name = "userLng", required = false) Double userLng
								, @RequestParam(name = "name", required = false) String nickname
								){
		
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		addAuthenticatedUserToModel(model);
		
		// 특정 지역별 조회
		List<Location> list = new ArrayList<>(locationService.findByRegion(region)); // 가변 리스트로 변환
		
		if(!search.isBlank()) {
			list = list.stream().filter(location -> location.getLocationName().toLowerCase().contains(search.toLowerCase())).collect(Collectors.toList());
		}
	
		 // 거리 계산 및 정렬 (DB에 저장된 거리 값 사용)
	    if (userLat != null && userLng != null) {
	        list.forEach(location -> {
	            if (location.getLocationDistance() == 0.0) { // 거리 정보가 없는 경우에만 계산
	                double distance = locationService.calculateDistance(
	                        userLat, userLng, location.getLocationLatitude(), location.getLocationLongitude()
	                );
	                location.setLocationDistance(distance);
	                locationService.saveLocation(location); // 업데이트된 거리 값 저장
	            }
	        });

	        // 거리순 정렬
	        if ("distance".equals(filter)) {
	            list.sort(Comparator.comparing(Location::getLocationDistance));
	        }
	    }
		
		// 카테고리별 조회
		if (!category.equals("ALL")) {
	        list = list.stream()
	                .filter(location -> location.getLocationCategory().name().equals(category))
	                .collect(Collectors.toList());
	    }
		
//		list = new ArrayList<>(list);
		
		// 정렬 적용
	    switch (filter) {
	        case "reviewCount":
	            // 리뷰 많은 순으로 정렬
	            list.sort(Comparator.comparing(Location::getReviewCount).reversed());
	            break;
	        case "rating":
	            // 평점 높은 순으로 정렬
	            list.sort(Comparator.comparing(Location::getLocationRating).reversed());
	            break;
	        case "distance":
	        	if(userLat != null && userLng != null) {
	        		list.sort(Comparator.comparingDouble(location ->
	        		locationService.calculateDistance(userLat, userLng, location.getLocationLatitude(), location.getLocationLongitude()
	        				)
	        		));
	        	}
	        	break;
	        case "myLocations":
	        	// 내가 작성한 추천글만 필터링
	        	list = list.stream().filter(location -> location.getUser().getUserId()
	        			.equals(user.getUserId())).collect(Collectors.toList());
	        	break;
	        default:
	            // 기본 정렬 (LocationID 순)
	            list.sort(Comparator.comparing(Location::getLocationId));
	            break;
	    }
	    
	    // 특정 지역, 특정 카테고리, 특정 정렬기준을 모두 만족하는 결과가 없는 경우
	    boolean isEmpty = list.isEmpty();
	    
	    //가져온 거 모델에 담고
	    model.addAttribute("list",list);
	    model.addAttribute("isEmpty",isEmpty);
	    model.addAttribute("selectedRegion",region);
	    model.addAttribute("selectedFilter",filter);
	    model.addAttribute("selectedCategory",category);
	    model.addAttribute("search", search);
	    model.addAttribute("nickname", user.getName());
		
		return "location/locationList";
		}
	
	//장소 추천 글쓰기 페이지 이동
	@GetMapping("locationwrite")
	public String writeForm(Model model) {

		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		model.addAttribute("locationWrite", new LocationWrite());
		return "location/locationwrite";
		}
	
	// 장소 추천 글쓰기
	@PostMapping("locationwrite")
	public String writeLocation(@Validated @ModelAttribute LocationWrite locationWrite
								, BindingResult result
								, Model model
								,@RequestParam(name = "userLat", required = false) Double userLat
								,@RequestParam(name = "userLng", required = false) Double userLng) {
		if (result.hasErrors()) {
			return "location/locationwrite";
		}
		
		// 중복 확인
	    boolean exists = locationService.isLocationExists(locationWrite.getLocationName(), locationWrite.getLocationAddress());
	    if (exists) {
	        model.addAttribute("errorMessage", "해당 장소는 이미 등록되어 있습니다.");
	        return "location/locationwrite";
	    }
		
		//엔티티인 Location 객체로 변환하고
		Location location = LocationWrite.toLocation(locationWrite);
		
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		location.setUser(user);
		
		// 거리 계산
	    if (userLat != null && userLng != null) {
	        double distance = locationService.calculateDistance(
	            userLat, userLng, location.getLocationLatitude(), location.getLocationLongitude()
	        );
	        location.setLocationDistance(distance);
	    } else {
	        location.setLocationDistance(0.0); // 기본값 설정
	    }
		
		// DB에 글 등록
		locationService.saveLocation(location);
		
		model.addAttribute("successScript", "<script> alert('성공적으로 등록되었습니다.'); window.opener.location.reload(); window.close();</script>");
		
		return "location/locationwrite";
		}
	
	//장소 추천 글 읽기
	@GetMapping("locationRead")
	public String ReadLocation(@RequestParam("id") Long locationId
							,Model model
							,@RequestParam(name="filter", required = false, defaultValue = "default")String filter
							,@RequestParam(name = "name", required = false) String nickname) {
		
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		addAuthenticatedUserToModel(model);
		
		// 요청할 때 날아온 쿼리 파라미터로 repository에 있는 로케이션 객체 하나 가져오기 서비스타고
		Location location = locationService.findLocation(locationId);
		//리뷰 개수 가져오기
		long reviewCount = reviewService.countByLocationId(locationId);
		//리뷰 개수 DB에 반영
		locationService.updateReviewCount(locationId, reviewCount);
		// 해당 장소추천 게시글의 리뷰들 가져와야지 서비스 타고
		List<Review> listR = reviewService.findByLocationLocationId(locationId);
		// 리뷰 평점 계산
		Float locationRating = reviewService.calculateAverageRating(locationId);
		//리뷰 평점 계산 DB에 반영
		locationService.updateLocationRating(locationId, locationRating);
		// 정렬 적용
	    switch (filter) {
	        case "reviewRating":
	            // 리뷰 평점 높은 순
	            listR.sort(Comparator.comparing(Review::getReviewRating).reversed());
	            break;
	        case "reviewDate":
	            // 방문날짜 최신순
	            listR.sort(Comparator.comparing(Review::getReviewDate).reversed());
	            break;
	        case "myReviews":
	        	//내가 쓴 리뷰
	        	if(user != null) {
	        		listR = listR.stream()
	        						.filter(review -> review.getUser().getUserId().equals(user.getUserId()))
	        						.toList();
	        	}
	        	break;
	        default:
	            // 기본 정렬 (작성순)
	            listR.sort(Comparator.comparing(Review::getReviewId));
	            break;
	    }
	    //가져온 것들 모델에 담기
	    model.addAttribute("location", location);
	    model.addAttribute("reviewCount", reviewCount);
	    model.addAttribute("listR", listR);
	    model.addAttribute("locationRating", locationRating);
	    model.addAttribute("selectedFilter", filter);
	    model.addAttribute("nickname", user.getName());
	    
		return "location/locationRead";
		}
	
	//장소 추천 글 수정하기 페이지 이동
	@GetMapping("locationUpdate")
	public String updateForm(Model model
							, @RequestParam("id") Long locationId
							) {

		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		// locationId에 해당하는 location 찾기
		Location location = locationService.findLocation(locationId);
		
		LocationUpdate locationUpdate = Location.toLocationUpdate(location);
		
		model.addAttribute("locationUpdate", locationUpdate);
		
		return "location/locationUpdate";
		}
	
	//수정하기
	@PostMapping("locationUpdate")
	public String UpdateLocation(@Validated @ModelAttribute LocationUpdate locationUpdate
								, BindingResult result
								, Model model) {
		
		if(result.hasErrors()) {
			return "location/locationUpdate";
		}
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		// 중복 확인
	    boolean exists = locationService.isLocationExists(locationUpdate.getLocationName(), locationUpdate.getLocationAddress());
	    if (exists) {
	        model.addAttribute("errorMessage", "해당 장소는 이미 등록되어 있습니다.");
	        return "location/locationUpdate";
	    }
		
		// 수정작업을 위한 엔티티 변환
		Location updateLocation = LocationUpdate.toLocation(locationUpdate);
		// 서비스에 있는 업데이트 보드로 던저벼리기
		locationService.updateLocation(updateLocation);
		
		// 성공 후 alert 및 팝업 닫기
		model.addAttribute("successScript", "<script>alert('성공적으로 수정되었습니다.'); window.opener.location.reload(); window.close();</script>");
		
		return "location/locationUpdate";
		}
	
	// 장소 추천 글 삭제
	@GetMapping("locationDelete")
	public String RemoveLocation(@RequestParam(name="id", required = false) Long locationId) {
		
		User user = getAuthenticatedUser();
		if(user == null) {
			return "redirect:/user/login";
		}
		
		Location findLocation = locationService.findLocation(locationId);

		locationService.removeLocation(locationId);
		
		return "redirect:/location/locationList";
		}
	
	// 인증된 사용자 가져오기 메소드
	private User getAuthenticatedUser() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null && authentication.isAuthenticated()) {
	        Object principal = authentication.getPrincipal();
	        if (principal instanceof CustomUserDetails userDetails) {
	            return userDetails.getUser();
	        } else if (principal instanceof DefaultOAuth2User oAuth2User) {
	            String providerUserId = (String) oAuth2User.getAttributes().get("sub");
	            return userService.findUserByProviderUserId(providerUserId);
	        }
	    }
	    return null;
	}
	
	private void addAuthenticatedUserToModel(Model model) {
	    User user = getAuthenticatedUser();
	    if (user != null) {
	        model.addAttribute("sessionUser", user);
	    }
	}
	
	//장소 중복 검증
	@PostMapping("/checkDuplicate")
	@ResponseBody
	public Map<String, Boolean> checkDuplicate(@RequestBody Map<String, String> requestData) {
	    String locationName = requestData.get("locationName");
	    String locationAddress = requestData.get("locationAddress");

	    boolean isDuplicate = locationService.isLocationExists(locationName, locationAddress);

	    Map<String, Boolean> response = new HashMap<>();
	    response.put("isDuplicate", isDuplicate);

	    return response;
	}
	
	//유저 거리 업데이트
	@PostMapping("/updateDistances")
	@ResponseBody
	public Map<String, Object> updateDistances(@RequestBody Map<String, Double> userLocation) {
	    Double userLat = userLocation.get("userLat");
	    Double userLng = userLocation.get("userLng");

	    if (userLat == null || userLng == null) {
	        throw new IllegalArgumentException("사용자 위치 정보가 유효하지 않습니다.");
	    }

	    List<Location> locations = locationService.findAll();

	    // 각 장소와 사용자의 거리 계산 및 업데이트
	    locations.forEach(location -> {
	        double distance = locationService.calculateDistance(
	            userLat, userLng, location.getLocationLatitude(), location.getLocationLongitude()
	        );
	        location.setLocationDistance(distance);
	        locationService.saveLocation(location);
	    });

	    Map<String, Object> response = new HashMap<>();
	    response.put("status", "success");
	    response.put("message", "거리 정보가 업데이트되었습니다.");
	    return response;
	}

}

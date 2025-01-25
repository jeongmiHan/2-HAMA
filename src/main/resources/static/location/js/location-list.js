// 지도 초기화
var mapContainer = document.getElementById('map'); // 지도를 표시할 div
var mapOption = {
    center: new kakao.maps.LatLng(35.1059, 129.0377), // 초기 중심 좌표
    level: 4 // 지도 확대 레벨
};
//지도 생성
var map = new kakao.maps.Map(mapContainer, mapOption);
// 장소 검색 서비스 객체 생성
var ps = new kakao.maps.services.Places();
var markers = []; // 마커 배열
var currentInfoWindow = null; // 현재 열려 있는 인포윈도우를 저장하는 변수
var infowindow = new kakao.maps.InfoWindow({ zIndex: 1 }); // 인포윈도우 객체 생성
// 장소 검색 함수
function searchPlaces() {
	var keyword = document.getElementById('keyword').value;
	
	if (!keyword) {
	    alert('키워드를 입력하세요');
	    return;
	}
	// 장소 검색
	ps.keywordSearch(keyword, placesSearchCB);
}
// 장소 검색 콜백 함수
function placesSearchCB(data, status, pagination) {
    if (status === kakao.maps.services.Status.OK) {
        // 검색된 장소 마커와 리스트 표시
        displayPlaces(data);
    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
        alert('검색 결과가 없습니다.');
    } else {
        alert('검색 중 오류가 발생했습니다.');
    }
}	
// 검색된 장소 마커와 리스트 표시 함수
function displayPlaces(places) {
    // 이전 마커 및 인포윈도우 제거
    removeMarkers();

    var bounds = new kakao.maps.LatLngBounds(); // 지도 범위 객체
    var placesList = document.getElementById('places-list');
    placesList.innerHTML = ''; // 이전 리스트 초기화

    places.forEach(function(place, index) {
        // 마커 생성 및 지도에 표시
        var markerPosition = new kakao.maps.LatLng(place.y, place.x);
        var marker = new kakao.maps.Marker({
            position: markerPosition,
            map: map
        });
        // 마커 배열에 추가
        markers.push(marker);

        // 지도 범위 확장
        bounds.extend(markerPosition);
        
     	// 마커 클릭 이벤트
        kakao.maps.event.addListener(marker, 'click', function() {
        	// 기존에 열려 있던 인포윈도우 닫기
            if (currentInfoWindow) {
                currentInfoWindow.close();
            }
            // 새로운 인포윈도우 생성 및 열기
            var infowindowContent = `
                <div style="padding:5px; font-size:12px;">
                    ${place.place_name}<br>
                    ${place.road_address_name || place.address_name}<br>
                    <button onclick="openRoute(${place.y}, ${place.x}, '${place.place_name}')">길찾기</button>
                </div>
            `;
            var infowindow = new kakao.maps.InfoWindow({
                content: infowindowContent
            });
            infowindow.open(map, marker);

            // 현재 열려 있는 인포윈도우 업데이트
            currentInfoWindow = infowindow;
        });

// 리스트에 장소 추가
var listItem = document.createElement('div');
listItem.className = 'place-item';
listItem.innerHTML = `
    <strong>${place.place_name}</strong><br>
    ${place.road_address_name || place.address_name}<br>
    ${place.phone || '전화번호 없음'}
`;
// 애니메이션 적용
setTimeout(() => {
    listItem.classList.add('show');
}, index * 100);

//리스트 항목 클릭 이벤트
listItem.onclick = function() {
	openPopup(place); // 팝업띄우기
	map.setLevel(4); // 확대 레벨
    map.setCenter(markerPosition); // 클릭한 장소로 지도 중심 이동
 	// 기존에 열려 있던 인포윈도우 닫기
    if (currentInfoWindow) {
        currentInfoWindow.close();
    }

// 새로운 인포윈도우 생성 및 열기
var infowindowContent = `
    <div style="padding:5px; font-size:12px;">
        ${place.place_name}<br>
        ${place.road_address_name || place.address_name}<br>
        <button onclick="openRoute(${place.y}, ${place.x}, '${place.place_name}')">길찾기</button>
    </div>
`;
var infowindow = new kakao.maps.InfoWindow({
    content: infowindowContent
});
infowindow.open(map, marker);

// 현재 열려 있는 인포윈도우 업데이트
currentInfoWindow = infowindow;
};

placesList.appendChild(listItem);
});

// 지도 범위를 검색된 장소에 맞게 조정
map.setBounds(bounds);
}

// 기존 마커 및 윈포윈도우 제거 함수
function removeMarkers() {
    markers.forEach(function(marker) {
        marker.setMap(null); //지도에서 마커 제거
    });
    markers = []; //마커배열 초기화
 	// 열려 있는 인포윈도우 닫기
    if (currentInfoWindow) {
        currentInfoWindow.close();
        currentInfoWindow = null; // 변수 초기화
    }
}
// 길찾기 URL 열기 함수
function openRoute(lat, lng, placeName) {
var routeUrl = `https://map.kakao.com/link/to/${encodeURIComponent(placeName)},${lat},${lng}`;
window.open(routeUrl, '_blank'); // 새 탭에서 열기
}

// 팝업 열기 함수
function openPopup(place) {
    document.getElementById('popup-title').innerText = place.place_name;
    document.getElementById('popup-address').innerText = `주소: ${place.road_address_name || place.address_name}`;
    document.getElementById('popup-phone').innerText = `전화번호: ${place.phone || '정보 없음'}`;
    document.getElementById('popup-route-btn').onclick = function () {
        openRoute(place.y, place.x, place.place_name);
    };
    document.getElementById('popup').classList.remove('hidden');
}
// 팝업 닫기 함수
function closePopup() {
    document.getElementById('popup').classList.add('hidden');
}
//지도, 위성, 확대바
var mapContainer = document.getElementById('map'), // 지도를 표시할 div 
mapOption = { 
    center: new kakao.maps.LatLng(userLat, userLng), // 지도의 중심좌표
    level: 5 // 지도의 확대 레벨
};


//일반 지도와 스카이뷰로 지도 타입을 전환할 수 있는 지도타입 컨트롤을 생성합니다
var mapTypeControl = new kakao.maps.MapTypeControl();

//지도에 컨트롤을 추가해야 지도위에 표시됩니다
//kakao.maps.ControlPosition은 컨트롤이 표시될 위치를 정의하는데 TOPRIGHT는 오른쪽 위를 의미합니다
map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

//지도 확대 축소를 제어할 수 있는  줌 컨트롤을 생성합니다
var zoomControl = new kakao.maps.ZoomControl();
map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

//지도 교통정보, 지형정보
// 지도에 추가된 지도타입정보를 가지고 있을 변수입니다
var currentTypeId;
// 버튼이 클릭되면 호출되는 함수입니다
function setOverlayMapTypeId(maptype) {
    var changeMaptype;  
    // maptype에 따라 지도에 추가할 지도타입을 결정합니다
    if (maptype === 'traffic') {            
        changeMaptype = kakao.maps.MapTypeId.TRAFFIC;     
    } else if (maptype === 'terrain') {
        changeMaptype = kakao.maps.MapTypeId.TERRAIN;    
    } else if (maptype === 'bicycle') {
        changeMaptype = kakao.maps.MapTypeId.BICYCLE;    
    }
    // 이미 등록된 지도 타입이 있으면 제거합니다
    if (currentTypeId) {
        map.removeOverlayMapTypeId(currentTypeId);    
    }
    
    // maptype에 해당하는 지도타입을 지도에 추가합니다
    map.addOverlayMapTypeId(changeMaptype);
    
    // 지도에 추가된 타입정보를 갱신합니다
    currentTypeId = changeMaptype;        
}
//지도 초기화 함수
function resetMap() {
    if (currentTypeId) {
        map.removeOverlayMapTypeId(currentTypeId); // 지도 타입 제거
    }
        currentTypeId = null; // 상태 초기화
}
// 수정 및 삭제
function confirmAction(url, message) {
    if (confirm(message)) {
        window.location.href = url;
    }
}

//페이지 로드 시 사용자 위치를 가져와 장소추천 버튼에 쿼리 추가
document.addEventListener('DOMContentLoaded', function () {
    const userLatInput = document.getElementById('userLat');
    const userLngInput = document.getElementById('userLng');
    const recommendLink = document.querySelector('.navbar-menu li a[href="/location/locationList"]');

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function (position) {
                const userLat = position.coords.latitude;
                const userLng = position.coords.longitude;

                // 위치 정보를 숨겨진 input에 저장
                if (userLatInput && userLngInput) {
                    userLatInput.value = userLat;
                    userLngInput.value = userLng;
                }

                // 장소추천 버튼 클릭 시 위치 정보 포함 URL로 이동
                if (recommendLink) {
                    recommendLink.onclick = function (event) {
                        event.preventDefault();
                        location.href = `/location/locationList?userLat=${userLat}&userLng=${userLng}&region=ALL&category=ALL&filter=default`;
                    };
                }

                // 지도 중심을 사용자 위치로 설정
                map.setCenter(new kakao.maps.LatLng(userLat, userLng));
            },
            function (error) {
                // 위치 정보를 가져오는 데 실패한 경우 처리
                console.error("위치 정보를 가져올 수 없습니다:", error);

                let errorMessage = "위치 정보를 가져올 수 없습니다.";
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        errorMessage = "위치 정보 접근이 거부되었습니다. 브라우저 설정을 확인하세요.";
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorMessage = "위치 정보를 사용할 수 없습니다. 네트워크 상태를 확인하세요.";
                        break;
                    case error.TIMEOUT:
                        errorMessage = "위치 정보를 가져오는 데 시간이 초과되었습니다.";
                        break;
                    default:
                        errorMessage = "알 수 없는 에러가 발생했습니다.";
                        break;
                }

                alert(errorMessage);

                if (recommendLink) {
                    recommendLink.onclick = function () {
                        location.href = `/location/locationList`;
                    };
                }
            },
            {
                enableHighAccuracy: true, // 높은 정확도 요청
                timeout: 10000, // 최대 대기 시간 (ms)
                maximumAge: 0 // 캐시된 위치를 사용하지 않음
            }
        );
    } else {
        alert("이 브라우저는 위치 정보를 지원하지 않습니다.");

        if (recommendLink) {
            recommendLink.onclick = function () {
                location.href = `/location/locationList`;
            };
        }
    }
});

//도움말 말풍선
function toggleHelpBalloon() {
    const helpBalloon = document.getElementById('helpBalloon');
    if (helpBalloon.classList.contains('hidden')) {
        helpBalloon.classList.remove('hidden');
    } else {
        helpBalloon.classList.add('hidden');
    }
}

//더보기
document.addEventListener('DOMContentLoaded', function () {
    let currentIndex = 5; // 현재 표시된 게시글의 개수
    const items = document.querySelectorAll('.location-item'); // 게시글 목록 가져오기
    const loadMoreBtn = document.getElementById('loadMoreBtn'); // "더보기" 버튼

    // 초기 상태: 첫 5개만 표시
    items.forEach((item, index) => {
        if (index >= currentIndex) {
            item.classList.add('hidden');
        }
    });

    // "더보기" 버튼 클릭 이벤트
    loadMoreBtn.addEventListener('click', function () {
        let hiddenItemsCount = 0;

        // 다음 5개 항목을 표시
        for (let i = currentIndex; i < currentIndex + 5 && i < items.length; i++) {
            items[i].classList.remove('hidden');
            hiddenItemsCount++;
        }

        // 현재 인덱스 업데이트
        currentIndex += hiddenItemsCount;

        // 모든 항목이 표시되면 버튼 숨기기
        if (currentIndex >= items.length) {
            loadMoreBtn.style.display = 'none';
        }
    });
});
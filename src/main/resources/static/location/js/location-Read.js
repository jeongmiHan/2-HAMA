//리뷰 사진 모달
let imagePaths = [];
let currentIndex = 0;

//모달 창 열기
function openImageModal(imgElement) {
    // data-images 속성에서 이미지 경로를 가져오기
    let paths = imgElement.getAttribute('data-images');
    imagePaths = paths.split(','); // 콤마로 구분된 경로를 배열로 변환
    currentIndex = 0;

    // 첫 번째 이미지 표시
    updateModalImage();
    document.getElementById('imageModal').style.display = 'block';
}

// 모달 이미지 업데이트
function updateModalImage() {
    document.getElementById('modalImage').src = '/uploads/' + imagePaths[currentIndex].trim();
}

// 이전 이미지 보기
function prevImage() {
    if (currentIndex > 0) {
        currentIndex--;
        updateModalImage();
    }
}

// 다음 이미지 보기
function nextImage() {
    if (currentIndex < imagePaths.length - 1) {
        currentIndex++;
        updateModalImage();
    }
}

// 모달 이미지 업데이트
function updateModalImage() {
	let modalImage = document.getElementById('modalImage');
    modalImage.src = '/uploads/' + imagePaths[currentIndex].trim(); // 절대경로 보장
}

//닫기
function closeImageModal() {
    document.getElementById('imageModal').style.display = 'none';
    imagePaths = []; // 이미지 경로 초기화
    currentIndex = 0; // 인덱스 초기화
}
//수정, 삭제
function confirmAction(url, message) {
    if (confirm(message)) {
        window.location.href = url;
    }
}

//카카오 맵 열기
var locationLat = [[${location.locationLatitude}]]; // 장소의 위도
var locationLng = [[${location.locationLongitude}]]; // 장소의 경도
var locationName = '[[${location.locationName}]]'; // 장소 이름

// 지도 초기화
var mapContainer = document.getElementById('map'); 
var mapOption = {
    center: new kakao.maps.LatLng(locationLat, locationLng), 
    level: 4 
};

var map = new kakao.maps.Map(mapContainer, mapOption);

// 마커 생성
var markerPosition = new kakao.maps.LatLng(locationLat, locationLng); // 장소의 위치
var marker = new kakao.maps.Marker({
    position: markerPosition,
});
marker.setMap(map);
//InfoWindow 생성
var infoWindowContent = `
    <div style="padding:5px; text-align:center;">
        <strong>${locationName}</strong><br>
        <button onclick="openRoute(${locationLat}, ${locationLng}, '${locationName}')">
            길찾기
        </button>
    </div>
`;
var infoWindow = new kakao.maps.InfoWindow({
    content: infoWindowContent, // 말풍선의 HTML 내용
    removable: true // 닫기 버튼 표시 여부
});

//InfoWindow 생성 (말풍선)
var infoWindowContent = `
    <div style="padding:10px; text-align:center; font-size:14px;">
        <strong>${locationName}</strong><br>
        <button onclick="openRoute(${locationLat}, ${locationLng}, '${locationName}')"
                style="margin-top:5px; padding:5px 10px; background-color:#007bff; color:white; border:none; border-radius:5px; cursor:pointer;">
            길찾기
        </button>
    </div>
`;
var infoWindow = new kakao.maps.InfoWindow({
    content: infoWindowContent, // 말풍선의 HTML 내용
    removable: false // 닫기 버튼 표시 여부 (true: 닫기 버튼 표시, false: 닫기 버튼 숨김)
});

// 지도 로드 시 InfoWindow 표시
infoWindow.open(map, marker); // 지도와 마커에 말풍선 표시

// 길찾기 버튼 함수
function openRoute(lat, lng, name) {
    var routeUrl = `https://map.kakao.com/link/to/${encodeURIComponent(name)},${lat},${lng}`;
    window.open(routeUrl, '_blank'); // 새 창에서 길찾기 페이지 열기
}
//중심을 장소로 이동
map.setCenter(new kakao.maps.LatLng(locationLat, locationLng));

//일반 지도와 스카이뷰로 지도 타입을 전환할 수 있는 지도타입 컨트롤을 생성합니다
var mapTypeControl = new kakao.maps.MapTypeControl();

//지도에 컨트롤을 추가해야 지도위에 표시됩니다
//kakao.maps.ControlPosition은 컨트롤이 표시될 위치를 정의하는데 TOPRIGHT는 오른쪽 위를 의미합니다
map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

//지도 확대 축소를 제어할 수 있는  줌 컨트롤을 생성합니다
var zoomControl = new kakao.maps.ZoomControl();
map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

//지도 교통정보, 지형정보
//지도에 추가된 지도타입정보를 가지고 있을 변수입니다
var currentTypeId;
//버튼이 클릭되면 호출되는 함수입니다
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
     currentTypeId = null; // 상태 초기화
 }
}


//방문자 사진
document.addEventListener("DOMContentLoaded", () => {
    const visitorPhotoList = document.getElementById("visitor-photo-list");

    let currentIndex = 0; // 현재 인덱스
    let itemWidth = 0; // 슬라이더 아이템의 너비
    let totalItems = 0; // 슬라이더 전체 아이템 개수

    // 방문자 사진 리스트 생성
    const reviewItems = document.querySelectorAll(".review-item");
    reviewItems.forEach((reviewItem) => {
        const reviewImages = reviewItem.querySelector(".review-image img");
        if (reviewImages) {
            const images = reviewImages.getAttribute("data-images").split(",");

            images.forEach((path) => {
                const trimmedPath = path.trim();

                // 방문자 사진 아이템 생성
                const photoItem = document.createElement("div");
                photoItem.classList.add("visitor-photo-item");

                // 이미지 태그 생성
                const imgElement = document.createElement("img");
                imgElement.src = `/uploads/${trimmedPath}`;
                imgElement.alt = "방문자 사진";

                // 클릭 이벤트: 모달 창 열기
                imgElement.addEventListener("click", () => openImageModal(imgElement));
                
                photoItem.appendChild(imgElement);
                visitorPhotoList.appendChild(photoItem);
            });
        }
    });

    // 클론 생성: 처음과 끝 복사
    const items = visitorPhotoList.children;
    totalItems = items.length;
    
 	// 8개 이하일 경우 슬라이더 적용하지 않음
    if (totalItems < 9) {
        return; // 슬라이더 기능 종료
    }

    // 앞뒤 클론 추가
    Array.from(items).forEach((item) => {
        const cloneFirst = item.cloneNode(true);
        visitorPhotoList.appendChild(cloneFirst); // 끝에 추가
    });

    // 초기 설정
    const updateSlider = () => {
        itemWidth = items[0].offsetWidth + 10; // 아이템 너비 + 간격
        visitorPhotoList.style.transform = `translateX(-${currentIndex * itemWidth}px)`; // 초기 위치로 이동
    };
    updateSlider();
    window.addEventListener("resize", updateSlider); // 화면 크기 변경 시 업데이트

    // 슬라이더 작동
    function startSlider() {
        setInterval(() => {
            currentIndex++;
            visitorPhotoList.style.transition = "transform 0.5s ease-in-out";
            visitorPhotoList.style.transform = `translateX(-${currentIndex * itemWidth}px)`;

            // 무한 루프 처리
            setTimeout(() => {
                if (currentIndex >= totalItems) {
                    // 첫 번째로 되돌아가기
                    currentIndex = 0;
                    visitorPhotoList.style.transition = "none";
                    visitorPhotoList.style.transform = `translateX(-${currentIndex * itemWidth}px)`;
                }
            }, 500); // 애니메이션 시간 후 리셋
        }, 3000); // 3초 간격
    }

    startSlider();
});
// 버튼 눌러서 장소 리스트 갈때, 위치정보 가져가게
document.addEventListener('DOMContentLoaded', function () {
            const recommendLink = document.querySelector('.navbar-menu li a[href="/location/locationList"]');

            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    function (position) {
                        const userLat = position.coords.latitude;
                        const userLng = position.coords.longitude;

                        // 장소추천 링크 클릭 시 좌표 포함 URL로 이동
                        recommendLink.onclick = function (event) {
                            event.preventDefault(); // 기본 동작 막기
                            location.href = `/location/locationList?userLat=${userLat}&userLng=${userLng}&region=ALL&category=ALL&filter=default`;
                        };
                    },
                    function (error) {
                        console.error("위치 정보를 가져올 수 없습니다:", error);
                        // 위치 정보를 가져오지 못하면 기본 URL로 이동
                        recommendLink.onclick = function () {
                            location.href = `/location/locationList`;
                        };
                    }
                );
            } else {
                console.error("브라우저가 위치 정보를 지원하지 않습니다.");
                recommendLink.onclick = function () {
                    location.href = `/location/locationList`;
                };
            }
        });

//방문자 사진 모달 관련 코드
let visitorImagePaths = [];
let visitorCurrentIndex = 0;

// 방문자 사진 클릭 시 모달 열기
function openVisitorImageModal(imgElement) {
    const allVisitorImages = document.querySelectorAll('.visitor-photo-item img');
    visitorImagePaths = Array.from(allVisitorImages).map(img => img.src);
    visitorCurrentIndex = Array.from(allVisitorImages).indexOf(imgElement);

    updateVisitorModalImage();
    document.getElementById('visitorImageModal').style.display = 'block';
}

// 방문자 사진 모달 이미지 업데이트
function updateVisitorModalImage() {
    const modalImage = document.getElementById('visitorModalImage');
    modalImage.src = visitorImagePaths[visitorCurrentIndex];
}

// 이전 방문자 사진 보기
function prevVisitorImage() {
    visitorCurrentIndex = (visitorCurrentIndex > 0) ? visitorCurrentIndex - 1 : visitorImagePaths.length - 1;
    updateVisitorModalImage();
}

// 다음 방문자 사진 보기
function nextVisitorImage() {
    visitorCurrentIndex = (visitorCurrentIndex < visitorImagePaths.length - 1) ? visitorCurrentIndex + 1 : 0;
    updateVisitorModalImage();
}

// 방문자 사진 모달 닫기
function closeVisitorImageModal() {
    document.getElementById('visitorImageModal').style.display = 'none';
}

// 방문자 사진 이벤트 초기화
function initializeVisitorPhotoModal() {
    const visitorImages = document.querySelectorAll('.visitor-photo-item img');
    visitorImages.forEach(img => {
        img.addEventListener('click', () => openVisitorImageModal(img));
    });
}

document.addEventListener('DOMContentLoaded', initializeVisitorPhotoModal);

//도움말 말풍선
function toggleHelpBalloon() {
    const helpBalloon = document.getElementById('helpBalloon');
    if (helpBalloon.classList.contains('hidden')) {
        helpBalloon.classList.remove('hidden');
    } else {
        helpBalloon.classList.add('hidden');
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const reviews = document.querySelectorAll('.review-item'); // 리뷰 항목
    const loadMoreBtn = document.getElementById('loadMoreReviewsBtn'); // "더보기" 버튼
    let currentIndex = 5; // 초기 표시할 리뷰 개수

    // 초기 상태: 첫 5개만 표시
    reviews.forEach((review, index) => {
        if (index >= currentIndex) {
            review.classList.add('hidden');
        }
    });

    // "더보기" 버튼 클릭 이벤트
    loadMoreBtn.addEventListener('click', function () {
        let hiddenCount = 0;

        // 다음 5개 항목을 표시
        for (let i = currentIndex; i < currentIndex + 5 && i < reviews.length; i++) {
            reviews[i].classList.remove('hidden');
            hiddenCount++;
        }

        // 현재 인덱스 업데이트
        currentIndex += hiddenCount;

        // 모든 리뷰가 표시되면 "더보기" 버튼 숨기기
        if (currentIndex >= reviews.length) {
            loadMoreBtn.style.display = 'none';
        }
    });
});
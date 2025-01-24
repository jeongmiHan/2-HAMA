let ps = new kakao.maps.services.Places(); // 장소 검색 객체
let geocoder = new kakao.maps.services.Geocoder(); // 주소 변환 객체

function searchPlaces() {
  let keyword = document.getElementById('locationName').value;

  if (!keyword) {
    return; // 입력값 없으면 중단
  }

  ps.keywordSearch(keyword, function(data, status) {
    let placeList = document.getElementById('placeList');
    placeList.innerHTML = ""; // 기존 결과 초기화

    if (status === kakao.maps.services.Status.OK) {
      // 검색 결과 추가
      data.forEach(function(place) {
        let listItem = document.createElement('li');
        listItem.innerText = place.place_name; // 장소명 표시

        listItem.onclick = function() {
          // 선택한 장소 정보를 입력
          document.getElementById('locationName').value = place.place_name;
          document.getElementById('locationAddress').value = place.address_name || place.road_address_name;

          // 좌표 변환 및 저장
          geocoder.addressSearch(place.address_name || place.road_address_name, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
              document.getElementById('locationLatitude').value = result[0].y;
              document.getElementById('locationLongitude').value = result[0].x;
            }
          });

          placeList.innerHTML = ""; // 목록 초기화
        };
        placeList.appendChild(listItem); // 결과 추가
      });
    } else {
      placeList.innerHTML = "<li>검색 결과가 없습니다.</li>";
    }
  });
}

//장소 초기화 함수 추가
function clearAddress() {
    const locationName = document.getElementById('locationName').value; // 장소명 확인
    if (!locationName) { // 입력값이 비어있으면 주소와 좌표 초기화
        document.getElementById('locationAddress').value = ''; // 주소 초기화
        document.getElementById('locationLatitude').value = ''; // 위도 초기화
        document.getElementById('locationLongitude').value = ''; // 경도 초기화
    }
}

function validateForm() {
  // 입력값 검증
  const locationName = document.getElementById('locationName').value.trim(); // 장소명
  const locationAddress = document.getElementById('locationAddress').value.trim(); // 주소
  const locationCategory = document.querySelector('input[name="locationCategory"]:checked'); // 카테고리
  
  // 장소명 검증
  if (!locationName) {
    alert("장소명을 입력해주세요.");
    return false; // 폼 제출 중단
  }

  // 카테고리 검증
  if (!locationCategory) {
    alert("카테고리를 선택해주세요.");
    return false; // 폼 제출 중단
  }
  
  //서버에서 중복 장소 확인 (AJAX 요청)
  const isDuplicate = checkDuplicateLocation(locationName, locationAddress);

  // 중복된 장소가 확인되면 경고 표시 후 폼 제출 중단
  if (isDuplicate) {
    alert("이미 등록된 장소입니다.");
    return false;
  }

  // 모든 검증 통과 시 제출 허용
  return true;
}

//중복 장소 확인 함수 (비동기 요청)
function checkDuplicateLocation(locationName, locationAddress) {
  let isDuplicate = false;

  // 동기 방식으로 서버에 요청
  const xhr = new XMLHttpRequest();
  xhr.open("POST", "/location/checkDuplicate", false); // 동기 요청
  xhr.setRequestHeader("Content-Type", "application/json");
  xhr.send(JSON.stringify({ locationName, locationAddress }));

  if (xhr.status === 200) {
    const response = JSON.parse(xhr.responseText);
    isDuplicate = response.isDuplicate; // 서버에서 반환된 중복 여부
  } else {
    alert("서버와의 통신 중 오류가 발생했습니다.");
  }

  return isDuplicate;
}
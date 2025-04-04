let calendar, currentEvent = null;
let allEvents = []; // 모든 이벤트를 저장하는 배열
let petImages = {};

document.addEventListener('DOMContentLoaded', async function() {
   var calendarEl = document.getElementById('calendar');  // 캘린더가 표시될 요소
   // FullCalendar 초기화
   calendar = new FullCalendar.Calendar(calendarEl, {
      // 캘린더를 한국어로 설정
      initialView: 'dayGridMonth', // 초기 뷰를 월간 보기로 설정
      // 캘린더 헤더 툴바 설정
      headerToolbar: {
         left: 'dayGridMonth,timeGridWeek',
         center: 'title',
         right: 'prev,today,next'
      },
      // 구글 캘린더 API 연동 (공휴일 표시용)
      googleCalendarApiKey: "AIzaSyBao8RIMQ_OxmPaGgmvg97seYRngfMGMwE",
      eventSources: [
         {
            googleCalendarId: 'ko.south_korea#holiday@group.v.calendar.google.com'
            , color: '#f8f9fc'   // 배경색
            , textColor: 'red' // 글자색
         }
      ],
     
     // Google Calendar 이벤트의 URL을 제거하는 필수 설정
             eventDataTransform: function(event) {
                 if (event.url) {
                     event.url = "";  // URL을 빈 문자열로 변경하여 이동 방지
                 }
                 return event;
             },
           
      // 날짜 클릭 시 이벤트 (일정 추가 모달 표시)
      dateClick: function(info) {
      resetEventModal(); // 모달 상태 초기화
         const start = info.dateStr; // 시작 시간
         const end = info.dateStr;   // 종료 시간
         showEventModal({ start, end }); // 모달 창 표시
         
      },
      // 이벤트 클릭 시 이벤트 (수정 모달 표시)
      eventClick: function(info) {
      //  공휴일 이벤트인지 확인하고 이동 방지
              if (info.event.source && info.event.source.internalEventSource.googleCalendarId) {
                  info.jsEvent.preventDefault();  // 기본 동작 막기
                  info.jsEvent.stopPropagation(); // 이벤트 전파 방지
                  return; // 더 이상 실행되지 않도록 종료
              }
         currentEvent = info.event; // 클릭한 이벤트 저장
         showEventModalForEdit(info.event);  // 수정용 모달 창 표시
      },
      displayEventTime: false, // 캘린더에 이벤트 시간 숨기고 제목만 보여주는 기능
      // 이벤트 렌더링 후 실행 (필터링 기능을 위해 모든 이벤트 저장)
     eventDidMount: function(info) {
      updateEventUI(info.event); // 이벤트 UI 업데이트
         if (!allEvents.some(event => event.id === info.event.id)) {
             allEvents.push(info.event);
         }

         const eventEl = info.el;
         const petImage = info.event.extendedProps.petImage;
         const eventColor = info.event.extendedProps.cd_color || "#000"; // 기본 검은색

         // 기존의 중복 요소 삭제
         let existingContainer = eventEl.querySelector('.event-title-container');
         if (existingContainer) {
             existingContainer.remove();
         }

         // 프로필에서 사진 가져오기
         const titleContainer = document.createElement('div');
         titleContainer.classList.add('event-title-container');
         titleContainer.innerHTML = `
             ${petImage ? `<img src="${petImage}" class="event-pet-icon">` : ''}
         `;

         eventEl.appendChild(titleContainer);
     },
      // 캘린더 날짜 변경 시 필터 드롭다운을 툴바에 추가
      datesSet: function() {
         const toolbarLeft = document.querySelector('.fc-toolbar.fc-header-toolbar .fc-toolbar-chunk:first-child');
         if (!toolbarLeft.contains(filterDropdown)) {
            toolbarLeft.appendChild(filterDropdown);
         }
      }
   });
   calendar.render(); // 캘린더 렌더링
   try {
        await loadPets();  // ✅ 반려동물 데이터를 먼저 로드
        await loadEvents(); // ✅ 이후 이벤트 데이터 로드
     } catch (error) {
        console.error("데이터 로딩 중 오류 발생:", error);
     }



   /* ------------------- 검색창 추가 ------------------- */
   const filterContainer = document.getElementById('filterDropdown');

   // 검색창 생성
   const searchContainer = document.createElement('div');
   searchContainer.id = 'calendarSearch';

   const searchInput = document.createElement('input');
   searchInput.type = 'text';
   searchInput.placeholder = '캘린더 일정 검색...';

   const searchButton = document.createElement('button');
   searchButton.textContent = '검색';

   // 검색 컨테이너에 검색 입력창과 버튼 추가
   searchContainer.appendChild(searchInput);
   searchContainer.appendChild(searchButton);

   // 필터링 드롭다운 옆에 삽입
   filterContainer.parentNode.insertBefore(searchContainer, filterContainer.nextSibling);

   // 검색 버튼 클릭 이벤트
   searchButton.onclick = function() {
      const searchQuery = searchInput.value.trim();
      if (!searchQuery) {
         alert('검색어를 입력하세요.');
         return;
      }

      const results = calendar.getEvents().filter(event => {
         return event.title.includes(searchQuery) ||
            (event.extendedProps.description && event.extendedProps.description.includes(searchQuery));
      });

      showSearchResults(results);
   };

   // Enter 키로 검색 실행
   searchInput.onkeypress = function(event) {
      if (event.key === 'Enter') {
         event.preventDefault();
         searchButton.click();
      }
   };
});

/* ---------------- 모달 창 관련 함수 ---------------- */
/* ---------------- 검색창 모달 ----------------------- */
// 검색 결과 모달 생성
const searchModal = document.createElement('div');
searchModal.id = 'searchModal';

const modalBackdrop = document.createElement('div');
modalBackdrop.id = 'modalBackdrop';

searchModal.innerHTML = `
             <h3>검색 결과</h3>
             <ul id="resultsList"></ul>
             <button id="closeSearchModal">닫기</button>
         `;

document.body.appendChild(modalBackdrop);
document.body.appendChild(searchModal);

// 검색 결과 표시
function showSearchResults(results) {
   const resultsList = document.getElementById('resultsList');
   resultsList.innerHTML = ''; // 기존 결과 제거
   if (results.length === 0) {
      resultsList.innerHTML = '<li>검색 결과가 없습니다.</li>';
   } else {
      results.forEach(event => {
         const listItem = document.createElement('li');
         listItem.innerHTML = `
                         <strong>${event.title}</strong><br>
                         <small>${event.start.toLocaleString()} ~ ${event.end ? event.end.toLocaleString() : '종료시간 없음'}</small><br>
                         <span>${event.extendedProps.description || '내용 없음'}</span>
                     `;
         listItem.onclick = function() {
            calendar.changeView('timeGridDay', event.start); // 클릭 시 해당 날짜로 이동
         };
         resultsList.appendChild(listItem);
      });
   }
   searchModal.style.display = 'block';
   modalBackdrop.style.display = 'block';
}

// 검색 모달 닫기
document.getElementById('closeSearchModal').onclick = function() {
   searchModal.style.display = 'none';
   modalBackdrop.style.display = 'none';
};

modalBackdrop.onclick = function() {
   searchModal.style.display = 'none';
   modalBackdrop.style.display = 'none';
};

// 일정 추가 모달 창 표시
async function showEventModal(date) {
    resetEventModal();
    currentEvent = null;

    document.getElementById('eventModal').style.display = 'block';
    document.getElementById('modalBackdrop').style.display = 'block';
    document.getElementById('startDate').value = date.start;
    document.getElementById('endDate').value = date.end;
    document.getElementById('eventTitle').value = '';
    document.getElementById('eventDescription').value = '';
    document.getElementById('eventColor').value = '#F08080';

    // 반려동물 로드
    await loadPets();

    if (selectedPetId) {
        document.getElementById('petSelect').value = selectedPetId;
        console.log(`📌 반려동물 자동 선택: ${selectedPetName} (ID: ${selectedPetId})`);
    }

    document.getElementById('updateEventButton').style.display = 'none';
    document.getElementById('deleteEventButton').style.display = 'none';
    document.getElementById('addEventButton').onclick = function () {
        addEvent();
    };
    document.getElementById('closeModal').onclick = function () {
        closeEventModal();
    };
    document.getElementById('modalBackdrop').onclick = function () {
        closeEventModal();
    };
}

// 하루종일 버튼 동작 추가
document.getElementById('allDayButton').onclick = function() {
   const startDate = document.getElementById('startDate').value;
   if (!startDate) {
      alert('시작일자를 선택해주세요.');
      return;
   }
   // 하루종일로 설정
   document.getElementById('startTime').value = '00:00';
   document.getElementById('endDate').value = startDate; // 같은 날로 설정
   document.getElementById('endTime').value = '23:59';
};

// 일정 수정 모달 창 표시
function showEventModalForEdit(event) {
   currentEvent = event; // 클릭한 이벤트를 현재 이벤트로 설정
   document.getElementById('eventModal').style.display = 'block';
   document.getElementById('modalBackdrop').style.display = 'block';
   const startDate = event.start.toLocaleDateString('en-CA'); // 'YYYY-MM-DD' 형식
   const startTime = event.start.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }); // 'HH:MM' 형식
   const endDate = event.end.toLocaleDateString('en-CA'); // 'YYYY-MM-DD' 형식
   const endTime = event.end.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }); // 'HH:MM' 형식
   const eventColor = event.backgroundColor;
   document.getElementById('eventTitle').value = event.title;
   document.getElementById('startDate').value = startDate;
   document.getElementById('startTime').value = startTime;
   document.getElementById('endDate').value = endDate;
   document.getElementById('endTime').value = endTime;
   document.getElementById('eventDescription').value = event.extendedProps.description || '';
   document.getElementById('eventColor').value = eventColor; // 숨겨진 입력 값
   document.getElementById('eventColorSelect').value = eventColor; // select 요소의 선택 값 업데이트
   document.getElementById('addEventButton').style.display = 'none'; // 추가 버튼 숨기기
   document.getElementById('updateEventButton').style.display = 'block'; // 수정 버튼 보이기
   document.getElementById('deleteEventButton').style.display = 'block'; // 삭제 버튼 보이기      
   document.getElementById('updateEventButton').onclick = function() {
      updateEvent(event);
   };
   document.getElementById('deleteEventButton').onclick = function() {
      deleteEvent();
   };
   document.getElementById('closeModal').onclick = function() {
      closeEventModal();
   };
   document.getElementById('modalBackdrop').onclick = function() {
      closeEventModal();
   };
}

// 모달 창 닫기 함수
function closeEventModal() {
   document.getElementById('eventModal').style.display = 'none';
   document.getElementById('modalBackdrop').style.display = 'none';
   currentEvent = null; // 현재 이벤트 초기화
}

function resetEventModal() {
    // 입력 필드 초기화
    document.getElementById('eventTitle').value = '';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('startTime').value = '';
    document.getElementById('endTime').value = '';
    document.getElementById('eventDescription').value = '';
    document.getElementById('eventColor').value = '#F08080'; // 기본 색상

    // 버튼 상태 초기화
    document.getElementById('addEventButton').style.display = 'block'; // 추가 버튼 보이기
    document.getElementById('updateEventButton').style.display = 'none'; // 수정 버튼 숨기기
    document.getElementById('deleteEventButton').style.display = 'none'; // 삭제 버튼 숨기기
}

/* ---------------- 이벤트 관리 함수 ---------------- */
// 이벤트 추가 
async function addEvent() {
    const title = document.getElementById('eventTitle').value;
    const startDate = document.getElementById('startDate').value;
    const startTime = document.getElementById('startTime').value;
    const endDate = document.getElementById('endDate').value;
    const endTime = document.getElementById('endTime').value;
    const description = document.getElementById('eventDescription').value;
    const color = document.getElementById('eventColor').value;
    const petId = document.getElementById('petSelect').value || null;

    if (!title || !startDate || !startTime || !endDate || !endTime) {
        alert('모든 필드를 채워주세요.');
        return;
    }

    const start = `${startDate}T${startTime}`;
    const end = `${endDate}T${endTime}`;

    if (new Date(end) <= new Date(start)) {
        alert('종료 시간은 시작 시간보다 이후여야 합니다.');
        return;
    }

    //반려동물 리스트
    await loadPets();

    const petImage = petId ? petImages[petId] : null;

    const eventData = {
        cd_title: title,
        eventDateStart: start,
        eventDateEnd: end,
        cd_description: description,
        cd_color: color,
        petId: petId ? Number(petId) : null
    };

    fetch('/api/events', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(eventData)
    })
    .then(response => response.json())  
    .then(data => {

        //반려동물 사진
        calendar.addEvent({
            id: data.calendar_id,
            title,
            start,
            end,
            backgroundColor: color,
            extendedProps: { description, petId, petImage }
        });

        closeEventModal();
    })
    .catch(error => console.error('이벤트 추가 실패:', error));
}

 async function updateEvent(event) {
     const title = document.getElementById('eventTitle').value;
     const startDate = document.getElementById('startDate').value;
     const startTime = document.getElementById('startTime').value;
     const endDate = document.getElementById('endDate').value;
     const endTime = document.getElementById('endTime').value;
     const description = document.getElementById('eventDescription').value;
     const color = document.getElementById('eventColor').value;
     const petId = document.getElementById('petSelect').value || null;

     if (!title || !startDate || !startTime || !endDate || !endTime) {
         alert('모든 필드를 채워주세요.');
         return;
     }

     const start = `${startDate}T${startTime}`;
     const end = `${endDate}T${endTime}`;

     if (new Date(end) <= new Date(start)) {
         alert('종료 시간은 시작 시간보다 이후여야 합니다.');
         return;
     }

     const eventData = {
         cd_title: title,
         eventDateStart: start,
         eventDateEnd: end,
         cd_description: description,
         cd_color: color,
         petId: petId ? Number(petId) : null
     };

     fetch(`/api/events/${event.id}`, {
         method: 'PUT',
         headers: { 'Content-Type': 'application/json' },
         body: JSON.stringify(eventData)
     })
     .then(response => response.json())
     .then(async data => {
         console.log('이벤트 수정 성공:', data);

         //새로운 반려동물 사진 적용
         let newPetImage = petId ? `http://localhost:9000/pets/${petId}/photo` : null;
         petImages[petId] = newPetImage;

         //기존 이벤트 삭제 후 다시 추가
         event.remove();
         calendar.addEvent({
             id: data.calendar_id,
             title: title,
             start: start,
             end: end,
             backgroundColor: color,
             extendedProps: {
                 description: description,
                 petId: petId,
                 petImage: newPetImage
             }
         });

         closeEventModal();
     })
     .catch(error => console.error('이벤트 수정 실패:', error));
 }

 // 이벤트 삭제 함수
 function deleteEvent() {
    if (!currentEvent) return;

    fetch(`/api/events/${currentEvent.id}`, {
       method: 'DELETE',
    })
    .then(response => {
       if (response.ok) {
          currentEvent.remove();
          closeEventModal();
       } else {
          alert('이벤트 삭제에 실패했습니다.');
       }
    })
    .catch(error => console.error('이벤트 삭제 실패:', error));
 }
/* ------------------- 필터링 기능 ------------------- */
// 색상별 이벤트 필터링
function filterByColor(selectedColor) {
   calendar.getEvents().forEach(event => event.remove());
   if (selectedColor === 'all') {
      allEvents.forEach(event => calendar.addEvent(event));
   } else {
      allEvents
         .filter(event => event.backgroundColor === selectedColor)
         .forEach(event => calendar.addEvent(event));
   }
}

/* ---------------- 반려동물 목록 불러오기 ---------------- */
async function loadPets() {
    try {
        const response = await fetch('/pets');
        if (!response.ok) throw new Error('반려동물 데이터를 불러올 수 없음');
        
        const pets = await response.json();
        const petSelect = document.getElementById('petSelect');

        petSelect.innerHTML = '<option value="">반려동물 없음</option>';
        pets.forEach(pet => {
            const option = document.createElement('option');
            option.value = pet.petId;
            option.textContent = pet.petName;
            petSelect.appendChild(option);

            // 반려동물 ID - 사진 URL 매핑 저장 
            petImages[pet.petId] = `http://localhost:9000/pets/${pet.petId}/photo`;  
        });
        console.log(" 반려동물 목록 로드 완료:", pets);
    } catch (error) {
        console.error('반려동물 목록 로드 실패:', error);
    }
}

function updateEventUI(event) {
    const eventEl = document.querySelector(`.fc-event[data-event-id="${event.id}"]`);
    if (!eventEl) return;

    const petImage = event.extendedProps.petImage;
    const eventColor = event.extendedProps.cd_color || "#000"; // 기본 검은색

   // 반려동물 이미지가 없을 경우, 다시 로드하여 업데이트
       if (!petImage && petId && petImages[petId]) {
           petImage = petImages[petId];
           event.setProp('extendedProps', { ...event.extendedProps, petImage });
       }

    //기존의 중복 요소 삭제
    let existingContainer = eventEl.querySelector('.event-title-container');
    if (existingContainer) {
        existingContainer.remove();
    }
   
    // UI 업데이트
    const titleContainer = document.createElement('div');
    titleContainer.classList.add('event-title-container');
    titleContainer.innerHTML = `
        ${petImage ? `<img src="${petImage}" class="event-pet-icon">` : ''}
    `;

    eventEl.appendChild(titleContainer);
}


/* ------------------- 서버 데이터 로드 ------------------- */
async function loadEvents() {
    try {
        const response = await fetch('/api/events');
        if (!response.ok) throw new Error('이벤트 데이터를 불러올 수 없음');
        
        const events = await response.json();
        events.forEach(event => {
            const petId = event.pet?.petId;  // 이벤트에 연결된 반려동물 ID
            const petImage = petId ? petImages[petId] : null; //ID 기반 프로필 이미지 찾기

            // FullCalendar에 이벤트 추가
            calendar.addEvent({
                id: event.calendar_id, 
                title: event.cd_title, 
                start: event.eventDateStart,
                end: event.eventDateEnd,
                backgroundColor: event.cd_color,
                extendedProps: {
                    description: event.cd_description, 
                    petId: petId,
                    petImage: petImage //이미지 URL을 `extendedProps`에 저장
                }
            });
        });

        console.log("이벤트 목록 로드 완료:", events);
    } catch (error) {
        console.error('❌ 이벤트 목록 로드 실패:', error);
    }
}

/* ------------------- 유틸리티 함수 ------------------- */
// 색상 선택 시 숨겨진 입력값 업데이트
window.selectColor = function(color) {
   document.getElementById('eventColor').value = color;
};

/*------------ 상단 장소추천 버튼 관련 코드---------------- */
//페이지 로드 시 사용자 위치를 가져와 장소추천 버튼에 쿼리 추가
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

let calendar, currentEvent = null;
let allEvents = []; // 모든 이벤트를 저장하는 배열

document.addEventListener('DOMContentLoaded', function() {
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
      // 날짜 클릭 시 이벤트 (일정 추가 모달 표시)
      dateClick: function(info) {
      resetEventModal(); // 모달 상태 초기화
         const start = info.dateStr; // 시작 시간
         const end = info.dateStr;   // 종료 시간
         showEventModal({ start, end }); // 모달 창 표시
         
      },
      // 이벤트 클릭 시 이벤트 (수정 모달 표시)
      eventClick: function(info) {
         currentEvent = info.event; // 클릭한 이벤트 저장
         showEventModalForEdit(info.event);  // 수정용 모달 창 표시
      },
      displayEventTime: false, // 캘린더에 이벤트 시간 숨기고 제목만 보여주는 기능
      // 이벤트 렌더링 후 실행 (필터링 기능을 위해 모든 이벤트 저장)
      eventDidMount: function(info) {
         if (!allEvents.some(event => event.id === info.event.id)) {
            allEvents.push(info.event); // 이벤트를 allEvents 배열에 저장
         }
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
   loadEvents();
   loadPets(); // 반려동물 목록 불러오기



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
function showEventModal(date) {
   resetEventModal(); // 모달 상태 초기화
   currentEvent = null; // 클릭한 이벤트를 현재 이벤트로 설정
   loadPets();
   document.getElementById('eventModal').style.display = 'block';
   document.getElementById('modalBackdrop').style.display = 'block';
   document.getElementById('startDate').value = date.start;
   document.getElementById('endDate').value = date.end;
   document.getElementById('eventTitle').value = '';
   document.getElementById('eventDescription').value = '';
   document.getElementById('eventColor').value = '#F08080'; // 기본 색상
   document.getElementById('updateEventButton').style.display = 'none'; // 수정 버튼 숨기기
   document.getElementById('deleteEventButton').style.display = 'none'; // 삭제 버튼 숨기기
   document.getElementById('addEventButton').onclick = function() {
      addEvent();
   };
   document.getElementById('closeModal').onclick = function() {
      closeEventModal();
   };
   document.getElementById('modalBackdrop').onclick = function() {
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
/* ---------------- 이벤트 관리 함수 ---------------- */
// ✅ 이벤트 추가 함수 (반려동물 ID 정상 저장)
 function addEvent() {
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
       petId: petId ? Number(petId) : null // 🚀 petId를 숫자로 변환
    };

	console.log("📤 서버로 전송할 데이터:", JSON.stringify(eventData));
	
    fetch('/api/events', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify(eventData)
    })
    .then(response => response.json())  // 🚀 JSON 응답 변환 추가
    .then(data => {
       console.log('✅ 이벤트 추가 성공:', data);
       calendar.addEvent({
          id: data.calendar_id,
          title,
          start,
          end,
          backgroundColor: color,
          extendedProps: { description, petId }
       });
       closeEventModal();
    })
    .catch(error => console.error('❌ 이벤트 추가 실패:', error));
 }

 // ✅ 이벤트 수정 함수 (반려동물 ID 저장 문제 해결)
 function updateEvent(event) {
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
       petId: petId ? Number(petId) : null // 🚀 petId를 숫자로 변환하여 전달
    };

    fetch(`/api/events/${event.id}`, {
       method: 'PUT',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify(eventData)
    })
    .then(response => response.json())  // 🚀 JSON 응답 변환 추가
    .then(data => {
       console.log('✅ 이벤트 수정 성공:', data);
       event.setProp('title', title);
       event.setStart(start);
       event.setEnd(end);
       event.setProp('backgroundColor', color);
       event.setExtendedProp('description', description);
       event.setExtendedProp('petId', petId);
       closeEventModal();
    })
    .catch(error => console.error('❌ 이벤트 수정 실패:', error));
 }

 // ✅ 이벤트 삭제 함수 (삭제 오류 해결)
 function deleteEvent() {
    if (!currentEvent) return;

    fetch(`/api/events/${currentEvent.id}`, {
       method: 'DELETE',
    })
    .then(response => {
       if (response.ok) {
          console.log("✅ 이벤트 삭제 성공");
          currentEvent.remove();
          closeEventModal();
       } else {
          console.error('❌ 이벤트 삭제 실패:', response.statusText);
          alert('이벤트 삭제에 실패했습니다.');
       }
    })
    .catch(error => console.error('❌ 이벤트 삭제 실패:', error));
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
        });

        console.log("✅ 반려동물 목록 로드 완료:", pets);
    } catch (error) {
        console.error('❌ 반려동물 목록 로드 실패:', error);
    }
}


/* ------------------- 서버 데이터 로드 ------------------- */
// 서버에서 이벤트 데이터 로드
async function loadEvents() {
   const response = await fetch('/api/events');
   if (response.ok) {
      const events = await response.json();
      events.forEach(event => {
         calendar.addEvent({
            id: event.calendar_id, // 서버에서 반환된 이벤트 ID
            title: event.cd_title, // 일정 제목
            start: event.eventDateStart, // 시작 날짜/시간
            end: event.eventDateEnd, // 종료 날짜/시간
            backgroundColor: event.cd_color, // 이벤트 색상
            extendedProps: {
                description: event.cd_description, petId: event.pet?.petId }
         });
      });
   }
}
/* ------------------- 유틸리티 함수 ------------------- */
// 색상 선택 시 숨겨진 입력값 업데이트
window.selectColor = function(color) {
   document.getElementById('eventColor').value = color;
};


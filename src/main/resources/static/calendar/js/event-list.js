document.addEventListener("DOMContentLoaded", () => {
    const eventList = document.getElementById("event-list");
    const prevDayButton = document.getElementById("prev-day");
    const nextDayButton = document.getElementById("next-day");
    const currentDateDisplay = document.getElementById("current-date");

    let currentDate = new Date(); // 기본값: 오늘 날짜

    // 날짜를 'YYYY-MM-DD' 형식으로 변환
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    }

    // 시간 형식 변환 ('오후 5:00' 형식)
    function formatTime(date) {
        const hours = date.getHours();
        const minutes = String(date.getMinutes()).padStart(2, "0");
        const ampm = hours >= 12 ? "오후" : "오전";
        const formattedHours = hours % 12 === 0 ? 12 : hours % 12; // 12시간제 변환
        return `${ampm} ${formattedHours}:${minutes}`;
    }

    // 날짜를 화면에 읽기 쉽게 표시 ('1월 20일 (월)' 형식)
    function formatReadableDate(date) {
        const options = { month: "long", day: "numeric", weekday: "short" };
        return date.toLocaleDateString("ko-KR", options);
    }

    // 아이콘을 색상에 따라 결정
    function getColorIcon(color) {
        const iconMap = {
            "#F08080": "🩺", // 병원예약
            "#FF9900": "💊", // 약시간
            "#F3E591": "🐾", // 산책
            "#98FB98": "🏫", // 유치원
            "#87CEFA": "⭐", // 중요
            "#8080FF": "📌", // 기타
            default: "📅", // 기본 아이콘
        };
        return iconMap[color] || iconMap.default; // 색상에 해당하는 아이콘 반환
    }

    // 일정 가져오기 및 특정 날짜로 필터링
    async function fetchAndDisplayEvents() {
        try {
            const response = await fetch(`/api/events`);
            const events = await response.json();

            // 현재 날짜를 기준으로 일정 필터링 (시작일과 종료일 사이에 포함되는 일정)
            const formattedDate = formatDate(currentDate);
            const filteredEvents = events.filter((event) => {
                const eventStartDate = new Date(event.eventDateStart);
                const eventEndDate = new Date(event.eventDateEnd);

                // 현재 날짜가 일정 시작일과 종료일 사이에 포함되는지 확인
                return eventStartDate <= currentDate && currentDate <= eventEndDate;
            });

            // 시간순으로 정렬
            filteredEvents.sort((a, b) => new Date(a.eventDateStart) - new Date(b.eventDateStart));

            // 일정 표시
            displayEvents(filteredEvents);
        } catch (error) {
            eventList.innerHTML = "<li>일정을 불러오는 데 실패했습니다.</li>";
        }
    }

    // 일정 목록을 화면에 표시
    function displayEvents(events) {
        eventList.innerHTML = ""; // 기존 내용 초기화

        if (events.length === 0) {
            eventList.innerHTML = "<li>등록된 일정이 없습니다.</li>";
        } else {
            events.forEach((event) => {
                const eventDateStart = new Date(event.eventDateStart);
                const eventDateEnd = new Date(event.eventDateEnd);

                const listItem = document.createElement("li");

                // 색상에 따른 아이콘 추가
                const icon = getColorIcon(event.cd_color);

                listItem.innerHTML = `
                    <div class="event-item">
                        <span class="event-icon">${icon}</span> <!-- 아이콘 -->
                        <div class="event-details">
                            <strong>${event.cd_title}</strong> <!-- 제목 -->
                            <p>${formatTime(eventDateStart)} ~ ${formatTime(eventDateEnd)}</p> <!-- 시간 -->
                            <p>${event.cd_description}</p> <!-- 내용 -->
                        </div>
                    </div>
                `;
                eventList.appendChild(listItem);
            });
        }
    }

    // UI에 현재 날짜 표시
    function updateDateDisplay() {
        currentDateDisplay.textContent = formatReadableDate(currentDate);
    }

    // 이전 날짜로 이동
    prevDayButton.addEventListener("click", () => {
        currentDate.setDate(currentDate.getDate() - 1); // 하루 전으로 이동
        updateDateDisplay(); // 화면에 날짜 업데이트
        fetchAndDisplayEvents(); // 새로운 일정 가져오기
    });

    // 다음 날짜로 이동
    nextDayButton.addEventListener("click", () => {
        currentDate.setDate(currentDate.getDate() + 1); // 하루 후로 이동
        updateDateDisplay(); // 화면에 날짜 업데이트
        fetchAndDisplayEvents(); // 새로운 일정 가져오기
    });

    // 초기화: 기본 날짜로 일정 표시
    updateDateDisplay();
    fetchAndDisplayEvents();
});

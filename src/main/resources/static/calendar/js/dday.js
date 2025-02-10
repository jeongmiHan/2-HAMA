document.addEventListener("DOMContentLoaded", () => {
    const eventSelectContainer = document.getElementById("event-select-container");
    const customDateContainer = document.getElementById("custom-date-container");
    const eventSelect = document.getElementById("event-select");
    const customDateInput = document.getElementById("custom-date-input");
    const ddayDisplay = document.getElementById("d-day-display");
    const selectEventBtn = document.getElementById("select-event-btn");
    const customDateBtn = document.getElementById("custom-date-btn");

    let isEventSelectActive = false; // 선택 창 상태를 추적

    // 기본 D-Day 메시지 설정
    function setDefaultDdayMessage() {
        ddayDisplay.innerHTML = `<p class="default-dday">D-day를 설정해보세요!</p>`;
        sessionStorage.removeItem("ddayData"); // 🔹 sessionStorage에서 D-Day 데이터 삭제
    }

    // 세션에서 저장된 D-DAY 데이터를 불러옴
    const storedDdayData = sessionStorage.getItem("ddayData");
    if (storedDdayData) {
        const ddayData = JSON.parse(storedDdayData);
        displayDday(ddayData);
    } else {
        setDefaultDdayMessage(); // 저장된 데이터가 없을 경우 기본 메시지 표시
    }

    // 이벤트 목록 가져오기
    fetch("/api/events")
        .then((response) => response.json())
        .then((events) => {
            if (events.length === 0) {
                setDefaultDdayMessage(); // 일정이 없을 경우 기본 메시지 표시
                return;
            }

            // 🔹 "D-Day를 선택하세요" 기본 옵션 추가
            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = "D-Day를 선택하세요";
            eventSelect.appendChild(defaultOption);

            events.forEach((event) => {
                const option = document.createElement("option");
                option.value = event.calendar_id;
                option.textContent = `${event.cd_title} (${new Date(event.eventDateStart).toLocaleDateString()})`;
                eventSelect.appendChild(option);
            });
        })
        .catch((error) => {
            console.error("이벤트 목록 로드 실패:", error);
            setDefaultDdayMessage();
        });

    // D-day 계산 함수
    function calculateDday(targetDate) {
        const today = new Date();
        const diffTime = targetDate - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const d = "D";
        const sign = diffDays === 0 ? "-" : diffDays > 0 ? "-" : "+";
        const days = diffDays === 0 ? "Day" : Math.abs(diffDays);

        return { d, sign, days };
    }

    // D-day 표시 함수
    function displayDday({ d, sign, days }) {
        if (!d || !sign || !days) {
            setDefaultDdayMessage();
            return;
        }
        ddayDisplay.innerHTML = `
            <div class="d-day-box">${d}</div>
            <div class="d-day-box">${sign}</div>
            <div class="d-day-box">${days}</div>
        `;
    }

    // + 버튼 클릭: 이벤트 선택 창 활성화/비활성화
    selectEventBtn.addEventListener("click", () => {
        isEventSelectActive = !isEventSelectActive; // 상태 토글
        eventSelectContainer.style.display = isEventSelectActive ? "block" : "none";
        customDateContainer.style.display = "none"; // 날짜 입력 창 비활성화
    });

    // * 버튼 클릭: 날짜 입력 창 활성화
    customDateBtn.addEventListener("click", () => {
        eventSelectContainer.style.display = "none";
        customDateContainer.style.display = "block";
        isEventSelectActive = false; // 선택 창 비활성화
    });

    // 이벤트 선택 후 D-day 계산 및 선택 창 숨기기
    eventSelect.addEventListener("change", () => {
        const eventId = eventSelect.value;

        if (!eventId) {
            setDefaultDdayMessage(); // 🔹 "D-Day를 선택하세요" 옵션 선택 시 초기화
            return;
        }

        fetch(`/api/events/dday?eventId=${eventId}`)
            .then((response) => response.json())
            .then((data) => {
                displayDday(data);
                sessionStorage.setItem("ddayData", JSON.stringify(data)); // 세션에 D-DAY 데이터 저장
                eventSelectContainer.style.display = "none"; // 선택 창 숨김
                isEventSelectActive = false; // 상태 업데이트
            })
            .catch((error) => {
                console.error("D-day 계산 실패:", error);
                setDefaultDdayMessage();
            });
    });

    // 날짜 입력 시 바로 D-day 계산
    customDateInput.addEventListener("change", () => {
        const selectedDate = new Date(customDateInput.value);
        if (!isNaN(selectedDate)) {
            const ddayData = calculateDday(selectedDate);
            displayDday(ddayData);
            sessionStorage.setItem("ddayData", JSON.stringify(ddayData)); // 세션에 D-DAY 데이터 저장
            customDateContainer.style.display = "none"; // 날짜 입력 창 숨김
        } else {
            alert("초기화합니다.");
            setDefaultDdayMessage();
        }
    });
});

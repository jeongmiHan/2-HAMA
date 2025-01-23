// 모달 열기
function openNotificationModal() {
    const modal = document.getElementById('noti_modal');
    modal.style.display = 'block';
    fetchNotifications(); // 알림 데이터를 가져옵니다.
}

// 모달 닫기
function closeNotificationModal() {
    const modal = document.getElementById('noti_modal');
    modal.style.display = 'none';
}

// 알림 가져오기
async function fetchNotifications() {
    try {
        const response = await fetch('/notifications/unread');
        const notifications = await response.json();
        updateNotificationUI(notifications);
    } catch (error) {
        console.error('Error fetching notifications:', error);
    }
}

// 알림 UI 업데이트
function updateNotificationUI(notifications) {
    const notificationList = document.getElementById('notification-list');
    const notificationCount = document.getElementById('notification-count');

    // 기존 알림 비우기
    notificationList.innerHTML = '';

    notifications.forEach(notification => {
        const listItem = document.createElement('li');
        listItem.textContent = notification.content;

        const readButton = document.createElement('button');
        readButton.textContent = '읽음';
        readButton.onclick = () => markAsRead(notification.id);

        listItem.appendChild(readButton);
        notificationList.appendChild(listItem);
    });

    // 알림 개수 표시
    notificationCount.textContent = notifications.length;
    notificationCount.style.display = notifications.length > 0 ? 'inline-block' : 'none';
}

// 알림 읽음 처리
async function markAsRead(notificationId) {
    try {
        const response = await fetch(`/notifications/${notificationId}/read`, {
            method: 'PUT',
        });

        if (response.ok) {
            console.log(`알림 ${notificationId} 읽음 처리 완료`);
            fetchNotifications(); // 알림 리스트 갱신
        } else {
            console.error('읽음 처리 실패:', response.statusText);
        }
    } catch (error) {
        console.error('Error marking notification as read:', error);
    }
}

// 주기적으로 알림 업데이트 10초동안
setInterval(fetchNotifications, 10000);
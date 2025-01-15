document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.querySelector('form');
    const userIdField = document.querySelector('#userId');
    const passwordField = document.querySelector('#password');
    const submitButton = loginForm.querySelector('button[type="submit"]');

	loginForm.addEventListener('submit', async function (event) {
	        let isValid = true;

	        // 아이디 필드 확인
	        if (userIdField.value.trim() === '') {
	            showError(userIdField, '아이디를 입력해주세요.');
	            isValid = false;
	        } else {
	            clearError(userIdField);
	        }

	        // 비밀번호 필드 확인
	        if (passwordField.value.trim() === '') {
	            showError(passwordField, '비밀번호를 입력해주세요.');
	            isValid = false;
	        } else {
	            clearError(passwordField);
	        }

	        // 폼 제출 방지 (유효하지 않을 때만)
	        if (!isValid) {
	            event.preventDefault();
	            return;
	        }

	        // 버튼 비활성화 및 상태 표시
	        submitButton.disabled = true;
	        submitButton.textContent = '로그인 중...';

	        // 서버로 아이디와 비밀번호 검증 요청
	        event.preventDefault(); // 기본 폼 제출 방지

	      try {
	          console.log("Fetching data from the server...");

	          const response = await fetch('/api/user/validate-password', {
	              method: 'POST',
	              headers: {
	                  'Content-Type': 'application/json'
	              },
	              body: JSON.stringify({
	                  userId: userIdField.value,
	                  password: passwordField.value
	              })
	          });

	          console.log("Server response received:", response);

	          if (!response.ok) {
	              throw new Error(`서버 응답 오류: ${response.status} ${response.statusText}`);
	          }

	          const result = await response.json();
	          console.log("Parsed JSON result:", result);

	          if (result.success === true) {
	              console.log("Password validated successfully. Submitting form...");
	              loginForm.submit(); // 비밀번호 일치 시 폼 제출
	          } else {
	              alert('비밀번호가 틀렸습니다.');
	              submitButton.disabled = false;
	              submitButton.textContent = '로그인'; // 텍스트 복원
	          }
	      } catch (error) {
	          console.error("Error occurred during fetch request:", error);
	          alert('서버 요청 중 오류가 발생했습니다. 다시 시도해주세요.');
	          submitButton.disabled = false;
	          submitButton.textContent = '로그인'; // 텍스트 복원
	      }
	});

    function showError(field, message) {
        const errorSpan = field.nextElementSibling;
        if (errorSpan) {
            errorSpan.textContent = message;
            errorSpan.style.color = 'red';
        }
    }

    function clearError(field) {
        const errorSpan = field.nextElementSibling;
        if (errorSpan) {
            errorSpan.textContent = '';
        }
    }
});

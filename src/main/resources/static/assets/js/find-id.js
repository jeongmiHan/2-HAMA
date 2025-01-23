document.addEventListener('DOMContentLoaded', function () {
    let isEmailVerified = false; // 이메일 인증 상태

    const emailInput = document.getElementById('email');
    const emailError = document.getElementById('emailError');
    const emailVerificationButton = document.getElementById('emailVerificationButton');
    const verificationCodeInput = document.getElementById('verificationCode');
    const verificationCodeButton = document.getElementById('verificationCodeButton');
    const verificationCodeError = document.getElementById('verificationCodeError');
    const findIdButton = document.getElementById('findIdButton');
    const resultMessage = document.getElementById('result');

    // 이메일 인증 요청
    emailVerificationButton.addEventListener('click', function () {
        const email = emailInput.value.trim();
        
        if (!email) {
            emailError.innerText = '이메일을 입력해주세요.';
            return;
        }

        emailError.innerText = '';
        emailVerificationButton.disabled = true;
        emailVerificationButton.innerText = '인증 요청 중...';

        fetch(`/api/email/verify-email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email }),
        })
        .then(response => {
            if (!response.ok) throw new Error('이메일 전송 실패');
            return response.json();
        })
        .then(() => {
            emailError.innerText = '인증 코드가 이메일로 전송되었습니다.';
            verificationCodeInput.disabled = false;
            verificationCodeButton.disabled = false;
        })
        .catch(error => {
            emailError.innerText = error.message;
        })
        .finally(() => {
            emailVerificationButton.disabled = false;
            emailVerificationButton.innerText = '인증 요청';
        });
    });

    // 인증 코드 확인
    verificationCodeButton.addEventListener('click', function () {
        const email = emailInput.value.trim();
        const code = verificationCodeInput.value.trim();

        if (!code) {
            verificationCodeError.innerText = '인증 코드를 입력해주세요.';
            return;
        }

        verificationCodeError.innerText = '';
        verificationCodeButton.disabled = true;  // Disable the button as soon as clicked

        fetch(`/api/email/verify-code?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`, {
            method: 'POST',
        })
        .then(response => {
            if (!response.ok) {
                verificationCodeError.innerText = '인증 코드가 유효하지 않습니다. 다시 입력해주세요.';
                verificationCodeButton.disabled = false;  // Re-enable the button if the code is incorrect
                throw new Error('인증 코드 검증 실패');
            }
            return response.json();
        })
        .then(() => {
            verificationCodeError.innerText = '인증 완료!';
            isEmailVerified = true;
            findIdButton.disabled = false;  // Enable the "Find ID" button after successful verification
            verificationCodeButton.disabled = true;  // Disable the "Verify Code" button after success
            verificationCodeInput.disabled = true;  // Disable the verification code input after success
        })
        .catch(() => {
            // Handle authentication failure
            console.error("인증 실패");
        });
    });

    // 아이디 찾기 폼 제출
    document.getElementById('findIdForm').addEventListener('submit', function (event) {
        event.preventDefault();
        
        if (!isEmailVerified) {
            resultMessage.innerText = '이메일 인증을 완료해주세요.';
            resultMessage.classList.remove('hidden');  // 결과 메시지를 표시
            resultMessage.classList.add('visible');   // 결과 메시지를 보이게 설정
            return;
        }

        const email = emailInput.value.trim();
        fetch(`/api/user/find-id`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email }),
        })
        .then(response => {
            if (!response.ok) throw new Error('아이디 찾기 요청 실패');
            return response.json();
        })
        .then(data => {
            if (data.userIds && data.userIds.length > 0) {
                // 아이디 개수 및 리스트 출력
                const userIdList = data.userIds.map(id => `<li class="user-id-item">${id}</li>`).join('');
                resultMessage.innerHTML = `
                    <h3>아이디 찾기 결과:</h3>
                    <p>${data.userIds.length}개의 아이디를 찾았습니다.</p>
                    <ul>${userIdList}</ul>
                `;
            } else {
                resultMessage.innerText = '해당 이메일로 가입된 아이디가 없습니다.';
            }
            resultMessage.classList.remove('hidden');  // 결과 메시지를 표시
            resultMessage.classList.add('visible');   // 결과 메시지를 보이게 설정
        })
        .catch(error => {
            resultMessage.innerText = error.message;
            resultMessage.classList.remove('hidden');  // 결과 메시지를 표시
            resultMessage.classList.add('visible');   // 결과 메시지를 보이게 설정
        });

        findIdButton.disabled = true; // 아이디 찾기 버튼 비활성화
    });
});

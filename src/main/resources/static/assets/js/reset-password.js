document.addEventListener('DOMContentLoaded', function () {
    let isEmailVerified = false; // 이메일 인증 상태

    const emailInput = document.getElementById('email');
    const emailError = document.getElementById('emailError');
    const emailVerificationButton = document.getElementById('emailVerificationButton');
    const verificationCodeInput = document.getElementById('verificationCode');
    const verificationCodeButton = document.getElementById('verificationCodeButton');
    const verificationCodeError = document.getElementById('verificationCodeError');
    const resetPasswordButton = document.getElementById('resetPasswordButton');
    const resultMessage = document.getElementById('result');

    // 이메일 인증 요청
    emailVerificationButton.addEventListener('click', function () {
        const email = emailInput.value.trim();

        if (!email) {
            emailError.innerText = '이메일을 입력해주세요.';
            return;
        }

        isEmailVerified = false;
        verificationCodeInput.value = '';
        verificationCodeInput.disabled = true;
        verificationCodeButton.disabled = true;
        verificationCodeError.innerText = '';
        resetPasswordButton.disabled = true;

        emailError.innerText = '';
        emailVerificationButton.disabled = true;
        emailVerificationButton.innerText = '인증 메일 발송 중...';

        fetch('/api/email/verify-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email }),
        })
            .then(response => response.json())
            .then(() => {
                emailError.innerText = '인증 메일이 발송되었습니다.';
                verificationCodeInput.disabled = false;
                verificationCodeButton.disabled = false;
            })
            .catch(() => {
                emailError.innerText = '이메일 발송에 실패했습니다.';
            })
            .finally(() => {
                emailVerificationButton.disabled = false;
                emailVerificationButton.innerText = '인증 요청';
            });
    });

    // 인증 코드 검증
    verificationCodeButton.addEventListener('click', function () {
        const email = emailInput.value.trim();
        const verificationCode = verificationCodeInput.value.trim();

        if (!verificationCode) {
            verificationCodeError.innerText = '인증 코드를 입력해주세요.';
            return;
        }

        fetch(`/api/email/verify-code?email=${encodeURIComponent(email)}&code=${encodeURIComponent(verificationCode)}`, {
            method: 'POST',
        })
            .then(response => {
                if (!response.ok) {
                    verificationCodeError.innerText = '인증 코드가 유효하지 않습니다. 다시 입력해주세요.';
                    isEmailVerified = false; // 인증 실패 시 이메일 인증 상태를 false로 설정
                    verificationCodeButton.disabled = false; // 코드 확인 버튼을 다시 활성화
                    throw new Error('인증 코드 검증 실패');
                }
                return response.json();
            })
            .then(() => {
                verificationCodeError.innerText = '인증 완료!';
                isEmailVerified = true;
                verificationCodeButton.disabled = true; // 인증 성공 시 버튼 비활성화
                verificationCodeInput.disabled = true; // 인증 후 입력 필드 비활성화
                resetPasswordButton.disabled = false; // 비밀번호 찾기 버튼 활성화
            })
            .catch(() => {
                // 인증 실패 시 처리
                console.error("인증 실패");
            });
    });

    // 비밀번호 찾기 폼 제출
    document.getElementById('resetPasswordForm').addEventListener('submit', function (event) {
        event.preventDefault();

        const userId = document.getElementById('userId').value.trim();
        const email = emailInput.value.trim();

        if (!isEmailVerified) {
            alert('이메일 인증을 완료해주세요.');
            return;
        }

        resetPasswordButton.disabled = true;

        fetch('/api/user/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId, email }),
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    resultMessage.innerHTML = `<strong>임시 비밀번호:</strong> ${data.temporaryPassword}`;
                    resultMessage.classList.remove('hidden');
                    resultMessage.classList.add('visible');
                } else {
                    alert(data.message || '가입된 정보가 일치하지 않습니다.');
                    resetPasswordButton.disabled = false;
                }
            })
            .catch(() => {
                alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
                resetPasswordButton.disabled = false;
            });
    });
});

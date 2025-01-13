document.addEventListener('DOMContentLoaded', function () {
    const currentDate = new Date().toISOString().split('T')[0];
    document.getElementById('joinDate').value = currentDate;

    let isEmailVerified = false; // 이메일 인증 상태
    let isUserIdAvailable = false; // 아이디 중복 확인 상태
    let isNameAvailable = false; // 닉네임 중복 확인 상태

    document.getElementById('registerForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        let isValid = true;

        const userId = document.getElementById('userId').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const name = document.getElementById('name').value;
        const email = document.getElementById('email').value;

        // 아이디 중복 확인 (폼 제출 시 다시 확인)
        await fetch(`/api/user/check-id?userId=${encodeURIComponent(userId)}`, {
            method: 'GET',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('아이디 중복 확인 요청 실패');
                }
                return response.json();
            })
            .then(exists => {
                if (exists) {
                    document.getElementById('userIdError').innerText = '이미 사용 중인 아이디입니다.';
                    alert('이미 사용 중인 아이디입니다.');
                    isUserIdAvailable = false;
                    isValid = false;
                } else {
                    document.getElementById('userIdError').innerText = '';
                    isUserIdAvailable = true;
                }
            })
            .catch(error => {
                document.getElementById('userIdError').innerText = error.message;
                alert(error.message);
                isUserIdAvailable = false;
                isValid = false;
            });

        // 닉네임 중복 확인 (폼 제출 시 다시 확인)
        await fetch(`/api/user/check-name?name=${encodeURIComponent(name)}`, {
            method: 'GET',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('닉네임 중복 확인 요청 실패');
                }
                return response.json();
            })
            .then(exists => {
                if (exists) {
                    document.getElementById('nameError').innerText = '이미 사용 중인 닉네임입니다.';
                    alert('이미 사용 중인 닉네임입니다.');
                    isNameAvailable = false;
                    isValid = false;
                } else {
                    document.getElementById('nameError').innerText = '';
                    isNameAvailable = true;
                }
            })
            .catch(error => {
                document.getElementById('nameError').innerText = error.message;
                alert(error.message);
                isNameAvailable = false;
                isValid = false;
            });

        // 비밀번호, 이름, 이메일 인증 상태 등 다른 유효성 검사
        if (userId.length < 4 || userId.length > 20) {
            document.getElementById('userIdError').innerText = '아이디는 4자 이상 20자 이하로 입력해주세요.';
            isValid = false;
        }

        if (password.length < 8 || password.length > 20) {
            document.getElementById('passwordError').innerText = '비밀번호는 8자 이상 20자 이하로 입력해주세요.';
            isValid = false;
        }

        if (password !== confirmPassword) {
            document.getElementById('confirmPasswordError').innerText = '비밀번호가 일치하지 않습니다.';
            isValid = false;
        }

        if (name.length < 2 || name.length > 15) {
            document.getElementById('nameError').innerText = '닉네임은 2자 이상 15자 이하로 입력해주세요.';
            isValid = false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            document.getElementById('emailError').innerText = '올바른 이메일 주소를 입력해주세요.';
            isValid = false;
        }

        if (!isEmailVerified) {
            document.getElementById('verificationCodeError').innerText = '이메일 인증을 완료해주세요.';
            isValid = false;
        }

        // 유효성 검사를 모두 통과하면 폼 제출
        if (isValid) {
            alert('회원가입이 완료되었습니다!');
            event.target.submit();
        }
    });

    document.getElementById('checkUserIdButton').addEventListener('click', function () {
        const userId = document.getElementById('userId').value;

        if (!userId) {
            document.getElementById('userIdError').innerText = '아이디를 입력해주세요.';
            alert('아이디를 입력해주세요.');
            return;
        }

        fetch(`/api/user/check-id?userId=${encodeURIComponent(userId)}`, {
            method: 'GET',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('아이디 중복 확인 요청 실패');
                }
                return response.json();
            })
            .then(exists => {
                if (exists) {
                    document.getElementById('userIdError').innerText = '이미 사용 중인 아이디입니다.';
                    alert('이미 사용 중인 아이디입니다.');
                    isUserIdAvailable = false;
                } else {
                    document.getElementById('userIdError').innerText = '사용 가능한 아이디입니다.';
                    alert('사용 가능한 아이디입니다.');
                    isUserIdAvailable = true;
                }
            })
            .catch(error => {
                document.getElementById('userIdError').innerText = error.message;
                alert(error.message);
                isUserIdAvailable = false;
            });
    });

    document.getElementById('checkNameButton').addEventListener('click', function () {
        const name = document.getElementById('name').value;

        if (!name) {
            document.getElementById('nameError').innerText = '닉네임을 입력해주세요.';
            alert('닉네임을 입력해주세요.');
            return;
        }

        fetch(`/api/user/check-name?name=${encodeURIComponent(name)}`, {
            method: 'GET',
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('닉네임 중복 확인 요청 실패');
                }
                return response.json();
            })
            .then(exists => {
                if (exists) {
                    document.getElementById('nameError').innerText = '이미 사용 중인 닉네임입니다.';
                    alert('이미 사용 중인 닉네임입니다.');
                    isNameAvailable = false;
                } else {
                    document.getElementById('nameError').innerText = '사용 가능한 닉네임입니다.';
                    alert('사용 가능한 닉네임입니다.');
                    isNameAvailable = true;
                }
            })
            .catch(error => {
                document.getElementById('nameError').innerText = error.message;
                alert(error.message);
                isNameAvailable = false;
            });
    });
	document.getElementById('emailVerificationButton').addEventListener('click', function () {
	        const email = document.getElementById('email').value;

	        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	        if (!emailRegex.test(email)) {
	            document.getElementById('emailError').innerText = '올바른 이메일 주소를 입력해주세요.';
	            return;
	        }

	        document.getElementById('emailError').innerText = '';
	        document.getElementById('emailVerificationButton').disabled = true;
	        document.getElementById('emailVerificationButton').innerText = '인증 메일 발송 중...';

	        // 이메일 인증 상태 초기화
	        isEmailVerified = false;
	        document.getElementById('verificationCodeButton').disabled = false;
	        document.getElementById('verificationCodeButton').innerText = '코드 확인'; // 버튼 텍스트 초기화

	        fetch(`/api/email/verify-email`, {
	            method: 'POST',
	            headers: {
	                'Content-Type': 'application/json',
	            },
	            body: JSON.stringify({ email: email }),
	        })
	            .then(response => {
	                if (!response.ok) {
	                    throw new Error('이메일 전송에 실패했습니다.');
	                }
	                return response.json();
	            })
	            .then(() => {
	                document.getElementById('emailSuccessMessage').innerText = '이메일을 보냈습니다.';
	                document.getElementById('emailVerificationButton').innerText = '코드 전송 완료';
	            })
	            .catch(error => {
	                document.getElementById('emailError').innerText = error.message;
	                document.getElementById('emailVerificationButton').innerText = '코드 전송';
	            })
	            .finally(() => {
	                document.getElementById('emailVerificationButton').disabled = false;
	            });
	    });

	    document.getElementById('verificationCodeButton').addEventListener('click', function () {
	        const email = document.getElementById('email').value;
	        const verificationCode = document.getElementById('verificationCode').value;

	        fetch(`/api/email/verify-code?email=${encodeURIComponent(email)}&code=${encodeURIComponent(verificationCode)}`, {
	            method: 'POST',
	        })
	            .then(response => {
	                if (!response.ok) {
	                    throw new Error('인증 코드가 유효하지 않거나 이미 사용되었습니다.');
	                }
	                return response.json();
	            })
	            .then(() => {
	                document.getElementById('verificationCodeError').innerText = '인증 코드가 확인되었습니다.';
	                isEmailVerified = true;

	                // 인증 코드 확인 후 버튼 비활성화
	                document.getElementById('verificationCodeButton').disabled = true;
	                document.getElementById('verificationCodeButton').innerText = '이미 인증되었습니다.';
	            })
	            .catch(error => {
	                document.getElementById('verificationCodeError').innerText = error.message;
	                isEmailVerified = false;
	            });
	    });
});

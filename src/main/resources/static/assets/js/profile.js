document.addEventListener('DOMContentLoaded', () => {
    const addButton = document.getElementById('add-button');
    const popup = document.getElementById('popup');
    const closeButton = document.getElementById('close-button');
    const saveButton = document.getElementById('save-button');
    const profileList = document.getElementById('profile-list');
    const imageInput = document.getElementById('profile-image');
    const cropPopup = document.getElementById('crop-popup');
    const cropImage = document.getElementById('crop-image');
    const cropSaveButton = document.getElementById('crop-save-button');
    const cropCancelButton = document.getElementById('crop-cancel-button');
    const previewImage = document.getElementById('profile-preview');

    let currentProfileIndex = 0;
    let cropper;
    let croppedImageBlob = null;

    // 이미지 선택 이벤트 처리
    imageInput.addEventListener('change', (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                cropImage.src = e.target.result;
                cropPopup.style.display = 'block';

                if (cropper) cropper.destroy();
                cropper = new Cropper(cropImage, {
                    aspectRatio: 1,
                    viewMode: 2,
                });
            };
            reader.readAsDataURL(file);
        }
    });

    // 크롭 취소 버튼
    cropCancelButton.addEventListener('click', () => {
        cropPopup.style.display = 'none';
        if (cropper) cropper.destroy();
    });

    // 크롭 확인 버튼
    cropSaveButton.addEventListener('click', () => {
        if (cropper) {
            const canvas = cropper.getCroppedCanvas();
            canvas.toBlob((blob) => {
                croppedImageBlob = blob;
                const previewUrl = URL.createObjectURL(blob);
                previewImage.src = previewUrl;
                previewImage.style.display = 'block';
                cropPopup.style.display = 'none';
                cropper.destroy();
            });
        }
    });

    // 프로필 추가 버튼 클릭
    addButton.addEventListener('click', () => {
        popup.style.display = 'flex';
        imageInput.value = '';
        croppedImageBlob = null;
        document.getElementById('name').value = '';
        document.getElementById('breed').value = '';
        document.getElementById('age').value = '';
        document.getElementById('birthdate').value = '';
        previewImage.src = '';
        previewImage.style.display = 'none';
        saveButton.dataset.mode = 'add';
        saveButton.dataset.petId = '';
    });

    // 팝업 닫기 버튼
    closeButton.addEventListener('click', () => {
        popup.style.display = 'none';
    });

    // 저장 버튼
    saveButton.addEventListener('click', () => {
        const name = document.getElementById('name').value.trim();
        const breed = document.getElementById('breed').value.trim();
        const age = document.getElementById('age').value.trim();
        const birthdate = document.getElementById('birthdate').value.trim();
        const mode = saveButton.dataset.mode;
        const petId = saveButton.dataset.petId;

        if (!name || !breed || isNaN(age) || age <= 0 || !birthdate) {
            alert("모든 항목을 올바르게 입력해주세요.");
            return;
        }

        const formData = new FormData();
        formData.append('name', name);
        formData.append('breed', breed);
        formData.append('age', parseInt(age, 10));
        formData.append('birthdate', birthdate);

        if (croppedImageBlob) {
            formData.append('photo', croppedImageBlob);
        }

        const url = mode === 'add' ? '/pets' : `/pets/${petId}`;
        const method = mode === 'add' ? 'POST' : 'PUT';

        fetch(url, {
            method: method,
            body: formData,
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => {
                        throw new Error(err.message || '서버에서 오류가 발생했습니다.');
                    });
                }
                return response.json();
            })
            .then(data => {
                const imageUrl = croppedImageBlob
                    ? URL.createObjectURL(croppedImageBlob)
                    : `${data.photoUrl}?t=${Date.now()}`;
                if (mode === 'add') {
                    addProfileToUI(
                        imageUrl,
                        data.petName,
                        data.petBreed,
                        data.petAge,
                        data.petBirthdate,
                        data.petId
                    );
                } else if (mode === 'edit') {
                    updateProfileInUI(
                        {
                            ...data,
                            photoUrl: imageUrl,
                        },
                        petId
                    );
                }
                updateSessionStorage();
                popup.style.display = 'none';
                croppedImageBlob = null;
            })
            .catch(error => {
                console.error('에러 발생:', error);
                alert(`작업 중 오류가 발생했습니다: ${error.message}`);
            });
    });

    // 프로필 추가
    function addProfileToUI(imageSrc, name, breed, age, birthdate, petId) {
        const card = document.createElement('div');
        card.classList.add('profile-card');
        card.setAttribute('data-id', petId);
        card.innerHTML = `
            <img src="${imageSrc}" alt="${name}">
            <h3>${name}</h3>
            <p>품종: ${breed}</p>
            <p>나이: ${age}세</p>
            <p>생일: ${birthdate}</p>
            <button class="edit-button">수정</button>
            <button class="delete-button">삭제</button>
        `;

        card.querySelector('.edit-button').addEventListener('click', () => editProfile(petId, card));
        card.querySelector('.delete-button').addEventListener('click', () => deleteProfile(petId, card));

        profileList.appendChild(card);
    }

    // 프로필 수정
    function editProfile(petId, card) {
        document.getElementById('name').value = card.querySelector('h3').innerText;
        document.getElementById('breed').value = card.querySelector('p:nth-child(3)').innerText.replace('품종: ', '');
        document.getElementById('age').value = card.querySelector('p:nth-child(4)').innerText.replace('나이: ', '').replace('세', '');
        document.getElementById('birthdate').value = card.querySelector('p:nth-child(5)').innerText.replace('생일: ', '');

        const imgSrc = card.querySelector('img').src;
        previewImage.src = imgSrc;
        previewImage.style.display = 'block';
        delete previewImage.dataset.croppedBlob;

        imageInput.value = '';
        saveButton.dataset.mode = 'edit';
        saveButton.dataset.petId = petId;
        popup.style.display = 'block';
    }

    // 프로필 업데이트
   function updateProfileInUI(data, petId) {
       const card = document.querySelector(`[data-id="${petId}"]`);
       if (!card) return;

       // 텍스트 정보 업데이트
       card.querySelector('h3').innerText = data.petName;
       card.querySelector('p:nth-child(3)').innerText = `품종: ${data.petBreed}`;
       card.querySelector('p:nth-child(4)').innerText = `나이: ${data.petAge}세`;
       card.querySelector('p:nth-child(5)').innerText = `생일: ${data.petBirthdate}`;

       // 이미지 즉시 업데이트
       const img = card.querySelector('img');
       if (data.photoUrl) {
           // 기존 이미지 유지하면서 새 이미지 로드
           const tempImg = new Image();
           tempImg.onload = () => {
               img.src = tempImg.src;
           };
           tempImg.src = data.photoUrl;
       }
   }

    // 프로필 삭제
    function deleteProfile(petId, card) {
        if (confirm('이 프로필을 삭제하시겠습니까?')) {
            fetch(`/pets/${petId}`, { method: 'DELETE' })
                .then(response => {
                    if (!response.ok) throw new Error('삭제 실패');
                    card.remove();
                    updateSessionStorage();
                })
                .catch(error => {
                    console.error('삭제 중 오류 발생:', error);
                    alert('프로필 삭제에 실패했습니다.');
                });
        }
    }

    // 세션 스토리지 업데이트
    function updateSessionStorage() {
        const profiles = [];
        document.querySelectorAll('.profile-card').forEach(card => {
            profiles.push({
                petId: card.getAttribute('data-id'),
                petName: card.querySelector('h3').innerText,
                petBreed: card.querySelector('p:nth-child(3)').innerText.replace('품종: ', ''),
                petAge: card.querySelector('p:nth-child(4)').innerText.replace('나이: ', '').replace('세', ''),
                birthdate: card.querySelector('p:nth-child(5)').innerText.replace('생일: ', ''),
            });
        });
        sessionStorage.setItem('profiles', JSON.stringify(profiles));
    }

   // 데이터 로드 함수 수정
      async function loadProfiles() {
          // 먼저 캐시된 데이터를 렌더링
          const cachedProfiles = sessionStorage.getItem('profiles');
          if (cachedProfiles) {
              renderProfiles(JSON.parse(cachedProfiles));
          }

          // 백그라운드에서 서버 데이터 동기화
          try {
              const response = await fetch('/pets');
              if (!response.ok) throw new Error('프로필 로드 실패');
              const serverData = await response.json();
              
              // 서버 데이터와 캐시 데이터가 다른 경우에만 업데이트
              const cachedData = cachedProfiles ? JSON.parse(cachedProfiles) : [];
              if (JSON.stringify(serverData) !== JSON.stringify(cachedData)) {
                  sessionStorage.setItem('profiles', JSON.stringify(serverData));
                  renderProfiles(serverData);
              }
          } catch (error) {
              console.error('프로필 로딩 오류:', error);
              // 에러가 발생해도 캐시된 데이터는 유지
          }
      }

    // 프로필 렌더링
    function renderProfiles(profiles) {
        profileList.innerHTML = '';
        profiles.forEach(profile => {
            addProfileToUI(
                `/pets/${profile.petId}/photo`,
                profile.petName,
                profile.petBreed || '정보 없음',
                profile.petAge || '정보 없음',
                profile.birthdate || '정보 없음',
                profile.petId
            );
        });
    }

    // 화살표 버튼
    const leftArrow = document.createElement('button');
    leftArrow.classList.add('arrow-button', 'left-arrow');
    leftArrow.innerHTML = '&lt;';
    profileList.parentElement.appendChild(leftArrow);

    const rightArrow = document.createElement('button');
    rightArrow.classList.add('arrow-button', 'right-arrow');
    rightArrow.innerHTML = '&gt;';
    profileList.parentElement.appendChild(rightArrow);

    rightArrow.addEventListener('click', () => {
        if (currentProfileIndex < profileList.children.length - 1) {
            currentProfileIndex++;
            updateProfileList();
        }
    });

    leftArrow.addEventListener('click', () => {
        if (currentProfileIndex > 0) {
            currentProfileIndex--;
            updateProfileList();
        }
    });

    function updateProfileList() {
        profileList.style.transform = `translateX(-${currentProfileIndex * 260}px)`;
    }

    // 초기 데이터 로드
    loadProfiles();
});
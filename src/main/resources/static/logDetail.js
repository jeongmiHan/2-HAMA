const logId = window.location.pathname.split("/").pop();
document.addEventListener("DOMContentLoaded", async () => {
	
	const imageContainer = document.getElementById("logImages");
	const modal = document.getElementById("imageGalleryModal");
	const modalImage = document.getElementById("galleryImage");
	const closeModal = document.getElementById("closeModal");
	const prevImage = document.getElementById("prevImage");
	const nextImage = document.getElementById("nextImage");
	let currentImageIndex = 0;
	let imageList = [];
	
    const commentInput = document.getElementById("commentInput");
    const submitComment = document.getElementById("submitComment");
    const commentList = document.getElementById("commentList");
    
    let parentReplyId = null; // 부모 댓글 ID
	
	const popupContainer = document.getElementById("logPopupContainer");
	const logContentInput = document.getElementById("logContentInput");
	const logImageInput = document.getElementById("logImageInput");
	const logSubmitButton = document.getElementById("logSubmitButton");
	const logCloseButton = document.getElementById("logCloseButton");

	let deletedFiles = []; // 삭제된 파일 목록

	// 팝업 열기
	window.openEditPopup = async function () {
	    const logFileListContainer = document.getElementById("logFileListContainer");

	    try {
	        const response = await fetch(`/log/${logId}`);
	        if (!response.ok) throw new Error("일기 조회 실패");

	        const logData = await response.json();
	        document.getElementById("logContentInput").value = logData.content || "";

	        // 기존 첨부파일 목록 표시
	        logFileListContainer.innerHTML = logData.images
	            .map(
	                (image) => `
	                <div class="file-item" data-filename="${image}">
	                    <a href="/log/images/${image}" target="_blank">${image}</a>
	                    <button type="button" class="delete-file-button" data-filename="${image}">삭제</button>
	                </div>`
	            )
	            .join("");

	        // 삭제 버튼 이벤트 추가
	        document.querySelectorAll(".delete-file-button").forEach((button) => {
	            button.addEventListener("click", () => {
	                const filename = button.getAttribute("data-filename");
	                deletedFiles.push(filename);
	                button.parentElement.style.display = "none"; // UI에서 숨기기
	            });
	        });

	        document.getElementById("logPopupContainer").style.display = "flex";
	    } catch (error) {
	        console.error("팝업 열기 실패:", error);
	        alert("일기 데이터를 불러오지 못했습니다.");
	    }
	};

	// 팝업 닫기 (취소 버튼 클릭)
	logCloseButton.addEventListener("click", () => {
	    // 모달 닫기
	    popupContainer.style.display = "none";

	    // 입력 내용 초기화
	    logContentInput.value = "";
	    logImageInput.value = null;

	    // 삭제 대기 목록 초기화 (삭제 예정 상태 취소)
	    deletedFiles = [];

	    // 기존 상태 복원은 하지 않음 (모달 닫고 끝냄)
	});

	// 팝업 저장 버튼 클릭
	logSubmitButton.addEventListener("click", async () => {
	    const content = logContentInput.value.trim();
	    if (!content) {
	        alert("내용을 입력하세요!");
	        return;
	    }

	    const formData = new FormData();
	    formData.append("content", content);
	    Array.from(logImageInput.files).forEach(file => formData.append("logFiles", file));
	    formData.append("deletedFiles", JSON.stringify(deletedFiles)); // 삭제 예정 파일 목록 전송

	    try {
	        const response = await fetch(`/log/${logId}/update`, {
	            method: "PUT",
	            body: formData,
	        });

	        if (!response.ok) throw new Error("일기 수정 실패");

	        alert("일기가 수정되었습니다.");
	        location.reload();
	    } catch (error) {
	        console.error("일기 수정 중 오류:", error);
	        alert("일기 수정에 실패했습니다.");
	    }
	});

	// 수정 첨부파일 삭제
	async function deleteLog() {
	    const confirmation = confirm("정말로 삭제하시겠습니까?");
	    if (!confirmation) return;

	    try {
	        const response = await fetch(`/log/${logId}/delete`, { method: "DELETE" });
	        if (!response.ok) throw new Error("삭제 실패");

	        // DOM에서 항목 제거
	        document.getElementById(`log-${logId}`).remove();

	        alert("일기가 삭제되었습니다.");
	    } catch (error) {
	        console.error("삭제 중 오류:", error);
	    }
	}

	// UI 업데이트 함수
	function updateImageList(images) {
	    const imageContainer = document.getElementById("logImages");
	    imageContainer.innerHTML = images
	        .map((image) => `<img src="/log/images/${image}" alt="${image}" />`)
	        .join("");
	}

	// 게시글 로드
	async function loadLogDetail() {
	    try {
	        const response = await fetch(`/log/${logId}`); // ID로 데이터 요청
	        const log = await response.json();
			
			if (!response.ok || !log) {
			            throw new Error("로그 데이터를 불러올 수 없습니다.");
			}
			
			// 작성자 여부에 따라 드롭다운 표시
	        if (log.isAuthor) {
	            document.getElementById("menuButton").style.display = "block"; // 버튼 표시
	        } else {
	            document.getElementById("menuButton").style.display = "none"; // 버튼 숨기기
	        }

	        // 화면에 데이터 적용
	        document.getElementById("nickname").innerText = log.author || "익명";
	        document.getElementById("logTime").innerText = log.timeAgo || "시간 정보 없음";
	        document.getElementById("logContent").innerText = log.content || "내용 없음";

			// 프로필 이미지 처리
			const profileImageElement = document.querySelector(".profile-image");
			if (log.photoUrl) {
			    profileImageElement.src = log.photoUrl;
			} else {
			    profileImageElement.src = "/static/default-profile.jpg"; // 기본 이미지 경로
			}
			
	        // 이미지 리스트 표시
	        imageList = log.images.map(img => `/log/images/${img}`); // 이미지 URL 수정
	        imageList.forEach((src, index) => {
	            const imgElement = document.createElement("img");
	            imgElement.src = src;
	            imgElement.alt = `이미지 ${index + 1}`;
	            imgElement.classList.add("thumbnail");
	            imgElement.addEventListener("click", () => openModal(index));
	            imageContainer.appendChild(imgElement);
	        });
	    } catch (error) {
	        console.error("로그 세부 정보를 불러오는 중 오류 발생:", error);
	    }
	}

	// 모달 열기
	function openModal(index) {
	    currentImageIndex = index;
	    modalImage.src = imageList[index];
	    modal.style.display = "block";
	}

	// 모달 닫기
	closeModal.addEventListener("click", () => {
	    modal.style.display = "none";
	});
	// 이전 이미지 보기
	prevImage.addEventListener("click", () => {
	    currentImageIndex = (currentImageIndex - 1 + imageList.length) % imageList.length;
	    modalImage.src = imageList[currentImageIndex];
	});

	// 다음 이미지 보기
	nextImage.addEventListener("click", () => {
	    currentImageIndex = (currentImageIndex + 1) % imageList.length;
	    modalImage.src = imageList[currentImageIndex];
	});

	// ESC 키로 모달 닫기
	window.addEventListener("keydown", (e) => {
	    if (e.key === "Escape") modal.style.display = "none";
	});
	
	loadLogDetail(); // 함수 호출
	
    // 댓글 렌더링 함수
	function renderReplies(replies, parentElement) {
	  const template = document.getElementById("comment-template");

	  if (!template) {
	    console.error("댓글 템플릿을 찾을 수 없습니다.");
	    return;
	  }

	  replies.forEach(reply => {
	    const clone = template.content.cloneNode(true);
	    const replyElement = clone.querySelector(".reply-item");

	    if (!replyElement) {
	      console.error("댓글 템플릿 구조가 잘못되었습니다. `.reply-item`이 누락되었습니다.");
	      return;
	    }

	    const isDeleted = reply.logReplyContent === "댓글이 삭제되었습니다.";

	    if (isDeleted) {
	      // 삭제된 댓글 처리
	      replyElement.innerHTML = ""; // 기존 내용 초기화
	      replyElement.textContent = "댓글이 삭제되었습니다."; // 삭제 메시지
	      replyElement.style.color = "#888";
	      replyElement.style.fontStyle = "italic";

	      // 자식 댓글 처리
	      if (reply.childReplies && reply.childReplies.length > 0) {
	        const childContainer = document.createElement("div");
	        childContainer.classList.add("logReplyContentList");
	        replyElement.appendChild(childContainer);
	        renderReplies(reply.childReplies, childContainer); // 자식 댓글 재귀 렌더링
	      }
	    } else {
	      // 댓글 정보 설정
	      replyElement.querySelector("[data-nickname]").textContent = reply.author || "익명";
	      replyElement.querySelector(".comment-time").textContent = reply.timeAgo || "방금 전";
	      replyElement.querySelector(".comment-content p").textContent = reply.logReplyContent;

	      // 수정/삭제 버튼 표시 여부
	      const editButton = replyElement.querySelector(".edit-button");
	      const deleteButton = replyElement.querySelector(".delete-button");
		  const commentActions = replyElement.querySelector(".comment-actions");
		  
	      if (reply.isAuthor) {
			commentActions.style.display = "block"; // 작성자만 버튼 보이기

	        // 수정 버튼 이벤트
	        editButton.addEventListener("click", () => {
	          prepareEditReply(reply.id, reply.logReplyContent);
	        });

	        // 삭제 버튼 이벤트
	        deleteButton.addEventListener("click", () => {
	          deleteReply(null, reply.id);
	          reply.logReplyContent = "댓글이 삭제되었습니다.";
	          replyElement.innerHTML = "";
	          replyElement.textContent = "댓글이 삭제되었습니다.";
	          replyElement.style.color = "#888";
	          replyElement.style.fontStyle = "italic";

	          // 자식 댓글 유지
	          if (reply.childReplies && reply.childReplies.length > 0) {
	            const childContainer = document.createElement("div");
	            childContainer.classList.add("logReplyContentList");
	            replyElement.appendChild(childContainer);
	            renderReplies(reply.childReplies, childContainer);
	          }
	        });
	      } else {
	        // 작성자가 아닌 경우 버튼 숨김
	        commentActions.style.display = "none";
	      }

	      // 답글 버튼 이벤트
	      replyElement.querySelector(".reply-button").addEventListener("click", () => {
	        setReplyParent(reply.id, reply.author);
	      });

	      // 자식 댓글 처리
	      if (reply.childReplies && reply.childReplies.length > 0) {
	        const childContainer = document.createElement("div");
	        childContainer.classList.add("logReplyContentList");
	        replyElement.appendChild(childContainer);
	        renderReplies(reply.childReplies, childContainer);
	      }
	    }

	    parentElement.appendChild(clone);
	  });
	}

  
	let isEditMode = false; // 수정 모드 여부
	let editReplyId = null; // 수정할 댓글 ID
	
	// 부모 ID 설정 후 답글
	window.setReplyParent = function(replyId, author) {
	    isEditMode = false; // 답글 모드로 설정
	    parentReplyId = replyId; // 부모 ID 설정
	    commentInput.focus(); // 입력창에 포커스

	    // 입력창에 @replyId 추가
	    commentInput.placeholder = `@${author} 답글을 입력하세요...`;
	    commentInput.value = `@${author} `;
	};
	// 댓글 수정 
	window.prepareEditReply = function(replyId, content) {
	    isEditMode = true; // 수정 모드 활성화
	    editReplyId = replyId; // 수정할 댓글 ID 저장
	    commentInput.value = content; // 기존 내용 표시
	    commentInput.placeholder = "댓글을 수정하세요...";
	    commentInput.focus(); // 입력창 포커스
	};

	// 댓글 추가/수정 함수
	window.addReply = async () => {
		try {
		    const replyText = commentInput.value.trim();
		    if (!replyText) {
		        alert("댓글 내용을 입력하세요!");
		        return;
		    }

		    const response = await fetch(`/reply/log/${logId}/reply`, {
		        method: "POST",
		        headers: { "Content-Type": "application/json" },
		        body: JSON.stringify({
		            logReplyContent: replyText,
		            parentReplyId: parentReplyId,
		        }),
		    });

		    if (!response.ok) throw new Error("댓글 추가 실패");
		    const responseData = await response.json();

		    if (responseData.status !== "success") {
		        throw new Error(responseData.message || "댓글 추가 실패");
		    }

		    alert("댓글이 추가되었습니다.");
		    location.reload();
		} catch (error) {
		    console.error("댓글 추가 오류:", error);
		    alert("댓글 추가 중 오류가 발생했습니다.");
		} finally {
		    commentInput.value = "";
		    parentReplyId = null;
		    commentInput.placeholder = "댓글을 작성해보세요.";
		}
	};
	// 댓글 수정 함수
	window.editReply = async () => {
	    try {
	        const replyText = commentInput.value.trim();
	        if (!replyText) {
	            alert("댓글 내용을 입력하세요!");
	            return;
	        }

	        const response = await fetch(`/reply/log/${editReplyId}/edit`, {
	            method: "PUT",
	            headers: { "Content-Type": "application/json" },
	            body: JSON.stringify({
	                logReplyContent: replyText,
	            }),
	        });

	        if (!response.ok) throw new Error("댓글 수정 실패");
	        const responseData = await response.json();

	        if (responseData.status !== "success") {
	            throw new Error(responseData.message || "댓글 수정 실패");
	        }

	        alert("댓글이 수정되었습니다.");
	        location.reload();
	    } catch (error) {
	        console.error("댓글 수정 오류:", error);
	        alert("댓글 수정 중 오류가 발생했습니다.");
	    } finally {
	        commentInput.value = "";
	        isEditMode = false;
	        editReplyId = null;
	        commentInput.placeholder = "댓글을 작성해보세요.";
	    }
	};

    // 댓글 삭제 함수
    window.deleteReply = async function(button, replyId) {
        const confirmed = confirm("정말로 삭제하시겠습니까?");
        if (!confirmed) return;

        try {
            const response = await fetch(`/reply/log/${replyId}/del`, { method: 'DELETE' });
            if (!response.ok) throw new Error('댓글 삭제 실패');
            alert('댓글이 삭제되었습니다.');
            location.reload();
        } catch (error) {
            console.error("댓글 삭제 오류:", error);
            alert("댓글 삭제 중 오류가 발생했습니다.");
        }
    };

    // 댓글 데이터 로딩
    async function fetchReplies() {
        try {
            const response = await fetch(`/reply/log/${logId}/replies`);
            if (!response.ok) throw new Error('댓글 조회 실패');
            const result = await response.json();

            if (!result || result.status !== "success") {
                throw new Error("응답 상태 실패");
            }

            const replies = result.replies || [];
            commentList.innerHTML = ""; // 초기화
			
			if (result.replies.length === 0) {
			    // 댓글이 없을 경우 메시지 추가
			    const noCommentsMessage = document.createElement("p");
			    noCommentsMessage.textContent = "첫 댓글을 작성해보세요!";
				noCommentsMessage.style.textAlign = "center";
				noCommentsMessage.style.color = "#888";
				noCommentsMessage.style.fontSize = "14px";
				commentList.appendChild(noCommentsMessage);
			} else {
			    // 댓글이 있을 경우 렌더링
            	renderReplies(replies, commentList); // 계층형 렌더링
			}
			
        } catch (error) {
            console.error("댓글 불러오기 실패:", error);
        }
    }

    // 댓글 초기 로딩
    fetchReplies();

	// 이벤트 핸들러 분리
	submitComment.addEventListener("click", () => {
	    if (isEditMode) {
	        editReply();
	    } else {
	        addReply();
	    }
	});
	
	// 댓글 수정 버튼 이벤트 등록
	commentInput.addEventListener("keydown", (event) => {
	    if (event.key === "Enter" && !event.shiftKey) {
	        event.preventDefault();
	        if (isEditMode) {
	            editReply();
	        } else {
	            addReply();
	        }
	    }
	});
    const menuButton = document.getElementById("menuButton");
    const menuDropdown = document.getElementById("menuDropdown");

	// 드롭다운 토글
	menuButton.addEventListener("click", (event) => {
	    event.stopPropagation(); // 이벤트 버블링 방지
	    const isVisible = menuDropdown.style.display === "block";
	    menuDropdown.style.display = isVisible ? "none" : "block";
	});

	// 드롭다운 외부 클릭 시 닫기
	document.addEventListener("click", () => {
	    menuDropdown.style.display = "none";
	});

	// 수정 버튼 클릭
	document.querySelector("#menuDropdown li:nth-child(1)").addEventListener("click", () => {
	    menuDropdown.style.display = "none";
	    openEditPopup();
	});

	function openEditPopup() {
	    const popupContainer = document.getElementById("logPopupContainer");
	    popupContainer.style.display = "flex";
	}
	// 삭제 기능
	window.deleteLog = async function() {
		const confirmed = confirm("정말로 삭제하시겠습니까?");
		if (!confirmed) return;

		try {
		    const response = await fetch(`/log/${logId}/delete`, { method: 'DELETE' });
		    if (!response.ok) throw new Error('일기 삭제 실패');
		    alert('일기가 삭제되었습니다.');
		    window.location.href = `/log/indexLog`;
		} catch (error) {
		    console.error("일기 삭제 오류:", error);
		    alert("일기 삭제 중 오류가 발생했습니다.");
		}
		
	};
	
	function toggleEditMode(isEdit) {
	    const logContent = document.getElementById("logContent");
	    const logEditContent = document.getElementById("logEditContent");
	    const saveButton = document.getElementById("saveLogButton");
	    const logEditFileContainer = document.getElementById("logEditFileContainer");
	    const logEditFileInput = document.getElementById("logEditFileInput");

	    if (isEdit) {
	        // 수정 모드 활성화
	        logEditContent.style.display = "block";
	        logEditContent.value = logContent.textContent.trim();
	        logContent.style.display = "none";
	        saveButton.style.display = "inline-block";
	        logEditFileContainer.style.display = "block";

	        // 기존 첨부파일 프리뷰 로드 (가정: currentLogAttachment는 서버에서 받은 첨부파일 정보)
	        if (currentLogAttachment) {
	            const attachmentPreview = document.createElement("p");
	            attachmentPreview.id = "logAttachmentPreview";
	            attachmentPreview.textContent = `첨부된 파일: ${currentLogAttachment}`;
	            logEditFileContainer.prepend(attachmentPreview);
	        }
	    } else {
	        // 수정 모드 비활성화
	        logEditContent.style.display = "none";
	        logContent.style.display = "block";
	        saveButton.style.display = "none";
	        logEditFileContainer.style.display = "none";

	        // 기존 프리뷰 제거
	        const attachmentPreview = document.getElementById("logAttachmentPreview");
	        if (attachmentPreview) {
	            attachmentPreview.remove();
	        }
	    }
	}

	async function saveLog() {
	    const logEditContent = document.getElementById("logContentInput").value.trim();
	    const logImageInput = document.getElementById("logImageInput");

	    const formData = new FormData();
	    formData.append("content", logEditContent);
	    Array.from(logImageInput.files).forEach(file => formData.append("logFiles", file));
	    formData.append("deletedFiles", JSON.stringify(deletedFiles)); // 삭제 대기 목록 전송

	    try {
	        const response = await fetch(`/log/${logId}/update`, {
	            method: "PUT",
	            body: formData,
	        });

	        if (response.ok) {
	            alert("일기가 수정되었습니다.");
	            location.reload(); // 수정 후 페이지 새로고침
	        } else {
	            throw new Error("일기 수정 실패");
	        }
	    } catch (error) {
	        console.error("일기 수정 중 오류 발생:", error);
	        alert("일기 수정에 실패했습니다.");
	    }
	}
	// 댓글 페이지 이동 URL에 해시 값이 있는 경우 처리 
	window.addEventListener("load", () => {
	    if (window.location.hash === "#commentList") {
	        const commentSection = document.getElementById("commentList");
	        if (commentSection) {
	            setTimeout(() => {
	                commentSection.scrollIntoView({ behavior: "smooth" });
	            }, 200); // 약간의 지연 추가
	        } else {
	            console.error("댓글 섹션을 찾을 수 없습니다.");
	        }
	    }
	});

	// 좋아요 증가 또는 취소
	window.increaseLike = async (button) => {
		    const postElement = button.closest(".logPost");

		    try {
		        const response = await fetch(`/log/${logId}/like`, {
		            method: 'POST',
		            headers: {
		                'Content-Type': 'application/json',
		            },
		        });

		        if (!response.ok) {
		            const errorMessage = await response.text();
		            console.error("Server Error:", errorMessage);
		            throw new Error('Failed to toggle like');
		        }

		        const { isLiked, totalLikes } = await response.json(); // 서버 응답 데이터
		        console.log(`isLiked: ${isLiked}, totalLikes: ${totalLikes}`);

		        // UI 업데이트
		        const likeCount = button.querySelector("span");
		        likeCount.textContent = totalLikes;

		        // 버튼 상태 업데이트
		        if (isLiked) {
		            button.classList.add("liked"); // 좋아요 활성화
		        } else {
		            button.classList.remove("liked"); // 좋아요 비활성화
		        }
		    } catch (error) {
		        console.error("Error toggling like:", error);
		        alert("좋아요 처리 중 오류가 발생했습니다.");
		    }
		};
		window.toggleBookmark = async function(button) {
		    const postElement = button.closest(".logPost"); // 게시물 요소 찾기

		    try {
		        // 즐겨찾기 API 호출
		        const response = await fetch(`/log/${logId}/bookmark`, {
		            method: 'POST',
		            headers: { 'Content-Type': 'application/json' },
		        });

		        if (!response.ok) {
		            const errorMessage = await response.text();
		            console.error("Server Error:", errorMessage);
		            throw new Error('즐겨찾기 토글 실패');
		        }

		        const { isBookmarked, totalBookmarks } = await response.json(); // 서버 응답 데이터
		        console.log(`isBookmarked: ${isBookmarked}, totalBookmarks: ${totalBookmarks}`);

		        // UI 업데이트
		        const bookmarkCount = button.querySelector("span");
		        bookmarkCount.textContent = totalBookmarks;

		        // 버튼 상태 업데이트
		        if (isBookmarked) {
		            button.classList.add("bookmarked"); // 즐겨찾기 활성화
		        } else {
		            button.classList.remove("bookmarked"); // 즐겨찾기 비활성화
		        }
		    } catch (error) {
		        console.error("즐겨찾기 오류:", error);
		        alert("즐겨찾기 처리 중 오류가 발생했습니다.");
		    }
		};
		window.navigateToLogDetail = function(commentButton) {
		    if (logId) {
		        // logDetail로 이동하면서 해시 추가
		        window.location.href = `/detail/${logId}#commentList`;
		    } else {
		        console.error("postId가 유효하지 않습니다.");
		    }
		};
	
});

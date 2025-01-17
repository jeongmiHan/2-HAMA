document.addEventListener("DOMContentLoaded", async () => {
    const postContainer = document.getElementById("logPostContainer");
    const submitButton = document.getElementById("logSubmitButton");
    const writeButton = document.getElementById("writeButton");
    const popupContainer = document.getElementById("logPopupContainer");
    const closeButton = document.getElementById("logCloseButton");
    const imageInput = document.getElementById("logImageInput");

    let uploadedImages = []; // 이미지 URL을 저장할 배열
    let currentImageIndex = 0; // 현재 보고 있는 이미지의 인덱스

    // 팝업 열기 이벤트
    writeButton.addEventListener("click", () => {
        popupContainer.style.display = "flex";
    });
    // 팝업 닫기 이벤트
    closeButton.addEventListener("click", () => {
        popupContainer.style.display = "none";
        uploadedImages = []; // 업로드된 이미지 초기화
    });

	
	
	// 댓글
	// 댓글 추가
	window.addReply = async (button, parentReplyId = null) => {
	    try {
	        const parentElement = button.closest(".reply-item");
	        let parentList;

	        if (parentElement) {
	            parentReplyId = parentElement.dataset.replyId; // 부모 ID 설정
	            parentList = parentElement.querySelector(".logReplyContentList");
	            if (!parentList) {
	                parentList = document.createElement("div");
	                parentList.classList.add("logReplyContentList");
	                parentElement.appendChild(parentList);
	            }
	        } else {
	            const postElement = button.closest(".logPost");
	            parentList = postElement.querySelector(".logReplyContentList");
	        }

	        const commentInput = button.closest(".logReplyContentInput").querySelector("textarea");
	        const replyText = commentInput.value.trim();
	        if (!replyText) {
	            alert("댓글 내용을 입력하세요!");
	            return;
	        }

	        const postElement = button.closest(".logPost");
	        const postId = postElement.querySelector(".logPostContent").dataset.id;

	        const response = await fetch(`/reply/log/${postId}/reply`, {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify({
	                logReplyContent: replyText,
	                parentReplyId: parentReplyId || null // 부모 ID 설정
	            }),
	        });

	        if (!response.ok) throw new Error("댓글 추가 실패");

	        const responseData = await response.json();
	        if (responseData.status !== "success") {
	            throw new Error(responseData.message || "댓글 추가 실패");
	        }

	        renderReplies([{
	            id: responseData.replyId,
	            logReplyContent: replyText,
	            parentReplyId: parentReplyId || null,
	            childReplies: []
	        }], parentList);

	        commentInput.value = "";
	    } catch (error) {
	        console.error("댓글 추가 오류:", error);
	        alert("댓글 추가 중 오류가 발생했습니다.");
	    }
	};

	// 댓글 수정
	window.editReply = async (replyId) => {
	    const newContent = prompt("수정할 내용을 입력하세요:");
	    if (!newContent) return;
	    try {
	        const response = await fetch(`/reply/${replyId}`, {
	            method: 'PUT',
	            headers: { 'Content-Type': 'application/json' },
	            body: JSON.stringify({ logReplyContent: newContent }),
	        });
	        if (!response.ok) throw new Error('댓글 수정 실패');
	        alert('댓글이 수정되었습니다.');
	        location.reload(); // 새로고침으로 UI 업데이트
	    } catch (error) {
	        console.error("댓글 수정 오류:", error);
	    }
	};
	// 댓글 삭제
	window.deleteReply = async (button, replyId) => {
	    const replyElement = button.closest(".reply-item"); // 댓글 요소 찾기
	    const postElement = button.closest(".logPost");
	    const postId = postElement.querySelector(".logPostContent").dataset.id; // 게시글 ID 가져오기

	    if (!replyId || !postId) {
	        alert("댓글 ID 또는 게시글 ID가 없습니다.");
	        return;
	    }

	    if (!confirm("정말로 삭제하시겠습니까?")) return;

	    try {
	        const response = await fetch(`/reply/log/${replyId}/del`, { method: 'DELETE' });
	        if (!response.ok) throw new Error('댓글 삭제 실패');
	        
	        // 성공 메시지
	        alert('댓글이 삭제되었습니다.');

	        // 댓글 내용을 '댓글이 삭제되었습니다.'로 변경 (대댓글은 유지)
	        const contentElement = replyElement.querySelector("p");
	        contentElement.innerText = "댓글이 삭제되었습니다.";
	        contentElement.style.color = "gray";

	        // 답글 입력창 유지
	        const replyInputContainer = replyElement.querySelector(".childReplyInputContainer");
	        if (!replyInputContainer) {
	            replyElement.innerHTML += `
	                <div class="childReplyInputContainer">
	                    <div class="logReplyContentInput">
	                        <textarea placeholder="답글을 입력하세요"></textarea>
	                        <button onclick="addReply(this, '${replyId}')">답글 등록</button>
	                    </div>
	                </div>`;
	        }
	    } catch (error) {
	        console.error("댓글 삭제 오류:", error);
	        alert("댓글 삭제 중 오류가 발생했습니다.");
	    }
	};
	// 댓글 랜더링
	const renderReplies = (replies, parentElement) => {
	    replies.forEach(reply => {
	        const replyElement = document.createElement("div");
	        replyElement.classList.add("reply-item");

	        replyElement.dataset.replyId = reply.id;
	        const content = reply.logReplyContent || reply.content; 
	        replyElement.innerHTML = `
	            <p>${reply.parentReplyId ? `@${reply.parentReplyId}` : ''} ${content}</p>
	            <button onclick="toggleReplyInput(this)">답글 달기</button>
	            <button onclick="editReply('${reply.id}')">수정</button>
	            <button onclick="deleteReply(this, '${reply.id}')">삭제</button>
	        `;

	        let childContainer = parentElement.querySelector(".logReplyContentList");
	        if (!childContainer) {
	            childContainer = document.createElement("div");
	            childContainer.classList.add("logReplyContentList");
	            parentElement.appendChild(childContainer);
	        }
	        childContainer.appendChild(replyElement);

	        if (reply.childReplies && reply.childReplies.length > 0) {
	            renderReplies(reply.childReplies, replyElement);
	        }
	    });
	};
	// DB 댓글 갯수 동기화
	window.updateReplyCount = async (postId) => {
		try {
		    const response = await fetch(`/reply/log/${postId}/count`);
		    if (!response.ok) throw new Error("댓글 수 조회 실패");

			const data = await response.json();
		    const countElement = document.getElementById(`comment-count-${postId}`); // ID로 찾기
			countElement.textContent = data.count;
			if (countElement) {
			    countElement.textContent = data.count; // 댓글 수 동기화
			}
		} catch (error) {
		    console.error("댓글 수 조회 오류:", error);
		}
	};
	// 댓글 목록 불러오기 함수
	window.fetchReplies = async (postId, commentList) => {
	    try {
	        const response = await fetch(`/reply/log/${postId}/replies`);
	        if (!response.ok) throw new Error('댓글 조회 실패');

	        const result = await response.json();
	        if (!result || result.status !== "success") {
	            throw new Error("응답 상태 실패");
	        }

	        const replies = result.replies || [];
	        console.log("불러온 댓글:", replies); // 디버깅용
	        commentList.innerHTML = ""; // 기존 댓글 초기화
	        renderReplies(replies, commentList); // 계층형 댓글 렌더링
	    } catch (error) {
	        console.error("댓글 불러오기 실패:", error);
	    }
	};
	// 댓글 섹션 토글
	window.toggleCommentSection = async (button) => {
	    const commentSection = button.closest(".logPost").querySelector(".logReplyContentSection");
	    const commentList = commentSection.querySelector(".logReplyContentList");
	    const postId = button.closest(".logPost").querySelector(".logPostContent").getAttribute("data-id");
	    commentSection.style.display =
	        commentSection.style.display === "none" ? "block" : "none";
	    if (commentSection.style.display === "block") {
	        await fetchReplies(postId, commentList); // 댓글 불러오기
	    }
	};
	// 답글 입력창 
	window.toggleReplyInput = (button) => {
	    // 기존에 열려 있는 모든 입력창 제거
	    document.querySelectorAll(".childReplyInputContainer").forEach(input => input.remove());

	    const parentReply = button.closest(".reply-item");

	    // 부모 댓글 ID를 HTML 속성에서 가져오기
	    const parentReplyId = parentReply.dataset.replyId;

	    let replyInputContainer = parentReply.querySelector(".childReplyInputContainer");

	    if (replyInputContainer) {
	        replyInputContainer.remove();
	    } else {
	        replyInputContainer = document.createElement("div");
	        replyInputContainer.classList.add("childReplyInputContainer");
	        replyInputContainer.innerHTML = `
	            <div class="logReplyContentInput">
	                <textarea placeholder="답글을 입력하세요"></textarea>
	                <button onclick="addReply(this, '${parentReplyId}')">답글 등록</button>
	            </div>`;
	        parentReply.appendChild(replyInputContainer); // 부모 댓글 아래에 추가
	    }
	};
	
	
	
	// 게시글
    // 게시물 렌더링 함수
    const renderPost = (post, prepend = false) => {
        const postElement = document.createElement("div");
        postElement.classList.add("logPost");

		const postId = post.id; // 게시물 ID
		const countElement = postElement.querySelector(".logPostComment span");

        const contentLines = post.content.split("\n");
        let displayContent = post.content;
        if (contentLines.length > 3) {
            displayContent =
                contentLines.slice(0, 3).join("\n") +
                '...<span class="show-more" onclick="showMore(this)">더보기</span>';
            postElement.dataset.fullContent = post.content;
        }
		// 이미지 경로를 그대로 출력
		const imagesHtml = post.images
		    ? post.images.map((image) => `<img src="/log/images/${image}" alt="게시물 이미지" class="logPostImage">`).join("")
		    : "";	
        postElement.innerHTML = `
			<div class="logPostHeader">
			    <div class="logPostAuthor">${post.author}</div>
			    <div class="logPostTime">${post.timeAgo}</div>
			</div>
			<div class="logPostContent" data-id="${post.id}">${displayContent}</div>
			${imagesHtml}
			<div class="logPostFooter">
			    <div class="logPostComment" onclick="toggleCommentSection(this)">💬<span id="comment-count-${post.id}">${post.comments || 0}</span></div>
			    <div class="logPostLike" onclick="increaseLike(this)">❤️ <span>${post.likes|| 0}</span></div>
			    <div>🔖 <span>${post.saves}</span></div>
			</div>
			<div class="logReplyContentSection" style="display: none;">
			    <div class="logReplyContentInput">
			        <textarea placeholder="댓글을 입력하세요..."></textarea>
			        <button onclick="addReply(this)">댓글 등록</button>
			    </div>
			    <div class="logReplyContentList">
				</div>
			</div>
        `;
	
		if (prepend) {
		    // 새 글은 항상 맨 위에 추가
		    postContainer.prepend(postElement);
		} else {
		    // 기존 글은 reverse() 결과대로 추가
		    postContainer.append(postElement);
		}
		// 댓글 수 동기화
		updateReplyCount(postId, countElement);
		
	};
	
	//프론트엔드 데이터 렌더링 유지
	window.onload = async () => {
		try {
		    const response = await fetch("/log/list");
		    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

		    const text = await response.text(); // 응답 데이터 원본 확인

		    const logs = JSON.parse(text); // JSON 파싱

		    if (!Array.isArray(logs)) throw new Error("Logs is not an array");
		    logs.reverse().forEach((log) => renderPost(log, false));
		} catch (error) {
		    console.error("Error fetching logs:", error);
		}
	};

    // 게시물 추가 이벤트
	submitButton.addEventListener("click", async () => {
	    const contentInput = document.getElementById("logContentInput");
	    const content = contentInput.value.trim();

	    if (!content) {
	        alert("내용을 입력하세요!");
	        return;
	    }

	    const formData = new FormData();
	    formData.append("logContent", content);
	    Array.from(imageInput.files).forEach((file) => formData.append("logFiles", file));

	    try {
	        const response = await fetch("/log/add", {
	            method: "POST",
	            body: formData,
	        });

	        if (!response.ok) throw new Error("Failed to save log");

	        const data = await response.json();
			
			
	        renderPost({
	            id: data.id,
	            author: data.author,
	            timeAgo: data.timeAgo,
	            content: data.content,
	            images: data.images || [],
	            likes: data.likes || 0,
	            comments: data.comments || 0,
	        }, true);

	        contentInput.value = "";
	        imageInput.value = "";
	        uploadedImages = [];
	        popupContainer.style.display = "none";
	    } catch (error) {
	        console.error("Error saving log:", error);
	        alert("로그 저장에 실패했습니다.");
	    }
	});	
	
	// 더보기 링크 클릭 이벤트
	window.showMore = (element) => {
	    const postElement = element.closest(".logPost");
	    const postContent = postElement.querySelector(".logPostContent");
	    const fullContent = postElement.dataset.fullContent;
	    postContent.innerHTML = fullContent; // 전체 내용으로 교체
	};
	
	// 좋아요 증가
	window.increaseLike = async (button) => {
	    const postElement = button.closest(".logPost");
	    const postId = postElement.querySelector(".logPostContent").dataset.id; // postId 가져오기

	    try {
	        const response = await fetch(`/log/${postId}/like`, { // postId 사용
	            method: 'POST',
	        });
	        if (!response.ok) throw new Error('Failed to add like');
	        const result = await response.text();
	        console.log(result);
	        // 좋아요 수 증가
	        const likeCount = button.querySelector("span");
	        likeCount.textContent = parseInt(likeCount.textContent) + 1;
	    } catch (error) {
	        console.error("Error adding like:", error);
	        alert("좋아요 추가 중 오류가 발생했습니다.");
	    }
	};
	// 세부 페이지 이동
	postContainer.addEventListener("click", (event) => {
	    const postContent = event.target.closest(".logPostContent");
	    const postHeader = event.target.closest(".logPostHeader");
	    
	    if (postContent || postHeader) {
	        const postElement = event.target.closest(".logPost");
	        if (postElement) {
	            const logId = postElement.querySelector(".logPostContent").dataset.id;
	            window.location.href = `/detail/${logId}`; // 상세 페이지로 이동
	        }
	    }
	});
	
});



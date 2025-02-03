document.addEventListener("DOMContentLoaded", async () => {
    const postContainer = document.getElementById("logPostContainer");
    const submitButton = document.getElementById("logSubmitButton");
    const writeButton = document.getElementById("writeButton");
    const popupContainer = document.getElementById("logPopupContainer");
    const closeButton = document.getElementById("logCloseButton");
    const imageInput = document.getElementById("logImageInput");

    // 팝업 열기 이벤트
    writeButton.addEventListener("click", () => {
        popupContainer.style.display = "flex";
    });
    // 팝업 닫기 이벤트
    closeButton.addEventListener("click", () => {
        popupContainer.style.display = "none";
        uploadedImages = []; // 업로드된 이미지 초기화
    });	
	
	// 게시글
    // 게시물 렌더링 함수
    const renderPost = (post, prepend = false) => {
        const postElement = document.createElement("div");
        postElement.classList.add("logPost");

        const contentLines = post.content.split("\n");
        let displayContent = post.content;
        if (contentLines.length > 2) {
            displayContent =
                contentLines.slice(0, 2).join("\n") +
                '<span class="show-more" onclick="showMore(this)">더보기</span>';
            postElement.dataset.fullContent = post.content;
        }
		// 이미지 경로를 그대로 출력
		const imagesHtml = post.images
		    ? post.images.map((image) => `<img src="/log/images/${image}" alt="게시물 이미지" class="logPostImage">`).join("")
		    : "";
		const commentCount = post.comments > 0 ? post.comments : "";	
        postElement.innerHTML = `
			<div class="logPostHeader">
				<div class="logPostAuthorSection">
				    <i class="fas fa-user user-icon"></i>
				    <div class="logPostAuthor">${post.author}</div>
				</div>
			    <div class="logPostTime">${post.timeAgo}</div>
			</div>
			<div class="logPostContent" data-id="${post.id}">${displayContent}</div>
			${imagesHtml}
			<div class="logPostFooter">
			    <div class="logPostComment" onclick="navigateToLogDetail(this)">
					<i class="fas fa-comment"></i>
					<span id="comment-count-${post.id}">${commentCount}</span>
				</div>
			    <div class="logPostLike ${post.isLiked ? 'liked' : ''}" onclick="increaseLike(this)">
					<i class="fas fa-heart"></i> 
					<span>${post.likes|| ""}</span>
				</div>
			    <div class="logPostBookmark ${post.isBookmarked ? 'bookmarked' : ''}" onclick="toggleBookmark(this)">
					<i class="fas fa-bookmark"></i> 
					<span>${post.bookmarks || ""}</span>
				</div>
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
		
		// 댓글 개수 동기화 호출
		updateReplyCount(post.id); 
		
		if (prepend) {
		    // 새 글은 항상 맨 위에 추가
		    postContainer.prepend(postElement);
		} else {
		    // 기존 글은 reverse() 결과대로 추가
		    postContainer.append(postElement);
		}
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
	            timeAgo: data.timeAgo || "방금 전",
	            content: data.content,
	            images: data.images || [],
	            likes: data.likes || 0,
				bookmarks: data.bookmarks || 0,
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
	// 이미지 업로드 개수 제한 (최대 5개)
	 imageInput.addEventListener("change", (event) => {
	     if (imageInput.files.length > 5) {
	         alert("최대 5개까지만 업로드할 수 있습니다.");
	         imageInput.value = ""; // 선택한 파일 초기화
	     }
	 });
	
	// 더보기 링크 클릭 이벤트
	window.showMore = (element) => {
	    const postElement = element.closest(".logPost");
	    const postContent = postElement.querySelector(".logPostContent");
	    const fullContent = postElement.dataset.fullContent;
	    postContent.innerHTML = fullContent; // 전체 내용으로 교체
	};
	
	// 좋아요 증가 또는 취소
	window.increaseLike = async (button) => {
	    const postElement = button.closest(".logPost");
	    const postId = postElement.querySelector(".logPostContent").dataset.id;

	    try {
	        const response = await fetch(`/log/${postId}/like`, {
	            method: 'POST',
	            headers: { 'Content-Type': 'application/json' },
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

	        button.classList.toggle("liked", isLiked);
	    } catch (error) {
	        console.error("Error toggling like:", error);
	    }
	};
	
	window.toggleBookmark = async function (button) {
	    const postElement = button.closest(".logPost");
	    const postId = postElement.querySelector(".logPostContent").dataset.id;

	    try {
	        // 서버로 API 요청
	        const response = await fetch(`/log/${postId}/bookmark`, {
	            method: "POST",
	            headers: { "Content-Type": "application/json" },
	        });

	        if (!response.ok) {
	            const errorMessage = await response.text();
	            console.error("Server Error:", errorMessage);
	            throw new Error("즐겨찾기 토글 실패");
	        }

	        const { isBookmarked, totalBookmarks } = await response.json();
	        console.log(`isBookmarked: ${isBookmarked}, totalBookmarks: ${totalBookmarks}`);

	        // UI 업데이트
	        const bookmarkCount = button.querySelector("span");
	        bookmarkCount.textContent = totalBookmarks;

	        if (isBookmarked) {
	            button.classList.add("bookmarked");
	        } else {
	            button.classList.remove("bookmarked");
	        }

	        console.log("Bookmark toggle completed successfully");
	    } catch (error) {
	        console.error("즐겨찾기 처리 중 오류:", error);
	        alert("즐겨찾기 처리 중 오류가 발생했습니다.");
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
	
	// 댓글 페이지 이동
	window.navigateToLogDetail = function(commentButton) {
	    const postId = commentButton.closest(".logPost").querySelector(".logPostContent").dataset.id;
	    if (postId) {
	        // logDetail로 이동하면서 해시 추가
	        window.location.href = `/detail/${postId}#commentList`;
	    } else {
	        console.error("postId가 유효하지 않습니다.");
	    }
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
	          countElement.textContent = data.count > 0 ? data.count : "";; // 댓글 수 동기화
	      }
	   } catch (error) {
	       console.error("댓글 수 조회 오류:", error);
	   }
	};


	
});



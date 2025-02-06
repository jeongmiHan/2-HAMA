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
			<div class="logPostContentWrapper">
			    <div class="logPostContent" data-id="${post.id}">${displayContent}</div>
			    ${imagesHtml}
			</div>
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
    // 필터 버튼 추가
    const filterButton = document.createElement("button");
    filterButton.textContent = "즐겨찾기한 게시글 보기"; // 기본 상태
    filterButton.id = "filterBookmarkButton";
	filterButton.innerHTML = '<i class="fas fa-bookmark"></i>'; // 아이콘 추가
	document.body.appendChild(filterButton);
	
	// 즐겨찾기 버튼을 글쓰기 버튼 위로 배치
	const writeButtonRect = writeButton.getBoundingClientRect();
	filterButton.style.position = "fixed";
	filterButton.style.right = "30px";
	
	// "내가 쓴 글 보기" 버튼 추가
	const myPostsButton = document.createElement("button");
	myPostsButton.textContent = "내가 쓴 글 보기";
	myPostsButton.id = "myPostsButton";
	myPostsButton.innerHTML = '<i class="fas fa-user"></i>'; // 아이콘 추가
	document.body.appendChild(myPostsButton);

	// 버튼 위치 지정 (기존 버튼과 함께 배치)
	myPostsButton.style.position = "fixed";
	myPostsButton.style.right = "30px";

	let showMyPosts = false;
	let showBookmarked = false;

	// 게시글을 불러오는 공통 함수
	async function loadPosts(url) {
	    try {
	        const response = await fetch(url);
	        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

	        const logs = await response.json();
	        postContainer.innerHTML = ""; // 기존 게시글 초기화
	        logs.reverse().forEach((log) => renderPost(log, false));
	    } catch (error) {
	        console.error("Error fetching logs:", error);
	    }
	}

	// "내가 쓴 글 보기" 버튼 클릭 이벤트
	myPostsButton.addEventListener("click", async () => {
	    if (showBookmarked) {
	        // "즐겨찾기" 버튼이 켜져 있으면 비활성화
	        showBookmarked = false;
	        filterButton.classList.remove("active");
	    }

	    showMyPosts = !showMyPosts;

	    // 버튼 상태 업데이트
	    myPostsButton.classList.toggle("active", showMyPosts);
	    filterButton.classList.remove("active"); // 항상 다른 버튼 비활성화

	    // 게시글 불러오기
	    await loadPosts(showMyPosts ? "/log/myLogs" : "/log/list");
	});

	// "즐겨찾기" 버튼 클릭 이벤트
	filterButton.addEventListener("click", async () => {
	    if (showMyPosts) {
	        // "내가 쓴 글 보기" 버튼이 켜져 있으면 비활성화
	        showMyPosts = false;
	        myPostsButton.classList.remove("active");
	    }

	    showBookmarked = !showBookmarked;

	    // 버튼 상태 업데이트
	    filterButton.classList.toggle("active", showBookmarked);
	    myPostsButton.classList.remove("active"); // 항상 다른 버튼 비활성화

	    // 게시글 불러오기
	    await loadPosts(showBookmarked ? "/log/bookmarked" : "/log/list");
	});
	// 페이지 로드 시 모든 게시글 불러오기
	await loadPosts("/log/list");

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
	// 최대 글자 수 제한
	const maxLength = 300;

	// 게시물 추가 이벤트
	submitButton.addEventListener("click", async () => {
	    const contentInput = document.getElementById("logContentInput");
	    const content = contentInput.value.trim();

		if (!content || content.length > maxLength) {
		    alert(content ? `글자 수는 최대 ${maxLength}자까지 입력 가능합니다. (${content.length}자 입력됨)` : "내용을 입력하세요!");
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
	
});



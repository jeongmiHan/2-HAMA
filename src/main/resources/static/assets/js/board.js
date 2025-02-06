document.addEventListener('DOMContentLoaded', () => {
         
         // 전역 변수 설정
         window.boardId = null; // 게시글 ID를 전역 변수로 설정

         // 댓글 개수 업데이트 함수 (window 객체에 추가)
         window.updateCommentCount = function (boardId) {
             const countElement = document.querySelector('.comment-count'); // 댓글 수를 표시할 요소 선택

             // AJAX 요청
             $.ajax({
                 url: '/reply/count?boardId=' + boardId, // 댓글 개수 조회 API 호출
                 type: 'GET',
                 success: function (data) {
                     countElement.textContent = `(${data})`; // 댓글 개수 업데이트
                 },
                 error: function () {
                     alert('댓글 개수를 불러오는데 실패했습니다.');
                 }
             });
         }

         // 초기 로드 시 댓글 개수 업데이트 호출
         window.addEventListener('DOMContentLoaded', function () {
             // 페이지 구분을 위해 body 태그의 ID 사용 (예: boardRead.html -> <body id="readPage">)
             if (document.body.id === "readPage") { // 'boardRead.html'만 실행되도록 조건 추가

                 // 서버에서 전달된 boardId 값을 설정 (HTML 태그의 data 속성 이용)
                 let boardElement = document.querySelector('.board');
                 if (boardElement) { // .board 클래스가 존재할 경우에만 실행
                     window.boardId = boardElement.getAttribute('data-board-id');

                     // 페이지 로드 시 댓글 개수 업데이트
                     window.updateCommentCount(window.boardId);
                 } else {
                     console.error("'.board' 클래스를 찾을 수 없습니다.");
                 }
             }
         });

         
   
         // 댓글 버튼 클릭 시 댓글 창 열기/닫기
         $(document).ready(function () {
             $('#toggle-comments-btn').click(function () {
                 $('#comment-section').toggle(0); // 댓글 영역 토글
                 if ($('#comment-section').is(':visible')) {
                     getReplies(); // 댓글 목록 불러오기
                 }
             });
         });

         // 댓글 작성 및 대댓글 작성
         window.writeReply = function (parentReplyId = null) {
             let boardId = $('.board').data('board-id'); 
             let rpContent = parentReplyId 
                 ? $(`#child-reply-content-${parentReplyId}`).val().trim() 
                 : $('#reply').val().trim();
             let isSecret = parentReplyId 
                 ? $(`#child-secret-checkbox-${parentReplyId}`).is(':checked') 
                 : $('#secret-reply-checkbox').is(':checked'); // 대댓글 비밀댓글 여부

             if (!rpContent) {
                 alert('댓글 내용을 입력하세요!');
                 return;
             }

             let url = parentReplyId 
                 ? '/reply/child' // 대댓글 등록
                 : `/reply/${boardId}`; // 댓글 등록

             let data = parentReplyId 
                 ? { rpContent, parentReplyId, isSecret } // 대댓글 데이터
                 : { rpContent, isSecret }; // 댓글 데이터

             $.ajax({
                 url: url,
                 type: "POST",
                 contentType: "application/json",
                 data: JSON.stringify(data),
                 success: function (response) {
                     $('#reply').val(''); // 입력란 비우기
                     getReplies(); // 댓글 목록 새로고침
                     window.updateCommentCount(boardId); 
                 },
                 error: function (xhr, status, error) {
                     console.error(error);
                     alert('댓글 등록에 실패했습니다.');
                 }
             });
         };


         // 댓글 및 대댓글 불러오기
         window.getReplies = function() {
             let boardId = $('.board').data('board-id'); // 게시글 ID 가져오기
             $.ajax({
                 url: "/reply/" + boardId,
                 type: "GET",
                 success: function(data) {
                     console.log("댓글 데이터 확인:", data); // 댓글 데이터를 출력
                     renderReplies(data, $('#replies')); // 댓글 렌더링 호출
                 },
                 error: function(xhr, status, error) {
                     console.error("댓글 데이터 불러오기 실패:", error);
                 }
             });
         };

         // 댓글 및 대댓글 불러오기
            window.loadMoreReplies = function () {
                if (window.isLastPage) return;

                let boardId = $('.board').data('board-id');

                $.ajax({
                    url: `/reply/${boardId}?page=${window.currentPage}&size=10`,
                    type: "GET",
                    success: function (data) {
                        if (data.length < 10) {
                            window.isLastPage = true;
                            $('#load-more-btn').hide();
                        } else {
                            $('#load-more-btn').show();
                        }

                        renderReplies(data, $('#replies'), false);
                        window.currentPage++;
                    },
                    error: function (xhr, status, error) {
                        console.error("댓글 데이터 불러오기 실패:", error);
                    }
                });
            };

         // 댓글 및 대댓글 렌더링
         window.renderReplies = function (replies, container) {
             container.html(''); // 초기화

             replies.forEach(reply => {
                 console.log("Reply data: ", reply); // 댓글 데이터를 출력하여 확인

                 let createdTime = new Date(reply.rpCreatedTime);
                 let formattedTime = `${createdTime.getFullYear()}-${String(createdTime.getMonth() + 1).padStart(2, '0')}-${String(createdTime.getDate()).padStart(2, '0')} ${String(createdTime.getHours()).padStart(2, '0')}:${String(createdTime.getMinutes()).padStart(2, '0')}`;

                 let authorName = reply.author || '알 수 없음'; // 작성자 이름
                 let isRpWriter = currentUserId && reply.userId === currentUserId; // 작성자 확인

                 // ✅ 비밀댓글 여부 처리
                 let content = reply.secret
                     ? `<span class="secret-icon">🔒</span> ${reply.accessible ? reply.rpContent : '비밀댓글입니다.'}`
                     : reply.rpContent;

                 // ✅ 좋아요 상태 (localStorage 활용)
                 let storedLike = localStorage.getItem(`like-${reply.replyId}`) || (reply.isLiked ? "❤️" : "🤍");
                 let likeClass = storedLike === "❤️" ? "liked" : "";

                 // ✅ HTML 구조 생성
                 let html = `
                     <div class="reply" id="reply-${reply.replyId}">
                         <div class="metadata">
                             <span class="author">${authorName}</span>
                             ${isRpWriter ? `
                                 <span class="ellipsis" onclick="toggleActions(${reply.replyId})">...</span>
                                 <div class="reply-actions" id="reply-actions-${reply.replyId}" style="display: none;">
                                     <button class="edit" onclick="updateReply(${reply.replyId})">수정</button>
                                     <button class="delete" onclick="removeReply(${reply.replyId})">삭제</button>
                                 </div>
                             ` : ''}
                         </div>
                         <div class="content">${content}</div>
                         <span class="time">${formattedTime}</span>
                         <div class="actions">
                             <button onclick="showReplyBox(${reply.replyId})">답글</button>
                             <button onclick="toggleLike(${reply.replyId})">
                                 <span id="like-icon-${reply.replyId}" class="${likeClass}">
                                     ${storedLike}
                                 </span>
                                 (<span id="like-count-${reply.replyId}">${reply.likeCount}</span>)
                             </button>
                         </div>
                         <div class="child-reply-box" id="child-reply-box-${reply.replyId}" style="display: none;">
                             <textarea id="child-reply-content-${reply.replyId}" placeholder="답글을 입력하세요"></textarea>
                             <input type="checkbox" id="child-secret-checkbox-${reply.replyId}"> 비밀댓글
                             <button onclick="writeReply(${reply.replyId})">등록</button>
                         </div>
                         <div class="child-replies" id="child-replies-${reply.replyId}"></div>
                     </div>`;

                 container.append(html);

                 // ✅ 대댓글 렌더링
                 if (reply.childReplies && reply.childReplies.length > 0) {
                     renderReplies(reply.childReplies, $(`#child-replies-${reply.replyId}`));
                 }
             });
         };



         // ... 버튼 클릭 시 수정/삭제 버튼 토글
         window.toggleActions = function(replyId) {
             const actionsMenu = $(`#reply-actions-${replyId}`);
             $(".reply-actions").not(actionsMenu).hide();
             actionsMenu.toggle();
         };

         // 댓글 수정
         window.updateReply = function(replyId) {
             $(`#reply-actions-${replyId}`).hide();
             const replyElement = document.querySelector(`#reply-${replyId}`);
             const contentElement = replyElement.querySelector(".content");
             const actionsElement = replyElement.querySelector(".actions");

             const existingInput = replyElement.querySelector(".edit-reply-input");
             if (existingInput) return;

             const currentContent = contentElement.textContent.trim();

             const inputContainer = document.createElement("div");
             inputContainer.classList.add("edit-reply-input");

             inputContainer.innerHTML = `
                 <textarea>${currentContent}</textarea>
                 <button onclick="saveUpdatedReply(${replyId}, this)">저장</button>
                 <button onclick="cancelUpdate(${replyId})">취소</button>
             `;

             contentElement.style.display = "none";
             actionsElement.insertAdjacentElement("beforebegin", inputContainer);
         };

         // 수정 저장
         window.saveUpdatedReply = function(replyId, button) {
             const replyElement = document.querySelector(`#reply-${replyId}`);
             const inputContainer = replyElement.querySelector(".edit-reply-input");
             const newContent = inputContainer.querySelector("textarea").value.trim();

             if (!newContent) {
                 alert("내용을 입력하세요!");
                 return;
             }

             fetch(`/reply/${replyId}`, {
                 method: 'PUT',
                 headers: { 'Content-Type': 'application/json' },
                 body: JSON.stringify({ rpContent: newContent }),
             })
             .then(response => {
                 if (!response.ok) throw new Error('수정 실패');
                 const contentElement = replyElement.querySelector(".content");
                 contentElement.textContent = newContent;
                 contentElement.style.display = "block";
                 inputContainer.remove();
             })
             .catch(error => alert(error.message));
         };

         // 수정 취소
         window.cancelUpdate = function(replyId) {
             const replyElement = document.querySelector(`#reply-${replyId}`);
             const contentElement = replyElement.querySelector(".content");
             const inputContainer = replyElement.querySelector(".edit-reply-input");
             contentElement.style.display = "block";
             inputContainer.remove();
         };

         // 댓글 삭제
         window.removeReply = function(replyId) {
             $(`#reply-actions-${replyId}`).hide();
             let check = confirm('삭제하시겠습니까?');
             if (!check) return;

             $.ajax({
                 url: `/reply/${replyId}`,
                 type: "DELETE",
                 success: function() {
                     getReplies();
                  // 댓글 개수 업데이트
                  window.updateCommentCount(window.boardId); // 댓글 수 감소
                 },
                 error: function(xhr, status, error) {
                     console.error(error);
                     alert('삭제 실패!');
                 }
             });
         };
         
         
         window.toggleLike = function(replyId) {
             let likeIcon = document.getElementById(`like-icon-${replyId}`);
             let likeCount = document.getElementById(`like-count-${replyId}`);

             if (!likeIcon || !likeCount) {
                 console.error("Error: Element not found for replyId:", replyId);
                 return;
             }

             fetch(`/reply/${replyId}/like`, {
                 method: "POST",
                 headers: { "Content-Type": "application/json" }
             })
             .then(response => response.json())
             .then(data => {
                 console.log("Server Response:", data);
                 likeIcon.textContent = data.isLiked ? "❤️" : "🤍"; // ✅ 서버 응답 기반으로 좋아요 상태 업데이트
                 likeCount.textContent = data.likeCount;

                 // ✅ 상태를 localStorage에 저장하여 새로고침 후에도 유지
                 localStorage.setItem(`like-${replyId}`, data.isLiked ? "❤️" : "🤍");
             })
             .catch(error => {
                 console.error("Error:", error);
             });
         };
         
         






         // 대댓글 입력 창 토글
         window.showReplyBox = function(replyId) {
             $(`#child-reply-box-${replyId}`).toggle();
         };

   
         
});

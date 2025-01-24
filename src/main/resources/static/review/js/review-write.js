//폼 검증
function validateForm(){
	const reviewContent = document.querySelector('textarea[name="reviewContent"]').value.trim();
	const reviewRating = document.getElementById("reviewRating").value;
	const reviewDate = document.querySelector('input[name=reviewDate]').value.trim();
	
	//리뷰 내용 검증
	if(!reviewContent){
		alert("리뷰 내용을 입력해주세요.");
		return false;
	}
	
	//리뷰 평점 검증
	if(reviewRating === "0"){
		alert("평점을 선택해주세요.");
		return false;
	}
	
	//방문 일자 검증
	if(!reviewDate){
		alert("방문 일자를 선택해주세요.");
		return false;
	}
	
	//방문 일자가 미래인지 확인
	const selectedDate = new Date(reviewDate);
	const today = new Date();

	if(selectedDate > today){
		alert("방문 날짜는 오늘 혹은 과거만 선택할 수 있습니다.")
		return false;
	}
	
	return true;
}

// 평점
document.addEventListener("DOMContentLoaded", function () {
    const stars = document.querySelectorAll(".star");
    const ratingInput = document.getElementById("reviewRating");
    
    stars.forEach((star) => {
        star.addEventListener("click", function () {
            const value = this.getAttribute("data-value");
            ratingInput.value = value; // 평점 값을 히든 필드에 저장
            
            // 모든 아이콘 초기화
            stars.forEach((s) => s.classList.remove("selected"));
            
            // 선택한 아이콘까지 강조
            for (let i = 0; i < value; i++) {
                stars[i].classList.add("selected");
            }
        });
    });
});
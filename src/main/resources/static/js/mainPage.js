document.addEventListener('DOMContentLoaded', () => {
    // 추천 모임 슬라이더 스크립트
    const slider = document.getElementById('recom-slider');
    const prevButton = document.querySelector('.slider-button.prev');
    const nextButton = document.querySelector('.slider-button.next');

    // 슬라이더 버튼 클릭 이벤트
    nextButton.addEventListener('click', () => {
        const cardWidth = slider.querySelector('.slider-card').offsetWidth;
        slider.scrollBy({ left: cardWidth + 16, behavior: 'smooth' });
    });

    prevButton.addEventListener('click', () => {
        const cardWidth = slider.querySelector('.slider-card').offsetWidth;
        slider.scrollBy({ left: -(cardWidth + 16), behavior: 'smooth' });
    });
});
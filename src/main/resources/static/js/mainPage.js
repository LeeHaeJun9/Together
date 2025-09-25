document.addEventListener('DOMContentLoaded', () => {

    function setupSlider(sliderId, prevSelector, nextSelector) {
        const slider = document.getElementById(sliderId);
        const prevButton = document.querySelector(prevSelector);
        const nextButton = document.querySelector(nextSelector);

        // 슬라이더 요소가 존재하지 않으면 함수 종료
        if (!slider || !prevButton || !nextButton) {
            return;
        }

        // 카드 하나와 gap 크기를 더한 스크롤 단위
        // CSS에서 gap: 1rem (16px)으로 가정
        // `.slider-card`는 `.slider-wrapper`의 자식으로 존재합니다.
        const cardElement = slider.querySelector('.slider-card:not(.empty-card)');
        if (!cardElement) {
            // 카드가 하나도 없으면 스크롤할 필요가 없으므로 종료
            return;
        }

        const scrollDistance = cardElement.offsetWidth + 16; // 카드 너비 + 16px (gap)

        // 다음 버튼 클릭 이벤트
        nextButton.addEventListener('click', () => {
            slider.scrollBy({ left: scrollDistance, behavior: 'smooth' });
        });

        // 이전 버튼 클릭 이벤트
        prevButton.addEventListener('click', () => {
            slider.scrollBy({ left: -scrollDistance, behavior: 'smooth' });
        });
    }

    // 1. 추천 카페 슬라이더 초기화
    // HTML에서 `.slider-button.prev`와 `.slider-button.next`를 사용합니다.
    setupSlider('recom-slider', '.slider-button.prev', '.slider-button.next');

    // 2. 💡 추천 중고거래 슬라이더 초기화
    // 중고거래 섹션의 버튼에는 구분을 위해 `.trade-prev`와 `.trade-next` 클래스를 사용한다고 가정합니다.
    // (이전에 HTML에 추가한 `.trade-prev`, `.trade-next` 클래스를 사용)
    setupSlider('trade-slider', '.slider-button.trade-prev', '.slider-button.trade-next');
});
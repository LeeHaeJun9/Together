document.addEventListener('DOMContentLoaded', function() {
    var calendarEl = document.getElementById('calendar');

    // ✅ HTML에 심어놓은 전역 변수를 사용합니다.
    var calendarEvents = calendarEventsData;

    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'ko',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek'
        },
        events: calendarEvents,
        eventClick: function(info) {
            if (info.event.url) {
                window.location.href = info.event.url;
            }
        }
    });

    calendar.render();
});

function confirmDelete() {
    if (confirm("정말로 이 카페를 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다.")) {
        document.getElementById("deleteForm").submit();
    }
}

function confirmLeave() {
    if (confirm("정말로 이 카페를 탈퇴하시겠습니까?")) {
        document.getElementById("leaveForm").submit();
    }
}

window.requestCafeJoin = function() {
    const form = document.getElementById('joinRequestForm');

    if (!form) return;

    if (!confirm('카페 가입을 신청하시겠습니까?')) {
        return;
    }

    // 1. CSRF 토큰 추출
    const url = form.action;
    // Thymeleaf가 렌더링한 hidden input에서 _csrf 값을 찾습니다.
    const csrfTokenInput = form.querySelector('input[name="_csrf"]');
    const csrfToken = csrfTokenInput ? csrfTokenInput.value : null;

    if (!csrfToken) {
        alert('🚨 보안 토큰(_csrf)을 찾을 수 없습니다. 페이지를 새로고침해주세요.');
        return;
    }

    // 2. 요청 본문 준비: application/x-www-form-urlencoded 형식
    const requestBody = new URLSearchParams();
    requestBody.append('_csrf', csrfToken); // 💡 CSRF 토큰을 본문에 추가 (필수)

    // 3. fetch API를 사용한 비동기 POST 요청
    fetch(url, {
        method: 'POST',
        headers: {
            // 💡 폼 제출 방식을 모방하는 Content-Type
            'Content-Type': 'application/x-www-form-urlencoded',
            // 💡 X-CSRF-TOKEN 헤더에도 토큰 추가 (이중 보장)
            'X-CSRF-TOKEN': csrfToken
        },
        body: requestBody.toString() // URLSearchParams 객체를 문자열로 변환
    })
        .then(response => {
            // HTTP 상태 코드 확인
            if (response.ok) {
                return response.text();
            } else if (response.status === 403) {
                throw new Error('권한이 부족하거나 CSRF 토큰이 유효하지 않습니다. (403)');
            } else if (response.status === 409) {
                return response.text().then(msg => { throw new Error(msg); });
            }
            throw new Error('가입 신청 처리 중 알 수 없는 오류가 발생했습니다. (HTTP Code: ' + response.status + ')');
        })
        .then(message => {
            alert(`✅ ${message}`);
            window.location.reload();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('🚨 오류 발생: ' + error.message);
        });
}
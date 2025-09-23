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
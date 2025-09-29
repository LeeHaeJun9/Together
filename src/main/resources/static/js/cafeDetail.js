document.addEventListener('DOMContentLoaded', function() {
    // =========================
    // FullCalendar 초기화
    // =========================
    var calendarEl = document.getElementById('calendar');
    if(calendarEl) {
        var calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek'
            },
            events: calendarEventsData,
            eventClick: function(info) {
                if (info.event.url) window.location.href = info.event.url;
            },
            eventDidMount: function(info) {
                var now = new Date();
                var eventEnd = info.event.end || info.event.start;
                if (eventEnd < now) {
                    info.el.style.backgroundColor = '#d3d3d3';
                    info.el.style.color = '#777';
                    info.el.title = '(지난 일정) ' + info.event.title;
                }
            }
        });
        calendar.render();
    }

    // =========================
    // 모달 관련 함수
    // =========================
    function showInfoModal(message) {
        const modal = document.getElementById('infoModal');
        document.getElementById('infoModalMessage').textContent = message;
        modal.style.display = 'flex';
    }

    window.closeInfoModal = function() {
        document.getElementById('infoModal').style.display = 'none';
    }

    function showConfirmModal(message, onConfirm) {
        const modal = document.getElementById('confirmModal');
        const yesBtn = document.getElementById('confirmModalYesBtn');
        const noBtn = document.getElementById('confirmModalNoBtn');

        document.getElementById('confirmModalMessage').textContent = message;

        // 기존 이벤트 제거 후 새로 등록
        const newYesBtn = yesBtn.cloneNode(true);
        yesBtn.parentNode.replaceChild(newYesBtn, yesBtn);

        const newNoBtn = noBtn.cloneNode(true);
        noBtn.parentNode.replaceChild(newNoBtn, noBtn);

        newYesBtn.addEventListener('click', () => {
            onConfirm();
            closeConfirmModal();
        });

        newNoBtn.addEventListener('click', () => {
            closeConfirmModal();
        });

        modal.style.display = 'flex';
    }

    window.closeConfirmModal = function() {
        document.getElementById('confirmModal').style.display = 'none';
    }

    // =========================
    // 공통 fetch 처리
    // =========================
    async function handleFormSubmit(formId, successCallback, failCallback) {
        const form = document.getElementById(formId);
        if (!form) {
            showInfoModal("오류: 폼을 찾을 수 없습니다.");
            return;
        }

        const url = form.action;
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        try {
            const res = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken
                },
                body: `_csrf=${csrfToken}`
            });

            const msg = await res.text();

            if (!res.ok) throw new Error(msg);

            successCallback(msg, form);

        } catch (err) {
            failCallback(err);
        }
    }

    // =========================
    // 가입신청 / 탈퇴 / 삭제
    // =========================
    window.confirmJoin = function() {
        showConfirmModal('카페 가입을 신청하시겠습니까?', () => {
            handleFormSubmit('joinRequestForm',
                (msg, form) => {
                    showInfoModal(`✅ ${msg}`);
                    const button = form.querySelector('button');
                    button.textContent = '신청 완료';
                    button.disabled = true;
                },
                (err) => showInfoModal(`🚨 오류: ${err.message}`)
            );
        });
    }

    window.confirmLeave = function() {
        showConfirmModal('정말 카페를 탈퇴하시겠습니까?', () => {
            handleFormSubmit('leaveForm',
                (msg) => {
                    showInfoModal(`✅ ${msg}`);
                    window.location.href = '/cafe/list';
                },
                (err) => showInfoModal(`🚨 오류: ${err.message}`)
            );
        });
    }

    window.confirmDelete = function() {
        showConfirmModal('정말 카페를 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다.', () => {
            handleFormSubmit('deleteForm',
                (msg) => {
                    showInfoModal(`✅ ${msg}`);
                    window.location.href = '/cafe/list';
                },
                (err) => showInfoModal(`🚨 오류: ${err.message}`)
            );
        });
    }

});

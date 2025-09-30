// /static/js/report-quick.js

// ✅ 전역 가드: 스크립트가 중복 로드돼도 한 번만 바인딩
if (!window.__REPORT_QUICK_BOUND__) {
    window.__REPORT_QUICK_BOUND__ = true;

    // CSRF 토큰 가져오기 (meta 우선, 없으면 쿠키)
    function getCookie(name) {
        const m = document.cookie.match(new RegExp('(?:^|; )' + name.replace(/([.$?*|{}()\[\]\\\/\+^])/g, '\\$1') + '=([^;]*)'));
        return m ? decodeURIComponent(m[1]) : null;
    }
    function getCsrf() {
        const metaToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const metaHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        if (metaToken && metaHeader) return { headerName: metaHeader, token: metaToken };

        // SecurityConfig에서 CookieCsrfTokenRepository 사용: 쿠키명 기본 변경하셨죠? (TOGETHER-XSRF)
        const cookieToken = getCookie('TOGETHER-XSRF');
        if (cookieToken) return { headerName: 'X-XSRF-TOKEN', token: cookieToken };

        return null;
    }

    // ✅ 한 번 클릭 시 동일 버튼에서 중복 실행을 막는 락
    function lockOnce(btn) {
        if (btn.dataset.reporting === '1') return false;
        btn.dataset.reporting = '1';
        // 다음 틱에 락 해제 (이벤트 루프 1회 후) — 같은 클릭에서만 중복 방지
        setTimeout(() => { delete btn.dataset.reporting; }, 0);
        return true;
    }

    async function createReport(type, targetId) {
        const csrf = getCsrf();
        const headers = { 'Accept': 'application/json', 'Content-Type': 'application/json' };
        if (csrf) headers[csrf.headerName] = csrf.token;

        // 간단 사유 입력(알럿 2번 방지 핵심은 이벤트 쪽이지만, confirm/prompt는 여기서 1회만)
        const reason = window.prompt('신고 사유를 입력해 주세요 (선택)') || '';

        const res = await fetch('/report', {
            method: 'POST',
            headers,
            credentials: 'same-origin',
            body: JSON.stringify({ reportType: String(type).toUpperCase(), targetId: Number(targetId), reason })
        });

        if (res.status === 401) { window.alert('로그인이 필요합니다.'); return; }
        if (res.status === 403) { window.alert('권한이 없습니다.'); return; }

        if (!res.ok) {
            // 서버에서 메시지 내려주면 표시
            try {
                const data = await res.json();
                window.alert(data?.message || '신고 처리 중 오류가 발생했습니다.');
            } catch {
                window.alert('신고 처리 중 오류가 발생했습니다.');
            }
            return;
        }

        window.alert('신고가 접수되었습니다.');
    }

    // ✅ 단 하나의 “위임” 리스너. 중복 등록 방지용으로 전역 가드 + 전파 차단을 같이 사용
    const handler = function (e) {
        // 버튼 또는 아이콘(<i>) 클릭 모두 허용
        const btn = e.target.closest('.report-btn, .report-btn-v2');
        if (!btn) return;

        // 같은 클릭에 달린 다른 리스너들이 또 실행되지 않도록 방지
        e.preventDefault();
        e.stopPropagation();
        e.stopImmediatePropagation();

        if (!lockOnce(btn)) return; // 같은 버튼에서 2중 실행 방지

        const type = btn.getAttribute('data-report-type');
        const targetId = btn.getAttribute('data-target-id');

        if (!type || !targetId) {
            window.alert('신고 정보가 올바르지 않습니다.');
            return;
        }

        createReport(type, targetId).catch(() => {
            window.alert('신고 처리 중 오류가 발생했습니다.');
        });
    };

    // ⚠️ 다른 스크립트가 버블 단계에 또 리스너를 붙여놨을 수도 있어서,
    //    “버블 단계”에서만 처리하되 stopImmediatePropagation으로 동일 단계 중복만 차단.
    document.addEventListener('click', handler, false);

    // 디버그용
    // console.debug('[report-quick] listener bound once');
}

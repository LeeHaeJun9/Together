(() => {
    const MAX = 10;
    const tile = document.getElementById('uploadTile');
    const input = document.getElementById('fileInput');
    const list  = document.getElementById('imageList');
    const form  = document.getElementById('tradeForm');
    const counter = document.getElementById('count');

    let files = [];           // 실제 전송할 File 배열
    let blobUrls = [];        // revoke를 위한 URL 목록

    function updateCount(){ counter.textContent = String(files.length); }

    function addFiles(fileList) {
        const incoming = Array.from(fileList).filter(f => /^image\//.test(f.type));
        const room = MAX - files.length;
        if (incoming.length > room) {
            alert(`이미지는 최대 ${MAX}장까지 가능합니다. (이번에 ${incoming.length}장 중 ${room}장만 추가)`);
        }
        files = files.concat(incoming.slice(0, Math.max(0, room)));
        render();
    }

    function clearBlobUrls() {
        blobUrls.forEach(url => URL.revokeObjectURL(url));
        blobUrls = [];
    }

    function render() {
        // 이전 미리보기 URL 정리
        clearBlobUrls();
        list.innerHTML = '';

        files.forEach((f, idx) => {
            const item = document.createElement('div');
            item.className = 'image-item';
            item.dataset.idx = String(idx);

            const img = document.createElement('img');
            const url = URL.createObjectURL(f);
            blobUrls.push(url);
            img.src = url;

            item.appendChild(img);
            list.appendChild(item);
        });
        updateCount();
    }

    // 업로드 트리거(클릭)
    tile.addEventListener('click', () => input.click());

    // 드래그 앤 드롭 UX
    ['dragenter','dragover'].forEach(ev =>
        tile.addEventListener(ev, e => { e.preventDefault(); tile.classList.add('dragover'); })
    );
    ['dragleave','drop'].forEach(ev =>
        tile.addEventListener(ev, e => { e.preventDefault(); tile.classList.remove('dragover'); })
    );
    tile.addEventListener('drop', e => addFiles(e.dataTransfer.files));

    // 파일 선택
    input.addEventListener('change', e => {
        addFiles(e.target.files);
        input.value = ''; // 같은 파일 재선택 허용
    });

    // 썸네일 클릭 → 삭제
    list.addEventListener('click', e => {
        const item = e.target.closest('.image-item');
        if (!item) return;
        const idx = Number(item.dataset.idx);
        files.splice(idx, 1);
        render();
    });

    // 제출: FormData로 파일 + 다른 필드 함께 전송
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const fd = new FormData(form);
        files.forEach(f => fd.append('images', f)); // 서버 파라미터명: images

        try {
            const resp = await fetch(form.action, { method: 'POST', body: fd });
            if (!resp.ok) throw new Error('upload failed');

            // 서버가 이동할 URL 문자열을 내려주는 경우 (예: "/trade/read?id=123")
            const text = await resp.text();
            if (/^(\/|https?:\/\/)/.test(text)) {
                location.href = text;
            } else {
                location.href = '/trade/list';
            }
        } catch (err) {
            console.error(err);
            alert('등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } finally {
            clearBlobUrls();
        }
    });

    // 페이지 이탈 시 URL 정리
    window.addEventListener('beforeunload', clearBlobUrls);
})();

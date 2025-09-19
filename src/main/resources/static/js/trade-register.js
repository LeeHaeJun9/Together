document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('fileInput');
    const dropzone  = document.getElementById('dropzone');
    const thumbs    = document.getElementById('thumbs');
    const counter   = document.getElementById('count'); // 있으면 갱신, 없으면 무시

    if (!fileInput || !dropzone || !thumbs) return;

    let filesBuffer = [];

    // 라벨 for=fileInput 로 클릭 트리거가 기본동작이지만,
    // 레이아웃 변형 대비해서 위임도 한 번 더 건다.
    document.addEventListener('click', (e) => {
        if (e.target.closest('#uploadTile')) {
            e.preventDefault();
            fileInput.click();
        }
    });

    // 드래그 앤 드롭
    ['dragenter','dragover'].forEach(evt => {
        dropzone.addEventListener(evt, (e) => {
            e.preventDefault(); e.stopPropagation();
            dropzone.classList.add('dragover');
        });
    });
    ['dragleave','drop'].forEach(evt => {
        dropzone.addEventListener(evt, (e) => {
            e.preventDefault(); e.stopPropagation();
            dropzone.classList.remove('dragover');
        });
    });
    dropzone.addEventListener('drop', (e) => {
        const dropped = Array.from(e.dataTransfer?.files || []);
        addFiles(dropped);
    });

    fileInput.addEventListener('change', (e) => {
        addFiles(Array.from(e.target.files || []));
        fileInput.value = ''; // input은 비워두고 버퍼로만 관리
    });

    function addFiles(incoming) {
        if (!incoming.length) return;
        const images = incoming.filter(f => f.type.startsWith('image/'));
        const room = 10 - filesBuffer.length;
        const toAdd = images.slice(0, Math.max(0, room));
        if (images.length > room) alert('최대 10장까지만 등록 가능합니다.');
        filesBuffer.push(...toAdd);
        renderThumbs();
        syncToInput();
    }

    function removeAt(idx) {
        filesBuffer.splice(idx, 1);
        renderThumbs();
        syncToInput();
    }

    function renderThumbs() {
        thumbs.innerHTML = '';
        if (counter) counter.textContent = String(filesBuffer.length);
        filesBuffer.forEach((file, i) => {
            const url = URL.createObjectURL(file);
            const wrap = document.createElement('div');
            wrap.className = 'thumb';

            const img = document.createElement('img');
            img.src = url;

            if (i === 0) {
                const badge = document.createElement('span');
                badge.className = 'badge text-bg-primary badge-top';
                badge.textContent = '대표';
                wrap.appendChild(badge);
            }

            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'remove';
            btn.innerHTML = '&times;';
            btn.addEventListener('click', () => removeAt(i));

            wrap.appendChild(img);
            wrap.appendChild(btn);
            thumbs.appendChild(wrap);
        });
    }

    function syncToInput() {
        if (typeof DataTransfer === 'undefined') return;
        const dt = new DataTransfer();
        filesBuffer.forEach(f => dt.items.add(f));
        fileInput.files = dt.files;
    }
});
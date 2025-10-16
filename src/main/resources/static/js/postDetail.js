if (surveyId) {
    async function fetchSurveyOptions() {
        try {
            const response = await fetch(`/api/cafe/${cafeId}/posts/${postId}/demandSurvey/${surveyId}/options`);
            if (!response.ok) throw new Error('옵션을 가져오는 중 오류 발생');
            return await response.json(); // ["초콜릿", "사탕", "쿠키"] 형식 예상
        } catch (err) {
            console.error(err);
            return [];
        }
    }

    async function fetchVoteResults() {
        try {
            const response = await fetch(`/api/cafe/${cafeId}/posts/${postId}/demandSurvey/${surveyId}/votes`);
            if (!response.ok) throw new Error('투표 결과를 가져오는 데 실패했습니다.');
            return await response.json(); // [{option: "초콜릿", count: 2, voterNicknames: [...]}, ...]
        } catch (err) {
            console.error(err);
            return [];
        }
    }

    async function renderVoteUI() {
        const now = new Date();
        const deadlineDate = new Date(deadline);
        const isExpired = now > deadlineDate;

        const hasVotedResp = await fetch(`/api/cafe/${cafeId}/posts/${postId}/demandSurvey/${surveyId}/hasVoted?userId=${loggedInUserId}`);
        const hasVoted = await hasVotedResp.json();

        if (isExpired || hasVoted) {
            showVoteResults();
        } else {
            showVoteForm();
        }
    }

    async function showVoteForm() {
        const options = await fetchSurveyOptions();
        if (options.length === 0) {
            voteContainer.innerHTML = `<div class="alert alert-warning">옵션이 없습니다.</div>`;
            return;
        }

        let html = `<div class="vote-form-options mb-3">`;
        options.forEach((option, idx) => {
            html += `
            <div class="form-check">
                <input class="form-check-input" type="radio" id="vote-option-${idx}" name="voteOption" value="${option}">
                <label class="form-check-label" for="vote-option-${idx}">${option}</label>
            </div>`;
        });
        html += `</div><button id="submitVoteBtn" class="btn btn-custom-primary">투표하기</button>`;

        voteContainer.innerHTML = html;

        document.getElementById('submitVoteBtn').addEventListener('click', async () => {
            const selectedOption = document.querySelector('input[name="voteOption"]:checked');
            if (!selectedOption) {
                alert("옵션을 선택해주세요.");
                return;
            }

            try {
                const response = await fetch(`/api/cafe/${cafeId}/posts/${postId}/demandSurvey/${surveyId}/votes`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    },
                    body: JSON.stringify({ option: selectedOption.value })
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(errorText);
                }

                alert("투표가 완료되었습니다.");
                renderVoteUI();
            } catch (err) {
                alert("투표 실패: " + err.message);
            }
        });
    }

    async function showVoteResults() {
        const results = await fetchVoteResults();
        if (results.length === 0) {
            voteContainer.innerHTML = `<div class="alert alert-info">투표 결과가 없습니다.</div>`;
            return;
        }

        let html = '<h5>투표 결과</h5><ul class="list-group">';
        results.forEach(result => {
            let voterBadges = '';
            if (result.voterNicknames && result.voterNicknames.length > 0) {
                voterBadges = result.voterNicknames.map(nick => `<span class="badge bg-secondary me-1">${nick}</span>`).join('');
            }

            html += `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <div>
                    <span class="fw-bold">${result.option}</span>
                    <div class="mt-1">${voterBadges}</div>
                </div>
                <span class="badge bg-primary rounded-pill">${result.count}표</span>
            </li>`;
        });
        html += '</ul>';
        voteContainer.innerHTML = html;
    }

    renderVoteUI();
}

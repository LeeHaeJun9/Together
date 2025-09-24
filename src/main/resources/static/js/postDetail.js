
const modal = document.getElementById('editCommentModal');
const closeBtn = document.querySelector('.close-button');
const editForm = document.getElementById('editCommentForm');
const commentIdInput = document.getElementById('editCommentId');
const contentTextarea = document.getElementById('editCommentContent');
const editButtons = document.querySelectorAll('.edit-comment-btn');
const voteContainer = document.getElementById('voteContainer');


if (surveyId) {
    // 투표 또는 결과 UI를 렌더링하는 메인 함수
    function renderVoteUI() {
        const now = new Date();
        const deadlineDate = new Date(deadline);
        const isExpired = now > deadlineDate;

        fetch(`/api/cafe/${cafeId}/posts/${postId}/demandSurvey/${surveyId}/hasVoted?userId=${loggedInUserId}`)
            .then(response => response.json())
            .then(hasVoted => {
                if (isExpired || hasVoted) {
                    showVoteResults();
                } else {
                    showVoteForm();
                }
            })
            .catch(error => {
                console.error('투표 참여 여부 확인 실패:', error);
                voteContainer.innerHTML = `<div class="alert alert-danger">투표 정보를 불러오는 중 오류가 발생했습니다.</div>`;
            });
    }

    // 투표 폼 UI를 렌더링하는 함수
    function showVoteForm() {
        let html = `
        <div class="vote-form-options mb-3">
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" id="vote-option-1" name="voteOption" value="찬성">
            <label class="form-check-label" for="vote-option-1">찬성</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" id="vote-option-2" name="voteOption" value="반대">
            <label class="form-check-label" for="vote-option-2">반대</label>
          </div>
        </div>
        <button id="submitVoteBtn" class="btn btn-custom-primary">투표하기</button>
      `;
        voteContainer.innerHTML = html;

        document.getElementById('submitVoteBtn').addEventListener('click', () => {
            const selectedOption = document.querySelector('input[name="voteOption"]:checked');
            if (selectedOption) {
                submitVote(selectedOption.value);
            } else {
                alert("옵션을 선택해주세요.");
            }
        });
    }

    async function showVoteResults() {
        try {
            const response = await fetch(`/api/cafe/${cafeId}/posts/${postId}/demandSurvey/${surveyId}/votes`);
            if (!response.ok) {
                throw new Error('투표 결과를 가져오는 데 실패했습니다.');
            }
            const results = await response.json();

            let html = '<h5>투표 결과</h5><ul class="list-group">';
            results.forEach(result => {
                if (result.voterNicknames && result.voterNicknames.length > 0) {
                    const nicknamesHtml = result.voterNicknames.map(nickname =>
                        `<span class="badge bg-secondary me-1">${nickname}</span>`
                    ).join('');

                    html += `<li class="list-group-item">
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="fw-bold">${result.option}</span>
                                <span class="badge bg-primary rounded-pill">${result.count}표</span>
                            </div>
                            <div class="mt-2 voter-list">${nicknamesHtml}</div>
                        </li>`;
                } else {
                    html += `<li class="list-group-item d-flex justify-content-between align-items-center">
                            ${result.option}
                            <span class="badge bg-primary rounded-pill">${result.count}표</span>
                        </li>`;
                }
            });
            html += '</ul>';
            voteContainer.innerHTML = html;
        } catch (error) {
            voteContainer.innerHTML = `<div class="alert alert-danger mt-3">오류: ${error.message}</div>`;
        }
    }

    async function submitVote(option) {
        if (!loggedInUserId) {
            alert("로그인 후 이용 가능합니다.");
            return;
        }
        try {
            const response = await fetch(`/api/cafe/${cafeId}/posts/${postId}/demandSurvey/${surveyId}/votes`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken // HTML에서 선언된 변수 사용
                },
                body: JSON.stringify({ option: option})
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText);
            }
            alert("투표가 완료되었습니다.");
            renderVoteUI();
        } catch (error) {
            alert("투표 실패: " + error.message);
        }
    }

    renderVoteUI();
}

editButtons.forEach(button => {
    button.addEventListener('click', (event) => {
        const commentId = event.target.dataset.commentId;
        const content = event.target.dataset.content;

        commentIdInput.value = commentId;
        contentTextarea.value = content;
        modal.style.display = 'block';
    });
});

closeBtn.addEventListener('click', () => {
    modal.style.display = 'none';
});

window.addEventListener('click', (event) => {
    if (event.target == modal) {
        modal.style.display = 'none';
    }
});

editForm.addEventListener('submit', (event) => {
    event.preventDefault();

    const commentId = commentIdInput.value;
    const newContent = contentTextarea.value;

    fetch(`/cafe/${cafeId}/posts/${postId}/comments/${commentId}/update`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({
            id: commentId,
            content: newContent
        })
    })
        .then(response => {
            if (!response.ok) {
                // 403 Forbidden 오류에 대한 상세 메시지 추가
                if (response.status === 403) {
                    alert("수정 실패: CSRF 토큰이 유효하지 않습니다. 페이지를 새로고침하고 다시 시도해주세요.");
                }
                return response.text().then(errorText => {
                    throw new Error(errorText);
                });
            }
            return response.text();
        })
        .then(message => {
            alert(message);
            location.reload();
        })
        .catch(error => {
            console.error("수정 실패:", error);
            alert("수정 실패: " + error.message);
        });
});
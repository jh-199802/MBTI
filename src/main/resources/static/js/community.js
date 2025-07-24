// 커뮤니티 페이지 JavaScript
let currentSort = 'recent';
let commentPage = 1;
let isLoading = false;
let currentUser = null;

/**
 * 커뮤니티 페이지 초기화
 */
function initializeCommunityPage() {
    checkUserLogin();
    setupSortButtons();
    setupMbtiFilters();
    loadComments(currentSort);
}

/**
 * 사용자 로그인 상태 확인
 */
async function checkUserLogin() {
    try {
        const response = await fetch('/user/api/current');
        const data = await response.json();
        
        if (data.loggedIn) {
            currentUser = data.user;
            showLoggedInUI();
        } else {
            showGuestUI();
        }
    } catch (error) {
        console.error('사용자 상태 확인 오류:', error);
        showGuestUI();
    }
}

/**
 * 로그인한 사용자 UI 표시
 */
function showLoggedInUI() {
    const commentForm = document.getElementById('commentForm');
    const loginPrompt = document.getElementById('loginPrompt');
    
    if (commentForm) {
        commentForm.style.display = 'block';
        
        // 사용자 정보가 있으면 닉네임 미리 채우기
        const nicknameInput = document.getElementById('nicknameInput');
        if (nicknameInput && currentUser.nickname) {
            nicknameInput.value = currentUser.nickname;
        }
        
        // 사용자의 MBTI가 있으면 미리 선택
        const mbtiSelect = document.getElementById('mbtiSelect');
        if (mbtiSelect && currentUser.mbtiType) {
            mbtiSelect.value = currentUser.mbtiType;
            updateMbtiSelectStyle(mbtiSelect);
        }
        
        setupCommentForm();
        setupCharCounter();
    }
    
    if (loginPrompt) {
        loginPrompt.style.display = 'none';
    }
}

/**
 * 게스트 사용자 UI 표시
 */
function showGuestUI() {
    const commentForm = document.getElementById('commentForm');
    const loginPrompt = document.getElementById('loginPrompt');
    
    if (commentForm) {
        commentForm.style.display = 'none';
    }
    
    if (loginPrompt) {
        loginPrompt.style.display = 'block';
    } else {
        // 로그인 프롬프트가 없으면 동적으로 생성
        createLoginPrompt();
    }
}

/**
 * 로그인 프롬프트 생성
 */
function createLoginPrompt() {
    const commentSection = document.querySelector('.comment-form-section');
    if (!commentSection) return;
    
    const loginPrompt = document.createElement('div');
    loginPrompt.id = 'loginPrompt';
    loginPrompt.className = 'login-prompt';
    loginPrompt.innerHTML = `
        <div class="login-prompt-content">
            <div class="login-prompt-icon">🔐</div>
            <h3>로그인이 필요합니다</h3>
            <p>커뮤니티에 댓글을 작성하려면 로그인해주세요!</p>
            <div class="login-prompt-buttons">
                <a href="/user/login?redirectUrl=${encodeURIComponent(window.location.pathname)}" class="btn-login">
                    <i class="fas fa-sign-in-alt"></i>
                    로그인
                </a>
                <a href="/user/register" class="btn-register">
                    <i class="fas fa-user-plus"></i>
                    회원가입
                </a>
            </div>
            <p class="login-prompt-note">
                <i class="fas fa-info-circle"></i>
                로그인 없이도 댓글은 볼 수 있어요!
            </p>
        </div>
    `;
    
    commentSection.appendChild(loginPrompt);
}

/**
 * 댓글 폼 설정
 */
function setupCommentForm() {
    const commentForm = document.getElementById('commentForm');
    if (!commentForm) return;

    commentForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        // 로그인 상태 재확인
        if (!currentUser) {
            showToast('로그인이 필요합니다!', 'error');
            setTimeout(() => {
                window.location.href = `/user/login?redirectUrl=${encodeURIComponent(window.location.pathname)}`;
            }, 1500);
            return;
        }
        
        submitComment();
    });

    // MBTI 선택 시 배지 색상 업데이트
    const mbtiSelect = document.getElementById('mbtiSelect');
    if (mbtiSelect) {
        mbtiSelect.addEventListener('change', function() {
            updateMbtiSelectStyle(this);
        });
    }
}

/**
 * 글자 수 카운터 설정
 */
function setupCharCounter() {
    const commentText = document.getElementById('commentText');
    const charCount = document.getElementById('charCount');
    
    if (commentText && charCount) {
        commentText.addEventListener('input', function() {
            const count = this.value.length;
            charCount.textContent = count;
            
            // 글자 수에 따른 스타일 변경
            if (count > 800) {
                charCount.style.color = '#ef4444';
            } else if (count > 600) {
                charCount.style.color = '#f59e0b';
            } else {
                charCount.style.color = '#6b7280';
            }
        });
    }
}

/**
 * 정렬 버튼 설정
 */
function setupSortButtons() {
    const sortButtons = document.querySelectorAll('.sort-btn');
    
    sortButtons.forEach(button => {
        button.addEventListener('click', function() {
            const sortType = this.dataset.sort;
            if (sortType !== currentSort) {
                // 버튼 활성화 상태 변경
                sortButtons.forEach(btn => btn.classList.remove('active'));
                this.classList.add('active');
                
                // 정렬 변경
                currentSort = sortType;
                commentPage = 1;
                loadComments(sortType);
            }
        });
    });
}

/**
 * MBTI 필터 설정
 */
function setupMbtiFilters() {
    const filterButtons = document.querySelectorAll('.mbti-filter-btn');
    
    filterButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            const mbtiType = this.dataset.mbti;
            if (mbtiType) {
                showMbtiTooltip(this, mbtiType);
            }
        });
        
        button.addEventListener('mouseleave', function() {
            hideMbtiTooltip();
        });
    });
}

/**
 * 댓글 로드
 */
function loadComments(sortType = 'recent') {
    if (isLoading) return;
    
    isLoading = true;
    showLoadingSpinner();
    
    const selectedMbti = window.selectedMbti;
    let url = '/comments/api/';
    
    if (selectedMbti) {
        url += `mbti/${selectedMbti}`;
    } else if (sortType === 'popular') {
        url += 'popular?limit=20';
    } else {
        url += 'recent?limit=20';
    }
    
    fetch(url)
        .then(response => response.json())
        .then(comments => {
            displayComments(comments);
            hideLoadingSpinner();
        })
        .catch(error => {
            console.error('댓글 로드 실패:', error);
            showToast('댓글을 불러오는데 실패했습니다.', 'error');
            hideLoadingSpinner();
        })
        .finally(() => {
            isLoading = false;
        });
}

/**
 * 더 많은 댓글 로드
 */
function loadMoreComments() {
    if (isLoading) return;
    
    commentPage++;
    
    // 추가 댓글 로드 로직 (페이징)
    // 현재는 기본 구현만
    loadComments(currentSort);
}

/**
 * 댓글 표시
 */
function displayComments(comments) {
    const commentsList = document.getElementById('commentsList');
    if (!commentsList) return;
    
    if (!comments || comments.length === 0) {
        commentsList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-comment-slash"></i>
                <h3>댓글이 없습니다</h3>
                <p>첫 번째 댓글을 작성해보세요!</p>
            </div>
        `;
        return;
    }
    
    let commentsHtml = '';
    comments.forEach(comment => {
        commentsHtml += createCommentHtml(comment);
    });
    
    commentsList.innerHTML = commentsHtml;
    
    // 댓글 카드에 이벤트 리스너 추가
    attachCommentEventListeners();
}

/**
 * 댓글 HTML 생성
 */
function createCommentHtml(comment) {
    const formattedDate = formatDateTime(comment.createdAt);
    const nickname = comment.nickname || '익명';
    
    return `
        <div class="comment-card" data-comment-id="${comment.commentId}">
            <div class="comment-header">
                <div class="user-info">
                    <span class="mbti-badge" data-mbti="${comment.mbtiType}">${comment.mbtiType}</span>
                    <span class="nickname">${nickname}</span>
                </div>
                <div class="comment-actions">
                    <time>${formattedDate}</time>
                    <div class="action-buttons">
                        <button class="like-btn" onclick="likeComment(${comment.commentId})">
                            <i class="fas fa-heart"></i>
                            <span>${comment.likesCount || 0}</span>
                        </button>
                        <button class="reply-btn" onclick="replyToComment(${comment.commentId})">
                            <i class="fas fa-reply"></i>
                        </button>
                        <button class="more-btn" onclick="showCommentMenu(${comment.commentId})">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                    </div>
                </div>
            </div>
            <div class="comment-content">
                <p>${escapeHtml(comment.commentText)}</p>
            </div>
            <div class="comment-footer">
                <span class="comment-time">${formatRelativeTime(comment.createdAt)}</span>
            </div>
        </div>
    `;
}

/**
 * 댓글 이벤트 리스너 추가
 */
function attachCommentEventListeners() {
    const commentCards = document.querySelectorAll('.comment-card');
    
    commentCards.forEach(card => {
        // 댓글 카드 호버 효과
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.1)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.05)';
        });
    });
}

/**
 * 댓글 작성 제출
 */
function submitComment() {
    const form = document.getElementById('commentForm');
    if (!form) return;
    
    // 폼 데이터 수집
    const mbtiType = document.getElementById('mbtiSelect').value;
    const nickname = document.getElementById('nicknameInput').value;
    const commentText = document.getElementById('commentText').value;
    
    const commentData = {
        resultId: null, // 일반 커뮤니티 댓글은 resultId가 null
        mbtiType: mbtiType,
        nickname: nickname.trim() || null,
        commentText: commentText.trim()
    };
    
    console.log('Sending comment data:', commentData); // 디버깅용
    
    // 유효성 검사
    if (!commentData.mbtiType) {
        showToast('MBTI 타입을 선택해주세요.', 'error');
        return;
    }
    
    if (!commentData.commentText || commentData.commentText.length < 3) {
        showToast('댓글은 3자 이상 입력해주세요.', 'error');
        return;
    }
    
    if (commentData.commentText.length > 1000) {
        showToast('댓글은 1000자를 초과할 수 없습니다.', 'error');
        return;
    }
    
    showLoadingSpinner();
    
    fetch('/comments/api/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(commentData)
    })
    .then(response => {
        console.log('Response status:', response.status); // 디버깅용
        return response.json();
    })
    .then(data => {
        console.log('Response data:', data); // 디버깅용
        if (data.success) {
            showToast('댓글이 작성되었습니다! 💬');
            form.reset();
            document.getElementById('charCount').textContent = '0';
            
            // 댓글 목록 새로고침
            setTimeout(() => {
                loadComments(currentSort);
            }, 500);
        } else {
            showToast(data.message || '댓글 작성에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('댓글 작성 실패:', error);
        showToast('댓글 작성 중 오류가 발생했습니다.', 'error');
    })
    .finally(() => {
        hideLoadingSpinner();
    });
}

/**
 * 댓글 좋아요
 */
function likeComment(commentId) {
    if (!commentId) return;
    
    fetch(`/comments/api/${commentId}/like`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 좋아요 수 업데이트
            const likeBtn = document.querySelector(`[data-comment-id="${commentId}"] .like-btn span`);
            if (likeBtn) {
                likeBtn.textContent = data.likesCount;
            }
            
            // 하트 애니메이션
            const heartIcon = document.querySelector(`[data-comment-id="${commentId}"] .like-btn i`);
            if (heartIcon) {
                heartIcon.classList.add('animate-heart');
                setTimeout(() => {
                    heartIcon.classList.remove('animate-heart');
                }, 600);
            }
            
            showToast('좋아요! ❤️', 'success');
        } else {
            showToast(data.message || '좋아요 처리에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('좋아요 실패:', error);
        showToast('좋아요 처리 중 오류가 발생했습니다.', 'error');
    });
}

/**
 * 댓글 답글
 */
function replyToComment(commentId) {
    // 답글 기능 구현 (향후 확장)
    showToast('답글 기능은 곧 추가될 예정입니다! 🚀', 'info');
}

/**
 * 댓글 메뉴 표시
 */
function showCommentMenu(commentId) {
    // 댓글 메뉴 (수정/삭제) 기능 구현 (향후 확장)
    showToast('댓글 관리 기능은 곧 추가될 예정입니다! ⚙️', 'info');
}

/**
 * MBTI 선택 스타일 업데이트
 */
function updateMbtiSelectStyle(selectElement) {
    const selectedValue = selectElement.value;
    if (selectedValue) {
        selectElement.style.background = getMbtiGradient(selectedValue);
        selectElement.style.color = '#ffffff';
    } else {
        selectElement.style.background = '';
        selectElement.style.color = '';
    }
}

/**
 * MBTI 툴팁 표시
 */
function showMbtiTooltip(element, mbtiType) {
    const tooltip = document.createElement('div');
    tooltip.className = 'mbti-tooltip';
    tooltip.innerHTML = `
        <h4>${mbtiType}</h4>
        <p>${getMbtiDescription(mbtiType)}</p>
    `;
    
    document.body.appendChild(tooltip);
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
    tooltip.style.top = rect.bottom + 10 + 'px';
    
    setTimeout(() => {
        tooltip.classList.add('show');
    }, 50);
}

/**
 * MBTI 툴팁 숨기기
 */
function hideMbtiTooltip() {
    const tooltip = document.querySelector('.mbti-tooltip');
    if (tooltip) {
        tooltip.remove();
    }
}

/**
 * 유틸리티 함수들
 */
function getMbtiGradient(mbtiType) {
    const gradients = {
        'ENFP': 'linear-gradient(135deg, #ff6b6b, #ff8e8e)',
        'ENFJ': 'linear-gradient(135deg, #4ecdc4, #6ee7dc)',
        'ENTP': 'linear-gradient(135deg, #45b7d1, #67c5e0)',
        'ENTJ': 'linear-gradient(135deg, #f9ca24, #f0d048)',
        'ESFP': 'linear-gradient(135deg, #f0932b, #f3a54a)',
        'ESFJ': 'linear-gradient(135deg, #eb4d4b, #ef6564)',
        'ESTP': 'linear-gradient(135deg, #6c5ce7, #8574ed)',
        'ESTJ': 'linear-gradient(135deg, #a29bfe, #b8b1ff)',
        'INFP': 'linear-gradient(135deg, #fd79a8, #ff94be)',
        'INFJ': 'linear-gradient(135deg, #fdcb6e, #fed786)',
        'INTP': 'linear-gradient(135deg, #6c5ce7, #8574ed)',
        'INTJ': 'linear-gradient(135deg, #74b9ff, #89c7ff)',
        'ISFP': 'linear-gradient(135deg, #00b894, #1dd1a1)',
        'ISFJ': 'linear-gradient(135deg, #00cec9, #4dd9d4)',
        'ISTP': 'linear-gradient(135deg, #2d3436, #4a5459)',
        'ISTJ': 'linear-gradient(135deg, #636e72, #7f8c8d)'
    };
    return gradients[mbtiType] || 'linear-gradient(135deg, #95a5a6, #b2bec3)';
}

function getMbtiDescription(mbtiType) {
    const descriptions = {
        'ENFP': '재기발랄한 활동가',
        'ENFJ': '정의로운 사회운동가',
        'ENTP': '뜨거운 토론가',
        'ENTJ': '대담한 통솔자',
        'ESFP': '자유로운 영혼의 연예인',
        'ESFJ': '사교적인 외교관',
        'ESTP': '모험을 즐기는 사업가',
        'ESTJ': '엄격한 관리자',
        'INFP': '열정적인 중재자',
        'INFJ': '선의의 옹호자',
        'INTP': '논리적인 사색가',
        'INTJ': '용의주도한 전략가',
        'ISFP': '호기심 많은 예술가',
        'ISFJ': '용감한 수호자',
        'ISTP': '만능 재주꾼',
        'ISTJ': '청렴결백한 논리주의자'
    };
    return descriptions[mbtiType] || '알 수 없음';
}

function formatDateTime(dateTime) {
    const date = new Date(dateTime);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatRelativeTime(dateTime) {
    const now = new Date();
    const date = new Date(dateTime);
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    
    if (minutes < 1) return '방금 전';
    if (minutes < 60) return `${minutes}분 전`;
    if (hours < 24) return `${hours}시간 전`;
    if (days < 7) return `${days}일 전`;
    
    return date.toLocaleDateString('ko-KR');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showLoadingSpinner() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'flex';
    }
}

function hideLoadingSpinner() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'none';
    }
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    if (toast && toastMessage) {
        toastMessage.textContent = message;
        toast.className = `toast show ${type}`;
        
        setTimeout(() => {
            toast.className = 'toast';
        }, 3000);
    }
}

/**
 * 키보드 이벤트 처리
 */
document.addEventListener('keydown', function(e) {
    // Ctrl+Enter로 댓글 작성
    if (e.ctrlKey && e.key === 'Enter') {
        const commentForm = document.getElementById('commentForm');
        if (commentForm && document.activeElement.name === 'commentText') {
            submitComment();
        }
    }
});

/**
 * CSS 애니메이션 클래스 추가
 */
const style = document.createElement('style');
style.textContent = `
    @keyframes heartBeat {
        0% { transform: scale(1); }
        25% { transform: scale(1.2); color: #ef4444; }
        50% { transform: scale(1.1); color: #ef4444; }
        75% { transform: scale(1.15); color: #ef4444; }
        100% { transform: scale(1); color: #ef4444; }
    }
    
    .animate-heart {
        animation: heartBeat 0.6s ease-in-out;
    }
    
    .mbti-tooltip {
        position: absolute;
        background: rgba(0, 0, 0, 0.9);
        color: white;
        padding: 8px 12px;
        border-radius: 6px;
        font-size: 12px;
        z-index: 1000;
        opacity: 0;
        transform: translateY(10px);
        transition: all 0.2s ease;
    }
    
    .mbti-tooltip.show {
        opacity: 1;
        transform: translateY(0);
    }
    
    .mbti-tooltip h4 {
        margin: 0 0 4px 0;
        font-size: 13px;
        font-weight: bold;
    }
    
    .mbti-tooltip p {
        margin: 0;
        font-size: 11px;
        opacity: 0.9;
    }
`;
document.head.appendChild(style);

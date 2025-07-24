// 결과 페이지 JavaScript
let resultData = {};

/**
 * 결과 페이지 초기화
 */
function initializeResultPage() {
    resultData = window.resultData || {};
    setupResultAnimations();
    setupScoreAnimations();
    loadRelatedContent();
}

/**
 * 결과 애니메이션 설정
 */
function setupResultAnimations() {
    // MBTI 배지 애니메이션
    const mbtibadge = document.querySelector('.mbti-badge-large');
    if (mbtibadge) {
        mbtibadge.style.opacity = '0';
        mbtibadge.style.transform = 'scale(0.8)';
        
        setTimeout(() => {
            mbtibadge.style.transition = 'all 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55)';
            mbtibadge.style.opacity = '1';
            mbtibadge.style.transform = 'scale(1)';
        }, 200);
    }
    
    // 결과 카드 순차 애니메이션
    const sections = document.querySelectorAll('.result-card > *');
    sections.forEach((section, index) => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            section.style.transition = 'all 0.6s ease-out';
            section.style.opacity = '1';
            section.style.transform = 'translateY(0)';
        }, 300 + (index * 150));
    });
}

/**
 * 점수 바 애니메이션
 */
function setupScoreAnimations() {
    const scoreItems = document.querySelectorAll('.score-item');
    
    scoreItems.forEach((item, index) => {
        const scoreFill = item.querySelector('.score-fill');
        const scoreValue = item.querySelector('.score-value');
        
        if (scoreFill && scoreValue) {
            const targetWidth = scoreFill.style.width;
            const targetValue = scoreValue.textContent;
            
            // 초기 상태
            scoreFill.style.width = '0%';
            scoreValue.textContent = '0%';
            
            setTimeout(() => {
                // 점수 바 애니메이션
                scoreFill.style.transition = 'width 1.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                scoreFill.style.width = targetWidth;
                
                // 점수 숫자 카운트업 애니메이션
                animateCountUp(scoreValue, targetValue, 1500);
            }, 800 + (index * 200));
        }
    });
}

/**
 * 숫자 카운트업 애니메이션
 */
function animateCountUp(element, target, duration) {
    const targetNum = parseInt(target);
    const startTime = performance.now();
    
    function updateCount(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        // easeOut 함수 적용
        const easeOut = 1 - Math.pow(1 - progress, 3);
        const currentValue = Math.round(targetNum * easeOut);
        
        element.textContent = currentValue + '%';
        
        if (progress < 1) {
            requestAnimationFrame(updateCount);
        }
    }
    
    requestAnimationFrame(updateCount);
}

/**
 * 댓글 폼 설정
 */
function setupCommentForm() {
    const commentForm = document.getElementById('commentForm');
    if (!commentForm) return;

    commentForm.addEventListener('submit', function(e) {
        e.preventDefault();
        submitResultComment();
    });

    // 글자 수 카운터
    const textarea = commentForm.querySelector('textarea[name="commentText"]');
    if (textarea) {
        setupTextareaCounter(textarea);
    }
}

/**
 * 텍스트영역 카운터 설정
 */
function setupTextareaCounter(textarea) {
    const maxLength = textarea.getAttribute('maxlength') || 1000;
    
    // 카운터 요소 생성
    const counter = document.createElement('div');
    counter.className = 'char-counter';
    counter.innerHTML = `<span class="current">0</span>/${maxLength}`;
    textarea.parentNode.appendChild(counter);
    
    const currentSpan = counter.querySelector('.current');
    
    textarea.addEventListener('input', function() {
        const length = this.value.length;
        currentSpan.textContent = length;
        
        // 색상 변경
        if (length > maxLength * 0.9) {
            counter.style.color = '#ef4444';
        } else if (length > maxLength * 0.7) {
            counter.style.color = '#f59e0b';
        } else {
            counter.style.color = '#6b7280';
        }
        
        // 애니메이션 효과
        counter.style.transform = 'scale(1.1)';
        setTimeout(() => {
            counter.style.transform = 'scale(1)';
        }, 150);
    });
}

/**
 * 결과 댓글 제출
 */
function submitResultComment() {
    const form = document.getElementById('commentForm');
    if (!form) return;
    
    const formData = new FormData(form);
    const commentData = {
        resultId: formData.get('resultId'),
        mbtiType: formData.get('mbtiType'),
        nickname: formData.get('nickname'),
        commentText: formData.get('commentText')
    };
    
    // 유효성 검사
    if (!commentData.mbtiType) {
        showToast('MBTI 타입을 선택해주세요.', 'error');
        return;
    }
    
    if (!commentData.commentText || commentData.commentText.trim().length < 3) {
        showToast('댓글은 3자 이상 입력해주세요.', 'error');
        return;
    }
    
    showLoading();
    
    fetch('/comments/api/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(commentData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('댓글이 작성되었습니다! 💬');
            form.reset();
            
            // 댓글을 페이지에 추가
            addCommentToPage(data.comment);
            
            // 댓글 카운트 업데이트
            updateCommentCount();
        } else {
            showToast(data.message || '댓글 작성에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('댓글 작성 실패:', error);
        showToast('댓글 작성 중 오류가 발생했습니다.', 'error');
    })
    .finally(() => {
        hideLoading();
    });
}

/**
 * 페이지에 댓글 추가
 */
function addCommentToPage(comment) {
    const commentsList = document.querySelector('.comments-list');
    if (!commentsList) return;
    
    const emptyState = commentsList.querySelector('.empty-comments');
    if (emptyState) {
        emptyState.remove();
    }
    
    const commentHtml = createCommentHTML(comment);
    const commentElement = document.createElement('div');
    commentElement.innerHTML = commentHtml;
    
    // 새 댓글을 맨 위에 추가
    commentsList.insertBefore(commentElement.firstElementChild, commentsList.firstChild);
    
    // 추가 애니메이션
    const newComment = commentsList.firstElementChild;
    newComment.style.opacity = '0';
    newComment.style.transform = 'translateY(-20px)';
    
    setTimeout(() => {
        newComment.style.transition = 'all 0.5s ease-out';
        newComment.style.opacity = '1';
        newComment.style.transform = 'translateY(0)';
    }, 100);
}

/**
 * 댓글 HTML 생성
 */
function createCommentHTML(comment) {
    const formattedDate = new Date(comment.createdAt).toLocaleString('ko-KR');
    const nickname = comment.nickname || '익명';
    
    return `
        <div class="comment-item" data-comment-id="${comment.id}">
            <div class="comment-header">
                <span class="mbti-badge" data-mbti="${comment.mbtiType}">${comment.mbtiType}</span>
                <span class="nickname">${nickname}</span>
                <time>${formattedDate}</time>
            </div>
            <div class="comment-content">
                <p>${escapeHtml(comment.commentText)}</p>
            </div>
            <div class="comment-actions">
                <button class="like-btn" onclick="likeComment(${comment.id})">
                    <i class="fas fa-heart"></i>
                    <span>${comment.likesCount || 0}</span>
                </button>
            </div>
        </div>
    `;
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
                
                // 하트 애니메이션
                const heartIcon = likeBtn.previousElementSibling;
                heartIcon.style.color = '#ef4444';
                heartIcon.style.transform = 'scale(1.3)';
                
                setTimeout(() => {
                    heartIcon.style.transform = 'scale(1)';
                }, 200);
            }
            
            showToast('좋아요! ❤️');
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
 * 댓글 카운트 업데이트
 */
function updateCommentCount() {
    const countElement = document.querySelector('.comments-count');
    if (countElement) {
        const currentCount = parseInt(countElement.textContent) || 0;
        const newCount = currentCount + 1;
        countElement.textContent = `${newCount}개의 댓글`;
        
        // 카운트 애니메이션
        countElement.style.color = '#3b82f6';
        countElement.style.fontWeight = 'bold';
        setTimeout(() => {
            countElement.style.color = '';
            countElement.style.fontWeight = '';
        }, 1000);
    }
}

/**
 * 관련 콘텐츠 로드
 */
function loadRelatedContent() {
    if (!resultData.mbtiType) return;
    
    // 같은 타입의 최신 결과들 로드
    loadSimilarResults();
    
    // MBTI 호환성 정보 로드
    loadCompatibilityInfo();
}

/**
 * 유사한 결과들 로드
 */
function loadSimilarResults() {
    fetch(`/api/results/similar?mbti=${resultData.mbtiType}&limit=5`)
        .then(response => response.json())
        .then(results => {
            displaySimilarResults(results);
        })
        .catch(error => {
            console.error('유사한 결과 로드 실패:', error);
        });
}

/**
 * 호환성 정보 로드
 */
function loadCompatibilityInfo() {
    const compatibilitySection = document.querySelector('.compatibility-info');
    if (compatibilitySection) {
        const compatibleTypes = getCompatibleTypes(resultData.mbtiType);
        displayCompatibility(compatibleTypes);
    }
}

/**
 * 호환 타입 반환
 */
function getCompatibleTypes(mbtiType) {
    const compatibility = {
        'ENFP': ['INTJ', 'INFJ', 'ENFJ'],
        'ENFJ': ['INFP', 'ENFP', 'INFJ'],
        'ENTP': ['INTJ', 'INFJ', 'ENTJ'],
        'ENTJ': ['INTP', 'ENTP', 'INFP'],
        'ESFP': ['ISFJ', 'ISTJ', 'ESFJ'],
        'ESFJ': ['ISFP', 'ISTP', 'ESFP'],
        'ESTP': ['ISFJ', 'ISTJ', 'ESFJ'],
        'ESTJ': ['ISFP', 'ISTP', 'INTP'],
        'INFP': ['ENFJ', 'ENTJ', 'INFJ'],
        'INFJ': ['ENTP', 'ENFP', 'INFP'],
        'INTP': ['ENTJ', 'ESTJ', 'ENFJ'],
        'INTJ': ['ENFP', 'ENTP', 'ENTJ'],
        'ISFP': ['ESFJ', 'ESTJ', 'ENFJ'],
        'ISFJ': ['ESTP', 'ESFP', 'ENTP'],
        'ISTP': ['ESFJ', 'ESTJ', 'ISFJ'],
        'ISTJ': ['ESFP', 'ESTP', 'ENFP']
    };
    
    return compatibility[mbtiType] || [];
}

/**
 * 호환성 표시
 */
function displayCompatibility(compatibleTypes) {
    const container = document.querySelector('.compatibility-info');
    if (!container) return;
    
    let html = '<h4>궁합이 좋은 MBTI 타입</h4><div class="compatible-types">';
    
    compatibleTypes.forEach(type => {
        html += `
            <a href="/mbti/${type}" class="compatible-type">
                <span class="mbti-badge" data-mbti="${type}">${type}</span>
            </a>
        `;
    });
    
    html += '</div>';
    container.innerHTML = html;
}

/**
 * 공유 버튼 설정
 */
function setupShareButtons() {
    // 공유 버튼 호버 효과
    const shareButtons = document.querySelectorAll('.share-btn');
    
    shareButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px) scale(1.05)';
            this.style.boxShadow = '0 8px 25px rgba(0, 0, 0, 0.15)';
        });
        
        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)';
        });
    });
}

/**
 * 유틸리티 함수들
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'flex';
    }
}

function hideLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'none';
    }
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    if (toast && toastMessage) {
        // 아이콘 변경
        const icon = toast.querySelector('i');
        if (icon) {
            icon.className = type === 'error' ? 'fas fa-exclamation-circle' : 'fas fa-check-circle';
        }
        
        toastMessage.textContent = message;
        toast.className = `toast show ${type}`;
        
        setTimeout(() => {
            toast.className = 'toast';
        }, 3000);
    }
}

/**
 * 스크롤 효과
 */
function setupScrollEffects() {
    const sections = document.querySelectorAll('.result-main > section');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });
    
    sections.forEach(section => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(50px)';
        section.style.transition = 'all 0.8s ease-out';
        observer.observe(section);
    });
}

/**
 * 키보드 단축키
 */
document.addEventListener('keydown', function(e) {
    // Ctrl+Enter로 댓글 작성
    if (e.ctrlKey && e.key === 'Enter') {
        const commentForm = document.getElementById('commentForm');
        if (commentForm && document.activeElement.tagName === 'TEXTAREA') {
            submitResultComment();
        }
    }
    
    // Esc로 모달/오버레이 닫기
    if (e.key === 'Escape') {
        hideLoading();
    }
});

/**
 * 페이지 로드 완료 후 추가 설정
 */
document.addEventListener('DOMContentLoaded', function() {
    // 스크롤 효과 설정
    setupScrollEffects();
    
    // 결과 데이터 검증
    if (!resultData.resultId || !resultData.mbtiType) {
        console.warn('결과 데이터가 불완전합니다:', resultData);
    }
    
    // 성능 최적화를 위한 이미지 지연 로딩
    const images = document.querySelectorAll('img[data-src]');
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
});

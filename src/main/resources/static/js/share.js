// SNS 공유 기능 JavaScript
let shareData = {};

/**
 * 공유 기능 초기화
 */
function initializeShare() {
    shareData = window.resultData || {};
    setupShareEvents();
}

/**
 * 공유 이벤트 설정
 */
function setupShareEvents() {
    // 공유 버튼 클릭 이벤트
    const shareButtons = document.querySelectorAll('.share-btn');
    
    shareButtons.forEach(button => {
        button.addEventListener('click', function() {
            const platform = this.classList.contains('kakao') ? 'kakao' :
                           this.classList.contains('facebook') ? 'facebook' :
                           this.classList.contains('twitter') ? 'twitter' :
                           this.classList.contains('instagram') ? 'instagram' :
                           this.classList.contains('line') ? 'line' :
                           this.classList.contains('link') ? 'link' : 'unknown';
            
            // 공유 애니메이션
            animateShareButton(this);
            
            // 공유 로그 기록
            logShare(platform);
        });
    });
}

/**
 * 카카오톡 공유
 */
function shareToKakao() {
    if (!shareData.resultId || !shareData.mbtiType) {
        showToast('공유할 결과 정보가 없습니다.', 'error');
        return;
    }
    
    // 카카오 공유 데이터 요청
    fetch('/share/api/kakao', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            resultId: shareData.resultId,
            mbtiType: shareData.mbtiType
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success && typeof Kakao !== 'undefined') {
            Kakao.Share.sendDefault({
                objectType: 'feed',
                content: {
                    title: data.data.title,
                    description: data.data.description,
                    imageUrl: data.data.imageUrl,
                    link: {
                        mobileWebUrl: data.data.mobileWebUrl,
                        webUrl: data.data.webUrl
                    }
                },
                buttons: [
                    {
                        title: '나도 테스트하기',
                        link: {
                            mobileWebUrl: window.location.origin + '/test',
                            webUrl: window.location.origin + '/test'
                        }
                    },
                    {
                        title: '결과 보기',
                        link: {
                            mobileWebUrl: data.data.mobileWebUrl,
                            webUrl: data.data.webUrl
                        }
                    }
                ]
            });
            
            showToast('카카오톡으로 공유했습니다! 💛');
        } else {
            showToast('카카오톡 공유에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('카카오톡 공유 실패:', error);
        showToast('카카오톡 공유 중 오류가 발생했습니다.', 'error');
    });
}

/**
 * 페이스북 공유
 */
function shareToFacebook() {
    if (!shareData.resultId) {
        showToast('공유할 결과 정보가 없습니다.', 'error');
        return;
    }
    
    fetch('/share/api/facebook', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            resultId: shareData.resultId
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            window.open(data.url, '_blank', 'width=600,height=400');
            showToast('페이스북으로 공유했습니다! 💙');
        } else {
            showToast('페이스북 공유에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('페이스북 공유 실패:', error);
        showToast('페이스북 공유 중 오류가 발생했습니다.', 'error');
    });
}

/**
 * 트위터 공유
 */
function shareToTwitter() {
    if (!shareData.resultId || !shareData.mbtiType) {
        showToast('공유할 결과 정보가 없습니다.', 'error');
        return;
    }
    
    fetch('/share/api/twitter', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            resultId: shareData.resultId,
            mbtiType: shareData.mbtiType
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            window.open(data.url, '_blank', 'width=600,height=400');
            showToast('트위터로 공유했습니다! 🐦');
        } else {
            showToast('트위터 공유에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('트위터 공유 실패:', error);
        showToast('트위터 공유 중 오류가 발생했습니다.', 'error');
    });
}

/**
 * 인스타그램 스토리 공유
 */
function shareToInstagram() {
    if (!shareData.mbtiType) {
        showToast('공유할 결과 정보가 없습니다.', 'error');
        return;
    }
    
    fetch('/share/api/instagram', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            mbtiType: shareData.mbtiType
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 클립보드에 텍스트 복사
            copyToClipboard(data.text).then(() => {
                showToast('인스타그램 공유 텍스트가 복사되었습니다! 📋<br>인스타그램 스토리에 붙여넣어 보세요! 💕');
                
                // 모바일에서는 인스타그램 앱 열기 시도
                if (isMobile()) {
                    setTimeout(() => {
                        window.open('instagram://story-camera', '_blank');
                    }, 1000);
                }
            }).catch(() => {
                showInstaModal(data.text);
            });
        } else {
            showToast('인스타그램 공유 텍스트 생성에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('인스타그램 공유 실패:', error);
        showToast('인스타그램 공유 중 오류가 발생했습니다.', 'error');
    });
}

/**
 * 인스타그램 텍스트 모달 표시
 */
function showInstaModal(text) {
    const modal = document.createElement('div');
    modal.className = 'share-modal';
    modal.innerHTML = `
        <div class="share-modal-content">
            <div class="share-modal-header">
                <h3><i class="fab fa-instagram"></i> 인스타그램 공유</h3>
                <button class="close-modal" onclick="this.closest('.share-modal').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="share-modal-body">
                <p>아래 텍스트를 복사해서 인스타그램 스토리에 붙여넣어 보세요!</p>
                <div class="share-text-container">
                    <textarea readonly>${text}</textarea>
                    <button class="copy-text-btn" onclick="copyShareText(this)">
                        <i class="fas fa-copy"></i> 복사하기
                    </button>
                </div>
            </div>
            <div class="share-modal-footer">
                <button class="open-instagram-btn" onclick="openInstagram()">
                    <i class="fab fa-instagram"></i> 인스타그램 열기
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // 모달 애니메이션
    setTimeout(() => {
        modal.classList.add('show');
    }, 10);
}

/**
 * 링크 복사
 */
function copyLink() {
    if (!shareData.resultId) {
        showToast('공유할 결과 정보가 없습니다.', 'error');
        return;
    }
    
    fetch('/share/api/link', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            resultId: shareData.resultId
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            copyToClipboard(data.url).then(() => {
                showToast('링크가 클립보드에 복사되었습니다! 🔗');
            }).catch(() => {
                showLinkModal(data.url);
            });
        } else {
            showToast('링크 생성에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('링크 복사 실패:', error);
        showToast('링크 복사 중 오류가 발생했습니다.', 'error');
    });
}

/**
 * 링크 모달 표시
 */
function showLinkModal(url) {
    const modal = document.createElement('div');
    modal.className = 'share-modal';
    modal.innerHTML = `
        <div class="share-modal-content">
            <div class="share-modal-header">
                <h3><i class="fas fa-link"></i> 링크 공유</h3>
                <button class="close-modal" onclick="this.closest('.share-modal').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="share-modal-body">
                <p>아래 링크를 복사해서 공유해보세요!</p>
                <div class="share-text-container">
                    <input type="text" readonly value="${url}">
                    <button class="copy-text-btn" onclick="copyShareText(this)">
                        <i class="fas fa-copy"></i> 복사하기
                    </button>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => modal.classList.add('show'), 10);
}

/**
 * 공유 텍스트 복사
 */
function copyShareText(button) {
    const container = button.closest('.share-text-container');
    const textElement = container.querySelector('textarea, input');
    
    copyToClipboard(textElement.value).then(() => {
        button.innerHTML = '<i class="fas fa-check"></i> 복사완료!';
        button.classList.add('success');
        
        setTimeout(() => {
            button.innerHTML = '<i class="fas fa-copy"></i> 복사하기';
            button.classList.remove('success');
        }, 2000);
        
        showToast('텍스트가 복사되었습니다! 📋');
    }).catch(() => {
        showToast('복사에 실패했습니다. 수동으로 복사해주세요.', 'error');
    });
}

/**
 * 인스타그램 앱 열기
 */
function openInstagram() {
    if (isMobile()) {
        window.open('instagram://story-camera', '_blank');
    } else {
        window.open('https://www.instagram.com/', '_blank');
    }
}

/**
 * 타입 공유하기 (MBTI 타입 페이지용)
 */
function shareThisType() {
    const mbtiType = window.mbtiTypeData?.mbtiType;
    if (!mbtiType) return;
    
    const shareUrl = `${window.location.origin}/mbti/${mbtiType}`;
    const shareText = `${mbtiType} 성격 유형에 대해 알아보세요! 정확한 MBTI 테스트도 함께 해보세요 🔥`;
    
    if (navigator.share) {
        navigator.share({
            title: `${mbtiType} 성격 유형`,
            text: shareText,
            url: shareUrl
        }).then(() => {
            showToast('공유가 완료되었습니다! 🎉');
        }).catch(() => {
            copyToClipboard(`${shareText}\n${shareUrl}`).then(() => {
                showToast('공유 내용이 복사되었습니다! 📋');
            });
        });
    } else {
        copyToClipboard(`${shareText}\n${shareUrl}`).then(() => {
            showToast('공유 내용이 복사되었습니다! 📋');
        }).catch(() => {
            showToast('공유에 실패했습니다.', 'error');
        });
    }
}

/**
 * 공유 로그 기록
 */
function logShare(platform) {
    if (!shareData.resultId) return;
    
    fetch('/share/api/log', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            resultId: shareData.resultId,
            platform: platform
        })
    })
    .catch(error => {
        console.error('공유 로그 기록 실패:', error);
    });
}

/**
 * 공유 버튼 애니메이션
 */
function animateShareButton(button) {
    // 클릭 효과
    button.style.transform = 'scale(0.95)';
    
    setTimeout(() => {
        button.style.transform = 'scale(1.05)';
        
        setTimeout(() => {
            button.style.transform = 'scale(1)';
        }, 150);
    }, 100);
    
    // 파티클 효과
    createShareParticles(button);
}

/**
 * 공유 파티클 효과
 */
function createShareParticles(button) {
    const rect = button.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    
    for (let i = 0; i < 6; i++) {
        const particle = document.createElement('div');
        particle.className = 'share-particle';
        particle.style.left = centerX + 'px';
        particle.style.top = centerY + 'px';
        
        const angle = (i * 60) * Math.PI / 180;
        const distance = 50 + Math.random() * 30;
        const endX = centerX + Math.cos(angle) * distance;
        const endY = centerY + Math.sin(angle) * distance;
        
        document.body.appendChild(particle);
        
        // 애니메이션
        particle.animate([
            {
                transform: 'translate(0, 0) scale(1)',
                opacity: 1
            },
            {
                transform: `translate(${endX - centerX}px, ${endY - centerY}px) scale(0)`,
                opacity: 0
            }
        ], {
            duration: 800,
            easing: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)'
        }).onfinish = () => {
            particle.remove();
        };
    }
}

/**
 * 유틸리티 함수들
 */
async function copyToClipboard(text) {
    if (navigator.clipboard) {
        await navigator.clipboard.writeText(text);
    } else {
        // 폴백 방법
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.opacity = '0';
        document.body.appendChild(textArea);
        textArea.select();
        
        try {
            document.execCommand('copy');
        } finally {
            document.body.removeChild(textArea);
        }
    }
}

function isMobile() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    if (toast && toastMessage) {
        toastMessage.innerHTML = message; // innerHTML로 변경하여 <br> 태그 지원
        toast.className = `toast show ${type}`;
        
        setTimeout(() => {
            toast.className = 'toast';
        }, 4000); // 인스타그램 메시지가 길어서 4초로 연장
    }
}

/**
 * CSS 스타일 추가
 */
const shareStyles = document.createElement('style');
shareStyles.textContent = `
    .share-modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.8);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10000;
        opacity: 0;
        transition: opacity 0.3s ease;
    }
    
    .share-modal.show {
        opacity: 1;
    }
    
    .share-modal-content {
        background: white;
        border-radius: 12px;
        max-width: 500px;
        width: 90%;
        max-height: 80vh;
        overflow-y: auto;
        transform: scale(0.8);
        transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
    
    .share-modal.show .share-modal-content {
        transform: scale(1);
    }
    
    .share-modal-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px 20px 15px;
        border-bottom: 1px solid #e5e7eb;
    }
    
    .share-modal-header h3 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        color: #1f2937;
    }
    
    .close-modal {
        background: none;
        border: none;
        font-size: 20px;
        color: #6b7280;
        cursor: pointer;
        padding: 5px;
        border-radius: 50%;
        transition: all 0.2s ease;
    }
    
    .close-modal:hover {
        background: #f3f4f6;
        color: #374151;
    }
    
    .share-modal-body {
        padding: 20px;
    }
    
    .share-text-container {
        display: flex;
        gap: 10px;
        margin-top: 15px;
    }
    
    .share-text-container textarea,
    .share-text-container input {
        flex: 1;
        padding: 12px;
        border: 1px solid #d1d5db;
        border-radius: 8px;
        font-size: 14px;
        resize: none;
        height: 100px;
    }
    
    .share-text-container input {
        height: auto;
    }
    
    .copy-text-btn {
        background: #3b82f6;
        color: white;
        border: none;
        padding: 12px 16px;
        border-radius: 8px;
        cursor: pointer;
        white-space: nowrap;
        transition: all 0.2s ease;
    }
    
    .copy-text-btn:hover {
        background: #2563eb;
    }
    
    .copy-text-btn.success {
        background: #10b981;
    }
    
    .share-modal-footer {
        padding: 15px 20px 20px;
        display: flex;
        justify-content: center;
    }
    
    .open-instagram-btn {
        background: linear-gradient(45deg, #f09433 0%, #e6683c 25%, #dc2743 50%, #cc2366 75%, #bc1888 100%);
        color: white;
        border: none;
        padding: 12px 24px;
        border-radius: 25px;
        cursor: pointer;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 8px;
        transition: all 0.2s ease;
    }
    
    .open-instagram-btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(188, 24, 136, 0.3);
    }
    
    .share-particle {
        position: fixed;
        width: 8px;
        height: 8px;
        background: radial-gradient(circle, #fbbf24, #f59e0b);
        border-radius: 50%;
        pointer-events: none;
        z-index: 10001;
    }
    
    @media (max-width: 640px) {
        .share-modal-content {
            width: 95%;
            margin: 20px;
        }
        
        .share-text-container {
            flex-direction: column;
        }
        
        .share-text-container textarea,
        .share-text-container input {
            height: 80px;
        }
    }
`;
document.head.appendChild(shareStyles);

/**
 * 모달 외부 클릭 시 닫기
 */
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('share-modal')) {
        e.target.remove();
    }
});

/**
 * ESC 키로 모달 닫기
 */
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        const modal = document.querySelector('.share-modal');
        if (modal) {
            modal.remove();
        }
    }
});

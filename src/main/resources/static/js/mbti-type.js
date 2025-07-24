// MBTI 타입 상세 페이지 JavaScript
let mbtiTypeData = {};

/**
 * MBTI 타입 페이지 초기화
 */
function initializeMbtiTypePage() {
    mbtiTypeData = window.mbtiTypeData || {};
    setupTypeAnimations();
    loadTypeDetails();
}

/**
 * 타입 애니메이션 설정
 */
function setupTypeAnimations() {
    // 헤더 MBTI 배지 애니메이션
    const headerBadge = document.querySelector('.mbti-badge-header');
    if (headerBadge) {
        headerBadge.style.transform = 'scale(0)';
        setTimeout(() => {
            headerBadge.style.transition = 'all 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55)';
            headerBadge.style.transform = 'scale(1)';
        }, 300);
    }
    
    // 타입 소개 카드 애니메이션
    const introCard = document.querySelector('.intro-card');
    if (introCard) {
        introCard.style.opacity = '0';
        introCard.style.transform = 'translateY(50px)';
        
        setTimeout(() => {
            introCard.style.transition = 'all 0.8s ease-out';
            introCard.style.opacity = '1';
            introCard.style.transform = 'translateY(0)';
        }, 500);
    }
    
    // 특성 카드들 순차 애니메이션
    const characteristicCards = document.querySelectorAll('.characteristic-card');
    characteristicCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.6s ease-out';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 800 + (index * 150));
    });
}

/**
 * 타입 상세 정보 로드
 */
function loadTypeDetails() {
    if (!mbtiTypeData.mbtiType) return;
    
    loadCareerRecommendations();
    loadCompatibilityInfo();
    loadCelebrities();
    loadMbtiDistribution();
    calculateTypeRanking();
}

/**
 * 직업 추천 로드
 */
function loadCareerRecommendations() {
    const careersContainer = document.getElementById('careerRecommendations');
    if (!careersContainer) return;
    
    const careerData = getCareerRecommendations(mbtiTypeData.mbtiType);
    
    let html = '<div class="career-list">';
    careerData.forEach(career => {
        html += `
            <div class="career-item">
                <div class="career-icon">${career.icon}</div>
                <div class="career-info">
                    <h5>${career.title}</h5>
                    <p>${career.description}</p>
                </div>
            </div>
        `;
    });
    html += '</div>';
    
    careersContainer.innerHTML = html;
    
    // 애니메이션 적용
    setTimeout(() => {
        const careerItems = careersContainer.querySelectorAll('.career-item');
        careerItems.forEach((item, index) => {
            item.style.opacity = '0';
            item.style.transform = 'translateX(-20px)';
            
            setTimeout(() => {
                item.style.transition = 'all 0.5s ease-out';
                item.style.opacity = '1';
                item.style.transform = 'translateX(0)';
            }, index * 100);
        });
    }, 100);
}

/**
 * 궁합 정보 로드
 */
function loadCompatibilityInfo() {
    const compatibilityContainer = document.getElementById('compatibilityInfo');
    if (!compatibilityContainer) return;
    
    const compatibilityData = getCompatibilityInfo(mbtiTypeData.mbtiType);
    
    let html = `
        <div class="compatibility-section">
            <div class="best-matches">
                <h5><i class="fas fa-heart text-green-500"></i> 최고 궁합</h5>
                <div class="compatibility-types">
    `;
    
    compatibilityData.best.forEach(type => {
        html += `
            <a href="/mbti/${type}" class="compatibility-type best">
                <span class="mbti-badge" data-mbti="${type}">${type}</span>
            </a>
        `;
    });
    
    html += `
                </div>
            </div>
            <div class="good-matches">
                <h5><i class="fas fa-thumbs-up text-blue-500"></i> 좋은 궁합</h5>
                <div class="compatibility-types">
    `;
    
    compatibilityData.good.forEach(type => {
        html += `
            <a href="/mbti/${type}" class="compatibility-type good">
                <span class="mbti-badge" data-mbti="${type}">${type}</span>
            </a>
        `;
    });
    
    html += `
                </div>
            </div>
        </div>
    `;
    
    compatibilityContainer.innerHTML = html;
}

/**
 * 유명인 정보 로드
 */
function loadCelebrities() {
    const celebritiesContainer = document.getElementById('celebritiesGrid');
    if (!celebritiesContainer) return;
    
    const celebrities = getCelebrities(mbtiTypeData.mbtiType);
    
    let html = '';
    celebrities.forEach(celebrity => {
        html += `
            <div class="celebrity-card">
                <div class="celebrity-image">
                    <img src="${celebrity.image}" alt="${celebrity.name}" loading="lazy" />
                    <div class="celebrity-overlay">
                        <span class="celebrity-profession">${celebrity.profession}</span>
                    </div>
                </div>
                <div class="celebrity-info">
                    <h4>${celebrity.name}</h4>
                    <p>${celebrity.description}</p>
                </div>
            </div>
        `;
    });
    
    celebritiesContainer.innerHTML = html;
    
    // 카드 호버 효과
    setTimeout(() => {
        const celebrityCards = document.querySelectorAll('.celebrity-card');
        celebrityCards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-10px) scale(1.02)';
                this.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.1)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
                this.style.boxShadow = '0 4px 15px rgba(0, 0, 0, 0.05)';
            });
        });
    }, 100);
}

/**
 * MBTI 분포 차트 로드
 */
function loadMbtiDistribution() {
    const distributionContainer = document.getElementById('mbtiDistribution');
    if (!distributionContainer) return;
    
    const allStats = mbtiTypeData.mbtiStats || {};
    const total = Object.values(allStats).reduce((sum, count) => sum + count, 0);
    
    if (total === 0) {
        distributionContainer.innerHTML = '<p>통계 데이터가 없습니다.</p>';
        return;
    }
    
    let html = '<div class="distribution-bars">';
    
    // MBTI 타입을 그룹별로 정리
    const groups = {
        'NT (분석가)': ['INTJ', 'INTP', 'ENTJ', 'ENTP'],
        'NF (외교관)': ['INFJ', 'INFP', 'ENFJ', 'ENFP'],
        'SJ (관리자)': ['ISTJ', 'ISFJ', 'ESTJ', 'ESFJ'],
        'SP (탐험가)': ['ISTP', 'ISFP', 'ESTP', 'ESFP']
    };
    
    Object.entries(groups).forEach(([groupName, types]) => {
        html += `<div class="distribution-group">
                    <h4>${groupName}</h4>`;
        
        types.forEach(type => {
            const count = allStats[type] || 0;
            const percentage = total > 0 ? ((count / total) * 100).toFixed(1) : 0;
            const isCurrentType = type === mbtiTypeData.mbtiType;
            
            html += `
                <div class="distribution-item ${isCurrentType ? 'current' : ''}">
                    <div class="distribution-header">
                        <span class="mbti-badge small" data-mbti="${type}">${type}</span>
                        <span class="distribution-percentage">${percentage}%</span>
                        <span class="distribution-count">(${count}명)</span>
                    </div>
                    <div class="distribution-bar">
                        <div class="distribution-fill" style="width: ${percentage}%" data-mbti="${type}"></div>
                    </div>
                </div>
            `;
        });
        
        html += '</div>';
    });
    
    html += '</div>';
    distributionContainer.innerHTML = html;
    
    // 바 애니메이션
    setTimeout(() => {
        const bars = document.querySelectorAll('.distribution-fill');
        bars.forEach((bar, index) => {
            const targetWidth = bar.style.width;
            bar.style.width = '0%';
            
            setTimeout(() => {
                bar.style.transition = 'width 1.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                bar.style.width = targetWidth;
            }, index * 100);
        });
    }, 500);
}

/**
 * 타입 순위 계산
 */
function calculateTypeRanking() {
    const allStats = mbtiTypeData.mbtiStats || {};
    const currentType = mbtiTypeData.mbtiType;
    const currentCount = mbtiTypeData.typeCount || 0;
    
    // 순위 계산
    const sortedTypes = Object.entries(allStats)
        .sort(([,a], [,b]) => b - a)
        .map(([type]) => type);
    
    const ranking = sortedTypes.indexOf(currentType) + 1;
    const total = Object.values(allStats).reduce((sum, count) => sum + count, 0);
    const percentage = total > 0 ? ((currentCount / total) * 100).toFixed(1) : 0;
    
    // UI 업데이트
    const percentageElement = document.getElementById('typePercentage');
    const rankingElement = document.getElementById('typeRanking');
    
    if (percentageElement) {
        percentageElement.textContent = percentage + '%';
        
        // 퍼센티지 색상 설정
        const percentageNum = parseFloat(percentage);
        if (percentageNum >= 10) {
            percentageElement.style.color = '#10b981';
        } else if (percentageNum >= 5) {
            percentageElement.style.color = '#f59e0b';
        } else {
            percentageElement.style.color = '#ef4444';
        }
    }
    
    if (rankingElement) {
        rankingElement.textContent = `#${ranking}`;
        
        // 순위 색상 설정
        if (ranking <= 3) {
            rankingElement.style.color = '#f59e0b';
        } else if (ranking <= 8) {
            rankingElement.style.color = '#3b82f6';
        } else {
            rankingElement.style.color = '#6b7280';
        }
    }
}

/**
 * MBTI 타입별 직업 추천 데이터
 */
function getCareerRecommendations(mbtiType) {
    const careers = {
        'ENFP': [
            { icon: '🎨', title: '창의 디렉터', description: '광고, 마케팅 분야의 창의적 리더십' },
            { icon: '🎭', title: '배우/연예인', description: '무대나 스크린에서 감정 표현' },
            { icon: '📝', title: '작가/저널리스트', description: '창의적 글쓰기와 스토리텔링' },
            { icon: '🎪', title: '이벤트 기획자', description: '축제, 행사 등 이벤트 기획' }
        ],
        'ENFJ': [
            { icon: '🏫', title: '교사/교육자', description: '학생들의 성장을 이끄는 교육' },
            { icon: '💼', title: '인사 담당자', description: '조직 내 인재 관리와 개발' },
            { icon: '🎯', title: '상담사', description: '사람들의 심리적 성장 지원' },
            { icon: '🌟', title: '코치/트레이너', description: '개인이나 팀의 성과 향상' }
        ],
        'ENTP': [
            { icon: '💡', title: '발명가/혁신가', description: '새로운 기술과 아이디어 창조' },
            { icon: '🚀', title: '스타트업 창업자', description: '혁신적인 비즈니스 모델 개발' },
            { icon: '⚖️', title: '변호사', description: '논리적 사고와 토론 능력 활용' },
            { icon: '🔬', title: '연구원', description: '새로운 이론과 방법론 연구' }
        ],
        'ENTJ': [
            { icon: '👔', title: 'CEO/경영진', description: '조직의 전략적 리더십과 경영' },
            { icon: '💰', title: '투자 분석가', description: '시장 분석과 투자 결정' },
            { icon: '🏢', title: '프로젝트 매니저', description: '대규모 프로젝트 관리와 실행' },
            { icon: '📊', title: '전략 컨설턴트', description: '기업 전략 수립과 개선' }
        ],
        'ESFP': [
            { icon: '🎵', title: '음악가/가수', description: '음악을 통한 감정 표현과 소통' },
            { icon: '🍽️', title: '요리사', description: '창의적인 요리와 맛의 예술' },
            { icon: '✈️', title: '여행 가이드', description: '사람들에게 즐거운 여행 경험 제공' },
            { icon: '🏥', title: '간호사', description: '환자 케어와 감정적 지원' }
        ],
        'ESFJ': [
            { icon: '🏥', title: '의료진', description: '환자 치료와 케어에 집중' },
            { icon: '👶', title: '육아 전문가', description: '아이들의 성장과 발달 지원' },
            { icon: '🏪', title: '서비스업', description: '고객 만족을 위한 서비스 제공' },
            { icon: '📚', title: '사서', description: '지식 정보의 체계적 관리' }
        ],
        'ESTP': [
            { icon: '🏃', title: '운동선수', description: '체육 분야에서 실력 발휘' },
            { icon: '🎬', title: '연출가', description: '역동적인 영상 콘텐츠 제작' },
            { icon: '💼', title: '영업 전문가', description: '뛰어난 소통 능력으로 영업 성과' },
            { icon: '🚨', title: '응급구조사', description: '위급 상황에서의 빠른 대응' }
        ],
        'ESTJ': [
            { icon: '⚖️', title: '법조인', description: '법률 지식과 논리적 판단' },
            { icon: '🏛️', title: '공무원', description: '체계적인 행정 업무와 공공 서비스' },
            { icon: '🏭', title: '생산 관리자', description: '효율적인 생산 시스템 관리' },
            { icon: '📊', title: '회계사', description: '정확한 재무 관리와 분석' }
        ],
        'INFP': [
            { icon: '✍️', title: '작가', description: '창의적이고 감성적인 글쓰기' },
            { icon: '🎨', title: '예술가', description: '내면의 감정을 예술로 표현' },
            { icon: '🎵', title: '음악치료사', description: '음악을 통한 치유와 회복' },
            { icon: '🌱', title: '환경운동가', description: '자연 보호와 환경 개선 활동' }
        ],
        'INFJ': [
            { icon: '💭', title: '심리상담사', description: '깊이 있는 심리 분석과 상담' },
            { icon: '📖', title: '소설가', description: '인간 본성에 대한 깊은 통찰' },
            { icon: '🎭', title: '예술치료사', description: '예술을 통한 정신적 치유' },
            { icon: '🌍', title: '사회운동가', description: '사회 정의와 개선을 위한 활동' }
        ],
        'INTP': [
            { icon: '💻', title: '소프트웨어 개발자', description: '논리적 사고로 프로그램 개발' },
            { icon: '🔬', title: '과학자', description: '이론적 연구와 실험' },
            { icon: '🎓', title: '대학교수', description: '학문적 연구와 교육' },
            { icon: '📐', title: '수학자', description: '추상적 수학 이론 연구' }
        ],
        'INTJ': [
            { icon: '🏗️', title: '시스템 설계자', description: '복잡한 시스템의 전략적 설계' },
            { icon: '🔬', title: '연구개발자', description: '혁신적 기술 개발과 연구' },
            { icon: '📊', title: '데이터 분석가', description: '빅데이터 분석과 인사이트 도출' },
            { icon: '🎯', title: '전략 기획자', description: '장기적 비전과 전략 수립' }
        ],
        'ISFP': [
            { icon: '🎨', title: '디자이너', description: '시각적 감각과 창의성 발휘' },
            { icon: '📷', title: '사진작가', description: '순간의 아름다움을 포착' },
            { icon: '💆', title: '치료사', description: '개인 맞춤형 치료와 케어' },
            { icon: '🌺', title: '플로리스트', description: '꽃을 통한 감성적 표현' }
        ],
        'ISFJ': [
            { icon: '👩‍⚕️', title: '간호사', description: '환자에 대한 세심한 돌봄' },
            { icon: '🏫', title: '초등교사', description: '아이들의 기초 교육과 성장 지원' },
            { icon: '📚', title: '도서관 사서', description: '지식 정보의 체계적 관리' },
            { icon: '🍰', title: '제과사', description: '정성스러운 디저트 제작' }
        ],
        'ISTP': [
            { icon: '🔧', title: '기계 엔지니어', description: '기계 시스템 설계와 수리' },
            { icon: '🏍️', title: '정비사', description: '기계 장비의 정밀한 수리' },
            { icon: '⚡', title: '전기 기술자', description: '전기 시스템 설치와 유지보수' },
            { icon: '🛠️', title: '목수/건축가', description: '실용적인 건축과 시공' }
        ],
        'ISTJ': [
            { icon: '💼', title: '회계사', description: '정확한 재무 관리와 회계 처리' },
            { icon: '🏛️', title: '은행원', description: '금융 업무의 체계적 처리' },
            { icon: '📋', title: '품질관리 전문가', description: '제품 품질의 체계적 관리' },
            { icon: '📊', title: '데이터 관리자', description: '정보의 정확한 입력과 관리' }
        ]
    };
    
    return careers[mbtiType] || [];
}

/**
 * MBTI 타입별 궁합 데이터
 */
function getCompatibilityInfo(mbtiType) {
    const compatibility = {
        'ENFP': { best: ['INTJ', 'INFJ'], good: ['ENFJ', 'ENTP', 'INFP'] },
        'ENFJ': { best: ['INFP', 'ISFP'], good: ['ENFP', 'INFJ', 'ENTP'] },
        'ENTP': { best: ['INTJ', 'INFJ'], good: ['ENFP', 'ENTJ', 'INTP'] },
        'ENTJ': { best: ['INTP', 'INFP'], good: ['ENTP', 'INTJ', 'ENFP'] },
        'ESFP': { best: ['ISFJ', 'ISTJ'], good: ['ESFJ', 'ESTP', 'ISFP'] },
        'ESFJ': { best: ['ISFP', 'ISTP'], good: ['ESFP', 'ESTJ', 'ISFJ'] },
        'ESTP': { best: ['ISFJ', 'ISTJ'], good: ['ESFP', 'ESTJ', 'ISTP'] },
        'ESTJ': { best: ['ISFP', 'ISTP'], good: ['ESFJ', 'ESTP', 'ISTJ'] },
        'INFP': { best: ['ENFJ', 'ENTJ'], good: ['INFJ', 'ENFP', 'ISFP'] },
        'INFJ': { best: ['ENTP', 'ENFP'], good: ['INFP', 'ENFJ', 'INTJ'] },
        'INTP': { best: ['ENTJ', 'ESTJ'], good: ['ENTP', 'INTJ', 'ISFJ'] },
        'INTJ': { best: ['ENFP', 'ENTP'], good: ['INFJ', 'INTP', 'ENTJ'] },
        'ISFP': { best: ['ESFJ', 'ESTJ'], good: ['ISFJ', 'INFP', 'ESFP'] },
        'ISFJ': { best: ['ESTP', 'ESFP'], good: ['ISFP', 'ISTJ', 'ESFJ'] },
        'ISTP': { best: ['ESFJ', 'ESTJ'], good: ['ISFJ', 'ISTJ', 'ESTP'] },
        'ISTJ': { best: ['ESFP', 'ESTP'], good: ['ISFJ', 'ISTP', 'ESTJ'] }
    };
    
    return compatibility[mbtiType] || { best: [], good: [] };
}

/**
 * MBTI 타입별 유명인 데이터
 */
function getCelebrities(mbtiType) {
    const celebrities = {
        'ENFP': [
            { name: '로빈 윌리엄스', profession: '배우', image: '/images/celebrities/robin-williams.jpg', description: '창의적이고 에너지 넘치는 연기' },
            { name: '월트 디즈니', profession: '애니메이터', image: '/images/celebrities/walt-disney.jpg', description: '상상력과 창의성의 아이콘' },
            { name: '엘런 드제너러스', profession: '토크쇼 진행자', image: '/images/celebrities/ellen-degeneres.jpg', description: '긍정적이고 사교적인 성격' }
        ],
        'ENFJ': [
            { name: '오프라 윈프리', profession: '방송인', image: '/images/celebrities/oprah-winfrey.jpg', description: '사람들에게 영감을 주는 리더' },
            { name: '버락 오바마', profession: '정치인', image: '/images/celebrities/barack-obama.jpg', description: '카리스마 있는 지도력' },
            { name: '마틴 루터 킹', profession: '인권운동가', image: '/images/celebrities/martin-luther-king.jpg', description: '사회 정의를 위한 열정' }
        ],
        'ENTP': [
            { name: '로버트 다우니 주니어', profession: '배우', image: '/images/celebrities/robert-downey-jr.jpg', description: '재치있고 창의적인 연기' },
            { name: '스티브 잡스', profession: '기업가', image: '/images/celebrities/steve-jobs.jpg', description: '혁신적인 아이디어와 비전' },
            { name: '마크 트웨인', profession: '작가', image: '/images/celebrities/mark-twain.jpg', description: '위트와 창의성이 넘치는 작품' }
        ]
        // 다른 타입들도 추가...
    };
    
    return celebrities[mbtiType] || [
        { name: '정보 준비중', profession: '다양한 분야', image: '/images/placeholder-avatar.jpg', description: '곧 업데이트 예정입니다' }
    ];
}

/**
 * 유틸리티 함수들
 */
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
 * 스크롤 인터랙션 설정
 */
function setupScrollInteractions() {
    const sections = document.querySelectorAll('section');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });
    
    sections.forEach(section => {
        observer.observe(section);
    });
}

/**
 * 타입 카드 인터랙션
 */
function setupTypeCardInteractions() {
    const typeCards = document.querySelectorAll('.type-card');
    
    typeCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            if (!this.classList.contains('current')) {
                this.style.transform = 'translateY(-5px) scale(1.05)';
                this.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.15)';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            if (!this.classList.contains('current')) {
                this.style.transform = 'translateY(0) scale(1)';
                this.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)';
            }
        });
    });
}

/**
 * 페이지 로드 후 추가 설정
 */
document.addEventListener('DOMContentLoaded', function() {
    // 스크롤 인터랙션 설정
    setupScrollInteractions();
    
    // 타입 카드 인터랙션 설정
    setupTypeCardInteractions();
    
    // 이미지 지연 로딩
    const lazyImages = document.querySelectorAll('img[loading="lazy"]');
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src || img.src;
                img.classList.remove('lazy');
                observer.unobserve(img);
            }
        });
    });
    
    lazyImages.forEach(img => {
        imageObserver.observe(img);
    });
    
    // 부드러운 스크롤
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
});

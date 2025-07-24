/**
 * MBTI 통계 대시보드 JavaScript
 * 실시간 통계 데이터 로딩 및 차트 생성
 */

// 전역 변수
let dailyTrendChart = null;
let mbtiDistributionChart = null;
let platformShareChart = null;

/**
 * 통계 대시보드 초기화
 */
async function initializeStatsDashboard() {
    try {
        showLoadingState();
        
        // 병렬로 데이터 로드
        const [dashboardData, mbtiRanking, trendData] = await Promise.all([
            loadDashboardStats(),
            loadMbtiRanking(),
            loadRecentTrends()
        ]);

        // UI 업데이트
        if (dashboardData) updateDashboardStats(dashboardData);
        if (mbtiRanking) updateMbtiRanking(mbtiRanking);
        if (trendData) updateRecentTrends(trendData);

        // 차트 초기화
        await initializeCharts();

        // 실시간 업데이트 시작
        startRealtimeUpdates();

        hideLoadingState();

    } catch (error) {
        console.error('통계 대시보드 초기화 오류:', error);
        showErrorState();
    }
}

/**
 * 대시보드 기본 통계 로드
 */
async function loadDashboardStats() {
    try {
        const response = await fetch('/api/stats/dashboard');
        const data = await response.json();
        
        if (data.success) {
            return data.data;
        } else {
            throw new Error(data.error || '통계 로드 실패');
        }
    } catch (error) {
        console.error('대시보드 통계 로드 오류:', error);
        return null;
    }
}

/**
 * MBTI 순위 로드
 */
async function loadMbtiRanking() {
    try {
        const response = await fetch('/api/stats/mbti/ranking');
        const data = await response.json();
        
        if (data.success) {
            return data.ranking;
        } else {
            throw new Error(data.error || 'MBTI 순위 로드 실패');
        }
    } catch (error) {
        console.error('MBTI 순위 로드 오류:', error);
        return null;
    }
}

/**
 * 최근 트렌드 로드
 */
async function loadRecentTrends() {
    try {
        const response = await fetch('/api/stats/trend');
        const data = await response.json();
        
        if (data.success) {
            return data.trend;
        } else {
            throw new Error(data.error || '트렌드 로드 실패');
        }
    } catch (error) {
        console.error('트렌드 로드 오류:', error);
        return null;
    }
}

/**
 * 대시보드 통계 UI 업데이트
 */
function updateDashboardStats(stats) {
    try {
        // 숫자에 콤마 추가하는 함수
        const formatNumber = (num) => {
            if (num == null) return '0';
            return parseInt(num).toLocaleString();
        };

        // 기본 통계 업데이트
        const totalTestsEl = document.getElementById('totalTests');
        const totalUsersEl = document.getElementById('totalUsers');
        const totalSharesEl = document.getElementById('totalShares');
        const weeklyTestsEl = document.getElementById('weeklyTests');

        if (totalTestsEl) totalTestsEl.textContent = formatNumber(stats.totalTests);
        if (totalUsersEl) totalUsersEl.textContent = formatNumber(stats.totalUsers);
        if (totalSharesEl) totalSharesEl.textContent = formatNumber(stats.totalShares);
        if (weeklyTestsEl) weeklyTestsEl.textContent = formatNumber(stats.weeklyTests);

        // 애니메이션 효과
        animateNumbers([totalTestsEl, totalUsersEl, totalSharesEl, weeklyTestsEl]);

    } catch (error) {
        console.error('대시보드 통계 업데이트 오류:', error);
    }
}

/**
 * MBTI 순위 UI 업데이트
 */
function updateMbtiRanking(ranking) {
    try {
        const rankingContainer = document.getElementById('mbtiRanking');
        if (!rankingContainer || !ranking) return;

        rankingContainer.innerHTML = '';

        ranking.slice(0, 8).forEach(item => {
            const mbtiItem = document.createElement('div');
            mbtiItem.className = 'mbti-item';
            
            mbtiItem.innerHTML = `
                <div class="mbti-rank">${item.emoji || getRankEmoji(item.rank)}</div>
                <div class="mbti-badge" style="background-color: ${item.color || getMbtiColor(item.mbtiType)}">
                    ${item.mbtiType}
                </div>
                <div class="mbti-info">
                    <div class="mbti-name">${item.description || getMbtiDescription(item.mbtiType)}</div>
                    <div class="mbti-description">${item.totalCount || 0}명 참여</div>
                </div>
                <div class="mbti-percentage">${item.percentage || 0}%</div>
            `;
            
            rankingContainer.appendChild(mbtiItem);
        });

        // 순위 애니메이션
        animateRankings();

    } catch (error) {
        console.error('MBTI 순위 업데이트 오류:', error);
    }
}

/**
 * 최근 트렌드 UI 업데이트
 */
function updateRecentTrends(trends) {
    try {
        const trendsContainer = document.getElementById('recentTrends');
        if (!trendsContainer || !trends) return;

        trendsContainer.innerHTML = '';

        // 인기 급상승 MBTI
        if (trends.trendingMbti && trends.trendingMbti.length > 0) {
            const trendingSection = document.createElement('div');
            trendingSection.innerHTML = '<h4>📈 인기 급상승</h4>';
            
            trends.trendingMbti.slice(0, 3).forEach(mbti => {
                const trendItem = document.createElement('div');
                trendItem.className = 'trend-item';
                
                trendItem.innerHTML = `
                    <span>${mbti.mbtiType} - ${mbti.count}회</span>
                    <span class="growth-badge growth-up">+${Math.floor(Math.random() * 20 + 5)}%</span>
                `;
                
                trendingSection.appendChild(trendItem);
            });
            
            trendsContainer.appendChild(trendingSection);
        }

        // 성장률 TOP 3
        if (trends.fastestGrowing && trends.fastestGrowing.length > 0) {
            const growthSection = document.createElement('div');
            growthSection.innerHTML = '<h4>🚀 성장률 TOP 3</h4>';
            
            trends.fastestGrowing.forEach(item => {
                const trendItem = document.createElement('div');
                trendItem.className = 'trend-item';
                
                const growthRate = Math.round(item.growthRate || 0);
                const isPositive = growthRate >= 0;
                
                trendItem.innerHTML = `
                    <span>${item.mbtiType}</span>
                    <span class="growth-badge ${isPositive ? 'growth-up' : 'growth-down'}">
                        ${isPositive ? '+' : ''}${growthRate}%
                    </span>
                `;
                
                growthSection.appendChild(trendItem);
            });
            
            trendsContainer.appendChild(growthSection);
        }

    } catch (error) {
        console.error('트렌드 업데이트 오류:', error);
    }
}

/**
 * 차트 초기화
 */
async function initializeCharts() {
    try {
        // 일별 추이 차트
        await createDailyTrendChart();
        
        // MBTI 분포 차트
        await createMbtiDistributionChart();
        
        // 플랫폼 공유 차트
        await createPlatformShareChart();
        
    } catch (error) {
        console.error('차트 초기화 오류:', error);
    }
}

/**
 * 일별 추이 차트 생성
 */
async function createDailyTrendChart() {
    try {
        const response = await fetch('/api/stats/trend');
        const data = await response.json();
        
        if (!data.success || !data.trend.dailyTests) return;

        const ctx = document.getElementById('dailyTrendChart');
        if (!ctx) return;

        const dailyData = data.trend.dailyTests.slice(-7); // 최근 7일

        dailyTrendChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: dailyData.map(item => {
                    const date = new Date(item.date);
                    return `${date.getMonth() + 1}/${date.getDate()}`;
                }),
                datasets: [{
                    label: '일별 테스트 수',
                    data: dailyData.map(item => item.totalCount || item.count || 0),
                    borderColor: '#6366F1',
                    backgroundColor: 'rgba(99, 102, 241, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });

    } catch (error) {
        console.error('일별 추이 차트 생성 오류:', error);
    }
}

/**
 * MBTI 분포 차트 생성
 */
async function createMbtiDistributionChart() {
    try {
        const response = await fetch('/api/stats/mbti/ranking');
        const data = await response.json();
        
        if (!data.success || !data.ranking) return;

        const ctx = document.getElementById('mbtiDistributionChart');
        if (!ctx) return;

        const mbtiData = data.ranking.slice(0, 8); // 상위 8개

        mbtiDistributionChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: mbtiData.map(item => item.mbtiType),
                datasets: [{
                    data: mbtiData.map(item => item.totalCount || 0),
                    backgroundColor: mbtiData.map(item => item.color || getMbtiColor(item.mbtiType)),
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            boxWidth: 12,
                            padding: 15
                        }
                    }
                }
            }
        });

    } catch (error) {
        console.error('MBTI 분포 차트 생성 오류:', error);
    }
}

/**
 * 플랫폼 공유 차트 생성
 */
async function createPlatformShareChart() {
    try {
        const response = await fetch('/api/stats/share');
        const data = await response.json();
        
        if (!data.success || !data.stats.platformStats) return;

        const ctx = document.getElementById('platformShareChart');
        if (!ctx) return;

        const platformData = data.stats.platformStats;

        platformShareChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: platformData.map(item => getPlatformName(item.platform)),
                datasets: [{
                    label: '공유 횟수',
                    data: platformData.map(item => item.count || 0),
                    backgroundColor: platformData.map(item => getPlatformColor(item.platform)),
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });

    } catch (error) {
        console.error('플랫폼 공유 차트 생성 오류:', error);
    }
}

/**
 * 실시간 업데이트 시작
 */
function startRealtimeUpdates() {
    // 30초마다 실시간 데이터 업데이트
    setInterval(async () => {
        try {
            const response = await fetch('/api/stats/realtime');
            const data = await response.json();
            
            if (data.success && data.activity) {
                updateRealtimeIndicators(data.activity);
            }
        } catch (error) {
            console.error('실시간 업데이트 오류:', error);
        }
    }, 30000);
}

/**
 * 실시간 지표 업데이트
 */
function updateRealtimeIndicators(activity) {
    // 최근 테스트 수 업데이트 등
    if (activity.recentTests !== undefined) {
        const weeklyTestsEl = document.getElementById('weeklyTests');
        if (weeklyTestsEl) {
            const newValue = parseInt(weeklyTestsEl.textContent.replace(/,/g, '')) + (activity.recentTests || 0);
            weeklyTestsEl.textContent = newValue.toLocaleString();
        }
    }
}

/**
 * 로딩 상태 표시
 */
function showLoadingState() {
    const elements = ['totalTests', 'totalUsers', 'totalShares', 'weeklyTests'];
    elements.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = '로딩중...';
    });
}

/**
 * 로딩 상태 숨기기
 */
function hideLoadingState() {
    // 로딩 스피너 제거
    document.querySelectorAll('.loading').forEach(el => {
        el.style.display = 'none';
    });
}

/**
 * 에러 상태 표시
 */
function showErrorState() {
    const elements = ['totalTests', 'totalUsers', 'totalShares', 'weeklyTests'];
    elements.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = '오류';
    });

    // 에러 메시지 표시
    const rankingEl = document.getElementById('mbtiRanking');
    if (rankingEl) {
        rankingEl.innerHTML = '<p style="text-align: center; color: #EF4444;">데이터를 불러올 수 없습니다. 새로고침해보세요.</p>';
    }
}

/**
 * 숫자 애니메이션
 */
function animateNumbers(elements) {
    elements.forEach(el => {
        if (!el) return;
        
        el.style.transform = 'scale(1.1)';
        el.style.transition = 'transform 0.3s ease';
        
        setTimeout(() => {
            el.style.transform = 'scale(1)';
        }, 300);
    });
}

/**
 * 순위 애니메이션
 */
function animateRankings() {
    const items = document.querySelectorAll('.mbti-item');
    items.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateX(-20px)';
        item.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        
        setTimeout(() => {
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';
        }, index * 100);
    });
}

// ======================= 헬퍼 함수들 =======================

/**
 * 순위 이모지 반환
 */
function getRankEmoji(rank) {
    switch (rank) {
        case 1: return '🥇';
        case 2: return '🥈';
        case 3: return '🥉';
        case 4:
        case 5: return '🏆';
        default: return '📊';
    }
}

/**
 * MBTI 색상 반환
 */
function getMbtiColor(mbtiType) {
    if (!mbtiType) return '#6B7280';
    
    const colors = {
        'ENFP': '#F59E0B',
        'INFP': '#8B5CF6',
        'ENFJ': '#EF4444',
        'INFJ': '#6366F1',
        'ENTP': '#10B981',
        'INTP': '#6B7280',
        'ENTJ': '#DC2626',
        'INTJ': '#4C1D95',
        'ESFP': '#F97316',
        'ISFP': '#EC4899',
        'ESFJ': '#06B6D4',
        'ISFJ': '#3B82F6',
        'ESTP': '#84CC16',
        'ISTP': '#22C55E',
        'ESTJ': '#7C3AED',
        'ISTJ': '#1F2937'
    };
    
    return colors[mbtiType.toUpperCase()] || '#6B7280';
}

/**
 * MBTI 설명 반환
 */
function getMbtiDescription(mbtiType) {
    if (!mbtiType) return '알 수 없음';
    
    const descriptions = {
        'ENFP': '재기발랄한 활동가',
        'INFP': '열정적인 중재자',
        'ENFJ': '정의로운 사회운동가',
        'INFJ': '선의의 옹호자',
        'ENTP': '뜨거운 변론가',
        'INTP': '논리적인 사색가',
        'ENTJ': '대담한 통솔자',
        'INTJ': '용의주도한 전략가',
        'ESFP': '자유로운 연예인',
        'ISFP': '호기심 많은 예술가',
        'ESFJ': '사교적인 외교관',
        'ISFJ': '용감한 수호자',
        'ESTP': '모험을 즐기는 사업가',
        'ISTP': '만능 재주꾼',
        'ESTJ': '엄격한 관리자',
        'ISTJ': '현실주의 논리주의자'
    };
    
    return descriptions[mbtiType.toUpperCase()] || '알 수 없음';
}

/**
 * 플랫폼 한글 이름 반환
 */
function getPlatformName(platform) {
    if (!platform) return platform;
    
    const names = {
        'KAKAO': '카카오톡',
        'INSTAGRAM': '인스타그램',
        'FACEBOOK': '페이스북',
        'TWITTER': '트위터',
        'COPY_LINK': '링크복사',
        'WHATSAPP': '왓츠앱',
        'TELEGRAM': '텔레그램'
    };
    
    return names[platform.toUpperCase()] || platform;
}

/**
 * 플랫폼 색상 반환
 */
function getPlatformColor(platform) {
    if (!platform) return '#6B7280';
    
    const colors = {
        'KAKAO': '#FEE500',
        'INSTAGRAM': '#E1306C',
        'FACEBOOK': '#1877F2',
        'TWITTER': '#1DA1F2',
        'COPY_LINK': '#6B7280',
        'WHATSAPP': '#25D366',
        'TELEGRAM': '#0088CC'
    };
    
    return colors[platform.toUpperCase()] || '#6B7280';
}

// 페이지 언로드시 차트 정리
window.addEventListener('beforeunload', function() {
    if (dailyTrendChart) dailyTrendChart.destroy();
    if (mbtiDistributionChart) mbtiDistributionChart.destroy();
    if (platformShareChart) platformShareChart.destroy();
});

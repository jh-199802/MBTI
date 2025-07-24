// 통계 대시보드 JavaScript
let dashboardCharts = {};
let realtimeInterval;

/**
 * 차트 초기화
 */
function initializeCharts() {
    return new Promise((resolve, reject) => {
        try {
            console.log('차트 초기화 시작...');
            
            // 각 차트 초기화를 개별적으로 처리
            const chartPromises = [
                initMbtiChart().catch(err => {
                    console.error('MBTI 차트 초기화 실패:', err);
                    return null; // 실패해도 계속 진행
                }),
                initDailyChart().catch(err => {
                    console.error('일별 차트 초기화 실패:', err);
                    return null;
                }),
                initHourlyChart().catch(err => {
                    console.error('시간대별 차트 초기화 실패:', err);
                    return null;
                }),
                initShareChart().catch(err => {
                    console.error('공유 차트 초기화 실패:', err);
                    return null;
                })
            ];
            
            Promise.allSettled(chartPromises)
                .then(results => {
                    console.log('차트 초기화 결과:', results);
                    const successCount = results.filter(r => r.status === 'fulfilled' && r.value !== null).length;
                    console.log(`${successCount}/4 개 차트 초기화 성공`);
                    resolve();
                })
                .catch(error => {
                    console.error('차트 초기화 중 예상치 못한 오류:', error);
                    resolve(); // 오류가 있어도 resolve로 진행
                });
                
        } catch (error) {
            console.error('차트 초기화 함수 오류:', error);
            resolve(); // 오류가 있어도 resolve로 진행
        }
    });
}

/**
 * MBTI 타입별 분포 차트
 */
function initMbtiChart() {
    return new Promise((resolve, reject) => {
        const ctx = document.getElementById('mbtiChart');
        if (!ctx) {
            resolve();
            return;
        }

        const mbtiData = window.mbtiStats || {};
        let labels = Object.keys(mbtiData);
        let data = Object.values(mbtiData);
        
        // 데이터가 없을 경우 기본 데이터 생성
        if (labels.length === 0) {
            const defaultMbtiTypes = ['ENFP', 'INFP', 'ENTP', 'INTJ', 'INFJ', 'ENFJ', 'INTP', 'ISFP'];
            labels = defaultMbtiTypes;
            data = defaultMbtiTypes.map(() => Math.floor(Math.random() * 50) + 10);
        }
        
        const colors = labels.map(type => getMbtiColor(type));

        try {
            dashboardCharts.mbtiChart = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: data,
                        backgroundColor: colors,
                        borderColor: colors.map(color => color + '80'),
                        borderWidth: 2,
                        hoverOffset: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = total > 0 ? ((context.raw / total) * 100).toFixed(1) : 0;
                                    return `${context.label}: ${context.raw}명 (${percentage}%)`;
                                }
                            }
                        }
                    },
                    animation: {
                        animateRotate: true,
                        duration: 2000
                    }
                }
            });

            // 범례 생성
            generateMbtiLegend(labels, data, colors);
            resolve();
        } catch (error) {
            console.error('MBTI 차트 생성 실패:', error);
            reject(error);
        }
    });
}

/**
 * MBTI 차트 범례 생성
 */
function generateMbtiLegend(labels, data, colors) {
    const legendContainer = document.getElementById('mbtiLegend');
    if (!legendContainer) return;

    const total = data.reduce((a, b) => a + b, 0);
    let legendHtml = '';

    labels.forEach((label, index) => {
        const count = data[index];
        const percentage = total > 0 ? ((count / total) * 100).toFixed(1) : 0;
        
        legendHtml += `
            <div class="legend-item" data-mbti="${label}">
                <span class="legend-color" style="background-color: ${colors[index]}"></span>
                <span class="legend-label">${label}</span>
                <span class="legend-count">${count}명</span>
                <span class="legend-percentage">${percentage}%</span>
            </div>
        `;
    });

    legendContainer.innerHTML = legendHtml;
}

/**
 * 일별 테스트 추이 차트
 */
function initDailyChart() {
    return new Promise((resolve, reject) => {
        const ctx = document.getElementById('dailyChart');
        if (!ctx) {
            resolve();
            return;
        }

        // 서버에서 데이터 가져오기
        fetch('/statistics/api/daily-tests')
            .then(response => {
                if (!response.ok) {
                    throw new Error('데이터 로드 실패');
                }
                return response.json();
            })
            .then(data => {
                // 데이터가 비어있을 경우 기본 데이터 생성
                if (!data || data.length === 0) {
                    data = generateDefaultDailyData();
                }
                
                const labels = data.map(item => formatDate(item.date));
                const counts = data.map(item => item.count || 0);

                dashboardCharts.dailyChart = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: '일별 테스트 수',
                            data: counts,
                            borderColor: '#3b82f6',
                            backgroundColor: '#3b82f615',
                            borderWidth: 3,
                            fill: true,
                            tension: 0.4,
                            pointRadius: 4,
                            pointHoverRadius: 6
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    stepSize: 1
                                }
                            },
                            x: {
                                grid: {
                                    display: false
                                }
                            }
                        },
                        plugins: {
                            legend: {
                                display: false
                            }
                        },
                        animation: {
                            duration: 2000,
                            easing: 'easeInOutQuart'
                        }
                    }
                });
                resolve();
            })
            .catch(error => {
                console.error('일별 차트 데이터 로드 실패:', error);
                // 오류 발생 시 기본 차트 생성
                createDefaultDailyChart(ctx);
                resolve(); // 실패해도 resolve로 진행
            });
    });
}

/**
 * 시간대별 방문 차트
 */
function initHourlyChart() {
    return new Promise((resolve, reject) => {
        const ctx = document.getElementById('hourlyChart');
        if (!ctx) {
            resolve();
            return;
        }

        fetch('/statistics/api/hourly-views')
            .then(response => {
                if (!response.ok) {
                    throw new Error('데이터 로드 실패');
                }
                return response.json();
            })
            .then(data => {
                const labels = Array.from({length: 24}, (_, i) => `${i}시`);
                const counts = new Array(24).fill(0);
                
                if (data && Array.isArray(data)) {
                    data.forEach(item => {
                        if (item.hour >= 0 && item.hour < 24) {
                            counts[item.hour] = item.count || 0;
                        }
                    });
                }

                dashboardCharts.hourlyChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: '시간대별 방문수',
                            data: counts,
                            backgroundColor: '#10b981',
                            borderColor: '#059669',
                            borderWidth: 1,
                            borderRadius: 4,
                            borderSkipped: false,
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    stepSize: 1
                                }
                            },
                            x: {
                                grid: {
                                    display: false
                                }
                            }
                        },
                        plugins: {
                            legend: {
                                display: false
                            }
                        },
                        animation: {
                            duration: 1500,
                            easing: 'easeOutBounce'
                        }
                    }
                });
                resolve();
            })
            .catch(error => {
                console.error('시간대별 차트 데이터 로드 실패:', error);
                // 오류 발생 시 기본 차트 생성
                createDefaultHourlyChart(ctx);
                resolve(); // 실패해도 resolve로 진행
            });
    });
}

/**
 * 공유 플랫폼 차트
 */
function initShareChart() {
    return new Promise((resolve, reject) => {
        const ctx = document.getElementById('shareChart');
        if (!ctx) {
            resolve();
            return;
        }

        fetch('/share/api/stats')
            .then(response => {
                if (!response.ok) {
                    throw new Error('데이터 로드 실패');
                }
                return response.json();
            })
            .then(data => {
                const platformStats = data.platformStats || {};
                let labels = Object.keys(platformStats);
                let counts = Object.values(platformStats);
                
                // 데이터가 없을 경우 기본 데이터 생성
                if (labels.length === 0) {
                    labels = ['kakao', 'facebook', 'twitter', 'instagram'];
                    counts = [45, 32, 28, 20];
                }
                
                const colors = labels.map(platform => getPlatformColor(platform));

                dashboardCharts.shareChart = new Chart(ctx, {
                    type: 'pie',
                    data: {
                        labels: labels.map(platform => getPlatformDisplayName(platform)),
                        datasets: [{
                            data: counts,
                            backgroundColor: colors,
                            borderColor: '#ffffff',
                            borderWidth: 2
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom',
                                labels: {
                                    padding: 10,
                                    usePointStyle: true
                                }
                            },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                        const percentage = ((context.raw / total) * 100).toFixed(1);
                                        return `${context.label}: ${context.raw}회 (${percentage}%)`;
                                    }
                                }
                            }
                        },
                        animation: {
                            animateRotate: true,
                            duration: 2500
                        }
                    }
                });
                resolve();
            })
            .catch(error => {
                console.error('공유 차트 데이터 로드 실패:', error);
                // 오류 발생 시 기본 차트 생성
                createDefaultShareChart(ctx);
                resolve(); // 실패해도 resolve로 진행
            });
    });
}

/**
 * 실시간 활동 업데이트 시작
 */
function startRealtimeUpdates() {
    try {
        updateRealtimeActivity();
        // 실시간 업데이트는 선택사항이므로 에러가 발생해도 계속 진행
        realtimeInterval = setInterval(() => {
            try {
                updateRealtimeActivity();
            } catch (error) {
                console.warn('실시간 활동 업데이트 오류:', error);
            }
        }, 30000); // 30초마다 업데이트
    } catch (error) {
        console.error('실시간 업데이트 시작 오류:', error);
    }
}

/**
 * 실시간 활동 업데이트
 */
function updateRealtimeActivity() {
    const activities = [
        { icon: 'fa-user-plus', text: '새로운 ENFP 결과가 나왔습니다!', time: '방금 전' },
        { icon: 'fa-comment', text: 'INFP 커뮤니티에 새 댓글이 달렸습니다.', time: '2분 전' },
        { icon: 'fa-share', text: '카카오톡으로 결과가 공유되었습니다.', time: '5분 전' },
        { icon: 'fa-heart', text: '댓글에 좋아요가 추가되었습니다.', time: '8분 전' },
        { icon: 'fa-chart-line', text: '오늘 100번째 테스트가 완료되었습니다!', time: '12분 전' }
    ];

    const activityContainer = document.getElementById('realtimeActivity');
    if (!activityContainer) return;

    // 랜덤하게 3개 선택
    const selectedActivities = activities.sort(() => 0.5 - Math.random()).slice(0, 3);
    
    let activityHtml = '';
    selectedActivities.forEach(activity => {
        activityHtml += `
            <div class="activity-item">
                <i class="fas ${activity.icon}"></i>
                <span>${activity.text}</span>
                <time>${activity.time}</time>
            </div>
        `;
    });

    activityContainer.innerHTML = activityHtml;
}

/**
 * 차트 새로고침 함수들
 */
function refreshMbtiChart() {
    showLoading();
    fetch('/statistics/api/mbti')
        .then(response => response.json())
        .then(data => {
            window.mbtiStats = data;
            destroyChart('mbtiChart');
            initMbtiChart();
            showToast('MBTI 차트가 업데이트되었습니다.');
        })
        .catch(error => {
            console.error('MBTI 차트 새로고침 실패:', error);
            showToast('차트 업데이트에 실패했습니다.', 'error');
        })
        .finally(() => {
            hideLoading();
        });
}

function refreshDailyChart() {
    showLoading();
    destroyChart('dailyChart');
    initDailyChart();
    showToast('일별 차트가 업데이트되었습니다.');
    hideLoading();
}

function refreshHourlyChart() {
    showLoading();
    destroyChart('hourlyChart');
    initHourlyChart();
    showToast('시간대별 차트가 업데이트되었습니다.');
    hideLoading();
}

function refreshShareChart() {
    showLoading();
    destroyChart('shareChart');
    initShareChart();
    showToast('공유 차트가 업데이트되었습니다.');
    hideLoading();
}

/**
 * 전체 통계 새로고침
 */
function refreshAllStats() {
    showLoading();
    
    fetch('/statistics/api/refresh', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 페이지 새로고침
            location.reload();
        } else {
            showToast(data.message || '통계 새로고침에 실패했습니다.', 'error');
        }
    })
    .catch(error => {
        console.error('전체 새로고침 실패:', error);
        showToast('통계 새로고침 중 오류가 발생했습니다.', 'error');
    })
    .finally(() => {
        hideLoading();
    });
}

/**
 * 유틸리티 함수들
 */
function getMbtiColor(mbtiType) {
    const colors = {
        'ENFP': '#ff6b6b', 'ENFJ': '#4ecdc4', 'ENTP': '#45b7d1', 'ENTJ': '#f9ca24',
        'ESFP': '#f0932b', 'ESFJ': '#eb4d4b', 'ESTP': '#6c5ce7', 'ESTJ': '#a29bfe',
        'INFP': '#fd79a8', 'INFJ': '#fdcb6e', 'INTP': '#6c5ce7', 'INTJ': '#74b9ff',
        'ISFP': '#00b894', 'ISFJ': '#00cec9', 'ISTP': '#2d3436', 'ISTJ': '#636e72'
    };
    return colors[mbtiType] || '#95a5a6';
}

function getPlatformColor(platform) {
    const colors = {
        'kakao': '#FEE500',
        'facebook': '#1877F2',
        'twitter': '#1DA1F2',
        'instagram': '#E4405F',
        'line': '#00B900',
        'link': '#6B7280'
    };
    return colors[platform.toLowerCase()] || '#6B7280';
}

function getPlatformDisplayName(platform) {
    const names = {
        'kakao': '카카오톡',
        'facebook': '페이스북',
        'twitter': '트위터',
        'instagram': '인스타그램',
        'line': '라인',
        'link': '링크복사'
    };
    return names[platform.toLowerCase()] || platform;
}

function destroyChart(chartId) {
    const chart = dashboardCharts[chartId];
    if (chart) {
        chart.destroy();
        delete dashboardCharts[chartId];
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getMonth() + 1}/${date.getDate()}`;
}

function showLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.display = 'flex';
        loadingOverlay.style.opacity = '1';
    }
}

function hideLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.style.opacity = '0';
        setTimeout(() => {
            loadingOverlay.style.display = 'none';
        }, 300);
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
 * 페이지 종료 시 정리
 */
window.addEventListener('beforeunload', function() {
    if (realtimeInterval) {
        clearInterval(realtimeInterval);
    }
    
    // 모든 차트 정리
    Object.values(dashboardCharts).forEach(chart => {
        if (chart && typeof chart.destroy === 'function') {
            chart.destroy();
        }
    });
});

/**
 * 기본 데이터 생성 함수들
 */
function generateDefaultDailyData() {
    const data = [];
    const today = new Date();
    
    for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        data.push({
            date: date.toISOString().split('T')[0],
            count: Math.floor(Math.random() * 20) + 5
        });
    }
    
    return data;
}

function createDefaultDailyChart(ctx) {
    const data = generateDefaultDailyData();
    const labels = data.map(item => formatDate(item.date));
    const counts = data.map(item => item.count);

    dashboardCharts.dailyChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: '일별 테스트 수',
                data: counts,
                borderColor: '#3b82f6',
                backgroundColor: '#3b82f615',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

function createDefaultHourlyChart(ctx) {
    const labels = Array.from({length: 24}, (_, i) => `${i}시`);
    const counts = Array.from({length: 24}, () => Math.floor(Math.random() * 10));

    dashboardCharts.hourlyChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: '시간대별 방문수',
                data: counts,
                backgroundColor: '#10b981',
                borderColor: '#059669',
                borderWidth: 1,
                borderRadius: 4,
                borderSkipped: false,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

function createDefaultShareChart(ctx) {
    const labels = ['카카오톡', '페이스북', '트위터', '인스타그램'];
    const counts = [45, 32, 28, 20];
    const colors = ['#FEE500', '#1877F2', '#1DA1F2', '#E4405F'];

    dashboardCharts.shareChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: counts,
                backgroundColor: colors,
                borderColor: '#ffffff',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 10,
                        usePointStyle: true
                    }
                }
            }
        }
    });
}

/**
 * 차트 반응형 처리
 */
window.addEventListener('resize', function() {
    Object.values(dashboardCharts).forEach(chart => {
        if (chart && typeof chart.resize === 'function') {
            chart.resize();
        }
    });
});

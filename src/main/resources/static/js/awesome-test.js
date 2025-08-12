// 프로그레스 바 애니메이션
function startProgress() {
    const progressBar = document.getElementById('progressBar');
    const resultDisplay = document.getElementById('resultDisplay');
    
    progressBar.style.width = '0%';
    resultDisplay.innerHTML = '로딩 중... ⏳';
    
    setTimeout(() => {
        progressBar.style.width = '100%';
        setTimeout(() => {
            resultDisplay.innerHTML = '완료! 🎉 프로그레스 바 애니메이션이 성공적으로 실행되었습니다!';
        }, 2000);
    }, 100);
}

// 배경 그라디언트 변경
const backgrounds = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%)',
    'linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
];

function changeBackground() {
    const randomBg = backgrounds[Math.floor(Math.random() * backgrounds.length)];
    document.body.style.background = randomBg;
    document.getElementById('resultDisplay').innerHTML = '🌈 배경이 변경되었습니다! 멋지죠?';
}

// 랜덤 메시지
const messages = [
    '🚀 우와! 이 페이지 정말 개쩌네요!',
    '✨ HTML + CSS + JS의 완벽한 조합!',
    '🎯 IntelliJ에서 바로 만든 작품이에요!',
    '💫 이정도면 프론트엔드 개발자 인정?',
    '🔥 애니메이션이 살아있는 것 같아요!',
    '🎨 디자인 센스가 폭발하는군요!',
    '⚡ 성능도 좋고 반응성도 완벽!',
    '🌟 이런 페이지가 진짜 개발의 재미죠!'
];

function showRandomMessage() {
    const randomMessage = messages[Math.floor(Math.random() * messages.length)];
    document.getElementById('resultDisplay').innerHTML = randomMessage;
}

// 폭죽 이펙트
function createFireworks() {
    const resultDisplay = document.getElementById('resultDisplay');
    resultDisplay.innerHTML = '🎆 폭죽이 터집니다! 🎇';
    
    // 폭죽 파티클 생성
    for (let i = 0; i < 20; i++) {
        createParticle();
    }
}

function createParticle() {
    const particle = document.createElement('div');
    particle.innerHTML = ['🎆', '🎇', '✨', '⭐', '💫'][Math.floor(Math.random() * 5)];
    particle.style.position = 'fixed';
    particle.style.left = Math.random() * window.innerWidth + 'px';
    particle.style.top = Math.random() * window.innerHeight + 'px';
    particle.style.fontSize = '2rem';
    particle.style.pointerEvents = 'none';
    particle.style.zIndex = '1000';
    particle.style.animation = 'float 3s ease-out forwards';
    
    document.body.appendChild(particle);
    
    setTimeout(() => {
        particle.remove();
    }, 3000);
}

// 페이지 로드 시 환영 메시지
window.addEventListener('load', () => {
    setTimeout(() => {
        document.getElementById('resultDisplay').innerHTML = '🎉 페이지가 완전히 로드되었습니다! 모든 기능을 테스트해보세요!';
    }, 1000);
});

// 카드 클릭 이벤트
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.card').forEach((card, index) => {
        card.addEventListener('click', () => {
            const messages = [
                '🔥 애니메이션 카드를 클릭하셨네요! 멋진 이펙트가 작동 중입니다!',
                '🎨 글라스모피즘 디자인 카드! 투명한 아름다움을 느껴보세요!',
                '📱 반응형 레이아웃 카드! 어떤 화면 크기에서도 완벽합니다!'
            ];
            document.getElementById('resultDisplay').innerHTML = messages[index];
        });
    });
});
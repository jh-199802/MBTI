// 회원가입 페이지 JavaScript

let isSubmitting = false;

function initializeRegisterPage() {
    setupFormValidation();
    setupPasswordStrength();
    setupRealTimeValidation();
}

function setupFormValidation() {
    const form = document.getElementById('registerForm');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        if (!isSubmitting) {
            submitRegistration();
        }
    });
}

function setupPasswordStrength() {
    const passwordInput = document.getElementById('password');
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');

    if (passwordInput && strengthFill && strengthText) {
        passwordInput.addEventListener('input', function() {
            const password = this.value;
            const strength = calculatePasswordStrength(password);
            
            updatePasswordStrength(strengthFill, strengthText, strength);
        });
    }
}

function setupRealTimeValidation() {
    // 사용자명 실시간 중복 체크
    const usernameInput = document.getElementById('username');
    if (usernameInput) {
        let usernameTimeout;
        usernameInput.addEventListener('input', function() {
            clearTimeout(usernameTimeout);
            usernameTimeout = setTimeout(() => {
                validateUsername(this.value);
            }, 500);
        });
    }

    // 이메일 실시간 중복 체크
    const emailInput = document.getElementById('email');
    if (emailInput) {
        let emailTimeout;
        emailInput.addEventListener('input', function() {
            clearTimeout(emailTimeout);
            emailTimeout = setTimeout(() => {
                validateEmail(this.value);
            }, 500);
        });
    }

    // 비밀번호 확인 실시간 체크
    const confirmPasswordInput = document.getElementById('confirmPassword');
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            validatePasswordConfirm();
        });
    }
}

function calculatePasswordStrength(password) {
    let score = 0;
    
    if (password.length >= 8) score++;
    if (password.match(/[a-z]/)) score++;
    if (password.match(/[A-Z]/)) score++;
    if (password.match(/[0-9]/)) score++;
    if (password.match(/[^a-zA-Z0-9]/)) score++;
    
    return score;
}

function updatePasswordStrength(fill, text, strength) {
    const strengthLevels = [
        { width: '0%', text: '비밀번호를 입력하세요', class: '' },
        { width: '20%', text: '매우 약함', class: 'weak' },
        { width: '40%', text: '약함', class: 'weak' },
        { width: '60%', text: '보통', class: 'fair' },
        { width: '80%', text: '강함', class: 'good' },
        { width: '100%', text: '매우 강함', class: 'strong' }
    ];
    
    const level = strengthLevels[strength] || strengthLevels[0];
    
    fill.style.width = level.width;
    fill.className = `strength-fill ${level.class}`;
    text.textContent = level.text;
    text.className = `strength-text ${level.class}`;
}

async function validateUsername(username) {
    const validationDiv = document.getElementById('usernameValidation');
    if (!validationDiv) return;

    if (!username || username.length < 4) {
        validationDiv.style.display = 'none';
        return;
    }

    try {
        const response = await fetch(`/user/api/check-username?username=${encodeURIComponent(username)}`);
        const data = await response.json();
        
        if (data.available) {
            showValidation(validationDiv, data.message, 'success');
        } else {
            showValidation(validationDiv, data.message, 'error');
        }
    } catch (error) {
        console.error('사용자명 검증 오류:', error);
    }
}

async function validateEmail(email) {
    const validationDiv = document.getElementById('emailValidation');
    if (!validationDiv) return;

    if (!email || !isValidEmail(email)) {
        validationDiv.style.display = 'none';
        return;
    }

    try {
        const response = await fetch(`/user/api/check-email?email=${encodeURIComponent(email)}`);
        const data = await response.json();
        
        if (data.available) {
            showValidation(validationDiv, data.message, 'success');
        } else {
            showValidation(validationDiv, data.message, 'error');
        }
    } catch (error) {
        console.error('이메일 검증 오류:', error);
    }
}

function validatePasswordConfirm() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const validationDiv = document.getElementById('confirmPasswordValidation');
    
    if (!confirmPassword) {
        validationDiv.style.display = 'none';
        return;
    }
    
    if (password === confirmPassword) {
        showValidation(validationDiv, '비밀번호가 일치합니다', 'success');
    } else {
        showValidation(validationDiv, '비밀번호가 일치하지 않습니다', 'error');
    }
}

function showValidation(element, message, type) {
    element.textContent = message;
    element.className = `validation-message ${type}`;
    element.style.display = 'block';
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

async function submitRegistration() {
    if (isSubmitting) return;
    
    // 폼 데이터 수집
    const formData = {
        username: document.getElementById('username').value.trim(),
        email: document.getElementById('email').value.trim(),
        password: document.getElementById('password').value,
        nickname: document.getElementById('nickname').value.trim()
    };

    // 유효성 검사
    if (!validateForm(formData)) {
        return;
    }

    isSubmitting = true;
    showLoadingSpinner();

    try {
        const response = await fetch('/user/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (data.success) {
            showToast(data.message, 'success');
            
            // 회원가입 성공 후 메인 페이지로 이동
            setTimeout(() => {
                window.location.href = '/';
            }, 2000);
        } else {
            showToast(data.message || '회원가입에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('회원가입 오류:', error);
        showToast('회원가입 중 오류가 발생했습니다.', 'error');
    } finally {
        isSubmitting = false;
        hideLoadingSpinner();
    }
}

function validateForm(formData) {
    // 사용자명 검증
    if (!formData.username || formData.username.length < 4) {
        showToast('사용자명은 4자 이상이어야 합니다.', 'error');
        return false;
    }

    // 이메일 검증
    if (!formData.email || !isValidEmail(formData.email)) {
        showToast('올바른 이메일 주소를 입력해주세요.', 'error');
        return false;
    }

    // 비밀번호 검증
    if (!formData.password || formData.password.length < 8) {
        showToast('비밀번호는 8자 이상이어야 합니다.', 'error');
        return false;
    }

    // 비밀번호 확인
    const confirmPassword = document.getElementById('confirmPassword').value;
    if (formData.password !== confirmPassword) {
        showToast('비밀번호가 일치하지 않습니다.', 'error');
        return false;
    }

    // 약관 동의 확인
    const agreeTerms = document.getElementById('agreeTerms').checked;
    const agreePrivacy = document.getElementById('agreePrivacy').checked;
    
    if (!agreeTerms || !agreePrivacy) {
        showToast('이용약관과 개인정보처리방침에 동의해주세요.', 'error');
        return false;
    }

    return true;
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    
    if (toast && toastMessage) {
        toastMessage.textContent = message;
        toast.className = `toast show ${type}`;
        
        setTimeout(() => {
            toast.className = 'toast';
        }, 4000);
    }
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

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializeRegisterPage();
});

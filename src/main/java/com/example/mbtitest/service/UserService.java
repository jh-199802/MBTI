package com.example.mbtitest.service;

import com.example.mbtitest.entity.User;
import com.example.mbtitest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 정규표현식 패턴
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{4,20}$");
    
    /**
     * 회원가입
     */
    public User registerUser(String username, String email, String password, String nickname) {
        log.info("회원가입 시도 - username: {}, email: {}, nickname: {}", username, email, nickname);
        
        // 입력값 검증
        validateUserInput(username, email, password, nickname);
        
        // 중복 체크
        checkDuplicates(username, email);
        
        try {
            // 비밀번호 암호화
            String hashedPassword = passwordEncoder.encode(password);
            
            User user = User.builder()
                .username(username.toLowerCase().trim())
                .email(email.toLowerCase().trim())
                .passwordHash(hashedPassword)
                .nickname(nickname != null ? nickname.trim() : null)
                .isActive("Y")
                .build();
            
            User savedUser = userRepository.save(user);
            log.info("회원가입 완료 - userId: {}, username: {}", savedUser.getUserId(), savedUser.getUsername());
            
            return savedUser;
            
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생 - username: {}", username, e);
            throw new RuntimeException("회원가입 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 로그인 인증
     */
    @Transactional(readOnly = true)
    public Optional<User> authenticateUser(String loginId, String password) {
        log.debug("로그인 시도 - loginId: {}", loginId);
        
        try {
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(loginId.toLowerCase().trim());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                if (!user.isActive()) {
                    log.warn("비활성 사용자 로그인 시도 - userId: {}", user.getUserId());
                    return Optional.empty();
                }
                
                if (passwordEncoder.matches(password, user.getPasswordHash())) {
                    // 마지막 로그인 시간 업데이트
                    user.updateLastLogin();
                    userRepository.save(user);
                    
                    log.info("로그인 성공 - userId: {}, username: {}", user.getUserId(), user.getUsername());
                    return Optional.of(user);
                } else {
                    log.warn("비밀번호 불일치 - loginId: {}", loginId);
                }
            } else {
                log.warn("사용자 없음 - loginId: {}", loginId);
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("로그인 인증 중 오류 발생 - loginId: {}", loginId, e);
            return Optional.empty();
        }
    }
    
    /**
     * 사용자명 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByUsername(username.toLowerCase().trim());
    }
    
    /**
     * 이메일 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByEmail(email.toLowerCase().trim());
    }
    
    /**
     * 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 사용자 ID로 조회
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * 사용자 프로필 업데이트
     */
    public User updateProfile(Long userId, String nickname, String mbtiType) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (nickname != null && !nickname.trim().isEmpty()) {
                user.setNickname(nickname.trim());
            }
            
            if (mbtiType != null && isValidMbtiType(mbtiType)) {
                user.setMbtiType(mbtiType.toUpperCase());
            }
            
            User updatedUser = userRepository.save(user);
            log.info("프로필 업데이트 완료 - userId: {}", userId);
            return updatedUser;
        }
        throw new RuntimeException("사용자를 찾을 수 없습니다.");
    }
    
    /**
     * 비밀번호 변경
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
            }
            
            validatePassword(newPassword);
            
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            log.info("비밀번호 변경 완료 - userId: {}", userId);
        } else {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
    }
    
    /**
     * 계정 비활성화
     */
    public void deactivateAccount(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive("N");
            userRepository.save(user);
            
            log.info("계정 비활성화 완료 - userId: {}", userId);
        } else {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
    }
    
    /**
     * 입력값 검증
     */
    private void validateUserInput(String username, String email, String password, String nickname) {
        // 사용자명 검증
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("사용자명을 입력해주세요.");
        }
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new RuntimeException("사용자명은 4-20자의 영문, 숫자, 언더스코어만 사용 가능합니다.");
        }
        
        // 이메일 검증
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("이메일을 입력해주세요.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new RuntimeException("올바른 이메일 형식이 아닙니다.");
        }
        
        // 비밀번호 검증
        validatePassword(password);
        
        // 닉네임 검증 (선택사항)
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (nickname.trim().length() < 2 || nickname.trim().length() > 20) {
                throw new RuntimeException("닉네임은 2-20자 사이여야 합니다.");
            }
        }
    }
    
    /**
     * 비밀번호 검증
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("비밀번호를 입력해주세요.");
        }
        if (password.length() < 8 || password.length() > 50) {
            throw new RuntimeException("비밀번호는 8-50자 사이여야 합니다.");
        }
        
        // 비밀번호 강도 검사 (영문, 숫자, 특수문자 중 2가지 이상)
        int complexity = 0;
        if (password.matches(".*[a-zA-Z].*")) complexity++;
        if (password.matches(".*[0-9].*")) complexity++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) complexity++;
        
        if (complexity < 2) {
            throw new RuntimeException("비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 포함해야 합니다.");
        }
    }
    
    /**
     * 중복 체크
     */
    private void checkDuplicates(String username, String email) {
        if (!isUsernameAvailable(username)) {
            throw new RuntimeException("이미 사용 중인 사용자명입니다.");
        }
        if (!isEmailAvailable(email)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
    }
    
    /**
     * MBTI 타입 유효성 검사
     */
    private boolean isValidMbtiType(String mbtiType) {
        if (mbtiType == null || mbtiType.length() != 4) {
            return false;
        }
        
        String[] validTypes = {
            "ENFP", "ENFJ", "ENTP", "ENTJ", "ESFP", "ESFJ", "ESTP", "ESTJ",
            "INFP", "INFJ", "INTP", "INTJ", "ISFP", "ISFJ", "ISTP", "ISTJ"
        };
        
        String upperMbti = mbtiType.toUpperCase();
        for (String validType : validTypes) {
            if (validType.equals(upperMbti)) {
                return true;
            }
        }
        return false;
    }
}

package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 Entity (선택적 회원가입)
 */
@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;
    
    @Column(name = "USER_UUID", unique = true, nullable = false, length = 36)
    private String userUuid;
    
    @Column(name = "NICKNAME", length = 50)
    private String nickname;
    
    @Column(name = "EMAIL", length = 100)
    private String email;
    
    @Column(name = "SOCIAL_TYPE", length = 20)
    private String socialType; // KAKAO, GOOGLE, NAVER, GUEST
    
    @Column(name = "SOCIAL_ID", length = 100)
    private String socialId;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    // 연관관계
    @OneToMany(mappedBy = "userUuid", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TestResult> testResults;
    
    // 비즈니스 메서드
    public boolean isGuest() {
        return socialType == null || "GUEST".equals(socialType);
    }
    
    public boolean isSocialUser() {
        return !isGuest();
    }
    
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        
        if (email != null) {
            String[] parts = email.split("@");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        
        if (socialId != null) {
            return socialId;
        }
        
        return "익명 유저";
    }
    
    // 소셜 로그인 타입별 아이콘
    public String getSocialIcon() {
        if (socialType == null) return "👤";
        
        return switch (socialType.toUpperCase()) {
            case "KAKAO" -> "💬";
            case "GOOGLE" -> "🟢";
            case "NAVER" -> "🟩";
            case "FACEBOOK" -> "👍";
            case "APPLE" -> "🍎";
            default -> "👤";
        };
    }
    
    // 소셜 로그인 타입별 색상
    public String getSocialColor() {
        if (socialType == null) return "#6B7280";
        
        return switch (socialType.toUpperCase()) {
            case "KAKAO" -> "#FEE500";
            case "GOOGLE" -> "#4285F4";
            case "NAVER" -> "#03C75A";
            case "FACEBOOK" -> "#1877F2";
            case "APPLE" -> "#000000";
            default -> "#6B7280";
        };
    }
    
    // 가입 경로별 한글 이름
    public String getSocialName() {
        if (socialType == null) return "게스트";
        
        return switch (socialType.toUpperCase()) {
            case "KAKAO" -> "카카오";
            case "GOOGLE" -> "구글";
            case "NAVER" -> "네이버";
            case "FACEBOOK" -> "페이스북";
            case "APPLE" -> "애플";
            default -> "게스트";
        };
    }
    
    // 활동 레벨 계산 (테스트 횟수 기반)
    public String getActivityLevel() {
        if (testResults == null) return "새싹";
        
        int testCount = testResults.size();
        if (testCount >= 20) return "전문가";
        else if (testCount >= 10) return "열정가";
        else if (testCount >= 5) return "탐험가";
        else if (testCount >= 1) return "입문자";
        else return "새싹";
    }
    
    // 활동 레벨별 이모지
    public String getActivityEmoji() {
        String level = getActivityLevel();
        return switch (level) {
            case "전문가" -> "🎓";
            case "열정가" -> "🔥";
            case "탐험가" -> "🗺️";
            case "입문자" -> "⭐";
            default -> "🌱";
        };
    }
    
    // UUID 생성 유틸리티
    public static String generateUuid() {
        return java.util.UUID.randomUUID().toString();
    }
    
    // 프리빌드 게스트 사용자
    public static User createGuest() {
        return User.builder()
                .userUuid(generateUuid())
                .socialType("GUEST")
                .nickname("익명 유저")
                .build();
    }
}

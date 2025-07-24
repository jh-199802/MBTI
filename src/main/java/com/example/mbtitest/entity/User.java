package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user")
    @SequenceGenerator(name = "seq_user", sequenceName = "SEQ_USER", allocationSize = 1)
    @Column(name = "USER_ID")
    private Long userId;
    
    @Column(name = "USERNAME", length = 50, nullable = false, unique = true)
    private String username;
    
    @Column(name = "EMAIL", length = 100, nullable = false, unique = true)
    private String email;
    
    @Column(name = "PASSWORD_HASH", nullable = false)
    private String passwordHash;
    
    @Column(name = "NICKNAME", length = 50)
    private String nickname;
    
    @Column(name = "PROFILE_IMAGE")
    private String profileImage;
    
    @Column(name = "MBTI_TYPE", length = 4)
    private String mbtiType; // 사용자의 대표 MBTI (최근 테스트 결과)
    
    @Column(name = "IS_ACTIVE", length = 1)
    @Builder.Default
    private String isActive = "Y";
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    @Column(name = "LAST_LOGIN")
    private LocalDateTime lastLogin;
    
    // 연관관계 - 사용자가 작성한 댓글들
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;
    
    // 연관관계 - 사용자의 테스트 결과들
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestResult> testResults;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드
    public String getDisplayName() {
        return nickname != null ? nickname : username;
    }
    
    public boolean isActive() {
        return "Y".equals(isActive);
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}

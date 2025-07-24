package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TEST_RESULTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_test_result")
    @SequenceGenerator(name = "seq_test_result", sequenceName = "SEQ_TEST_RESULT", allocationSize = 1)
    @Column(name = "RESULT_ID")
    private Long resultId;
    
    // 사용자 연관관계 추가 (nullable - 익명 테스트 허용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
    
    @Column(name = "USER_UUID", length = 36, nullable = false)
    private String userUuid;
    
    @Column(name = "USER_IP", length = 50)
    private String userIp;
    
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;
    
    @Column(name = "MBTI_TYPE", length = 4, nullable = false)
    private String mbtiType;
    
    @Column(name = "MBTI_DESCRIPTION", length = 2000)
    private String mbtiDescription;
    
    @Column(name = "MBTI_COLOR", length = 7)
    private String mbtiColor;
    
    @Lob
    @Column(name = "CATEGORY_SCORES")
    private String categoryScores;
    
    @Lob
    @Column(name = "DETAILED_SCORES")
    private String detailedScores;
    
    @Lob
    @Column(name = "ANSWER_DATA")
    private String answerData;
    
    @Lob
    @Column(name = "AI_ANALYSIS")
    private String aiAnalysis;
    
    @Column(name = "TEST_DURATION")
    private Integer testDuration;
    
    @Column(name = "VIEW_COUNT")
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(name = "SHARED_COUNT")
    @Builder.Default
    private Integer sharedCount = 0;
    
    @Column(name = "IS_PUBLIC", length = 1)
    @Builder.Default
    private String isPublicFlag = "Y";
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    // 연관관계 - 이 테스트 결과에 달린 댓글들
    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;
    
    // Helper methods for boolean conversion
    public Boolean getIsPublic() {
        return "Y".equals(isPublicFlag);
    }
    
    public boolean isPublic() {
        return "Y".equals(isPublicFlag);
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublicFlag = isPublic ? "Y" : "N";
    }
    
    // 편의 메서드
    public boolean isOwnedBy(User checkUser) {
        return user != null && checkUser != null && user.getUserId().equals(checkUser.getUserId());
    }
    
    public boolean isAnonymous() {
        return user == null;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
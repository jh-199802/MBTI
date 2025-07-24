package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_STATS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_stats")
    @SequenceGenerator(name = "seq_user_stats", sequenceName = "SEQ_USER_STATS", allocationSize = 1)
    @Column(name = "STATS_ID")
    private Long statsId;
    
    @Column(name = "STAT_DATE")
    private LocalDate statDate;
    
    @Column(name = "TOTAL_TESTS")
    @Builder.Default
    private Integer totalTests = 0;
    
    @Column(name = "MBTI_ENFP")
    @Builder.Default
    private Integer mbtiEnfp = 0;
    
    @Column(name = "MBTI_ENFJ")
    @Builder.Default
    private Integer mbtiEnfj = 0;
    
    @Column(name = "MBTI_ENTP")
    @Builder.Default
    private Integer mbtiEntp = 0;
    
    @Column(name = "MBTI_ENTJ")
    @Builder.Default
    private Integer mbtiEntj = 0;
    
    @Column(name = "MBTI_ESFP")
    @Builder.Default
    private Integer mbtiEsfp = 0;
    
    @Column(name = "MBTI_ESFJ")
    @Builder.Default
    private Integer mbtiEsfj = 0;
    
    @Column(name = "MBTI_ESTP")
    @Builder.Default
    private Integer mbtiEstp = 0;
    
    @Column(name = "MBTI_ESTJ")
    @Builder.Default
    private Integer mbtiEstj = 0;
    
    @Column(name = "MBTI_INFP")
    @Builder.Default
    private Integer mbtiInfp = 0;
    
    @Column(name = "MBTI_INFJ")
    @Builder.Default
    private Integer mbtiInfj = 0;
    
    @Column(name = "MBTI_INTP")
    @Builder.Default
    private Integer mbtiIntp = 0;
    
    @Column(name = "MBTI_INTJ")
    @Builder.Default
    private Integer mbtiIntj = 0;
    
    @Column(name = "MBTI_ISFP")
    @Builder.Default
    private Integer mbtiIsfp = 0;
    
    @Column(name = "MBTI_ISFJ")
    @Builder.Default
    private Integer mbtiIsfj = 0;
    
    @Column(name = "MBTI_ISTP")
    @Builder.Default
    private Integer mbtiIstp = 0;
    
    @Column(name = "MBTI_ISTJ")
    @Builder.Default
    private Integer mbtiIstj = 0;
    
    @Column(name = "AVG_DURATION")
    private Double avgDuration;
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (statDate == null) {
            statDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일일 통계 Entity
 */
@Entity
@Table(name = "DAILY_STATS", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"STAT_DATE", "MBTI_TYPE"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STAT_ID")
    private Long statId;
    
    @Column(name = "STAT_DATE", nullable = false)
    private LocalDate statDate;
    
    @Column(name = "MBTI_TYPE", nullable = false, length = 4)
    private String mbtiType;
    
    @Column(name = "TEST_COUNT")
    @Builder.Default
    private Integer testCount = 0;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
    
    // 비즈니스 메서드
    public void incrementCount() {
        this.testCount = (this.testCount == null ? 0 : this.testCount) + 1;
    }
    
    public double getPercentage(int totalTests) {
        if (totalTests == 0 || testCount == null) return 0.0;
        return Math.round(((double) testCount / totalTests) * 100.0 * 10.0) / 10.0;
    }
    
    // MBTI 순위 계산을 위한 정적 메서드
    public static String getRankEmoji(int rank) {
        return switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            case 4, 5 -> "🏆";
            default -> "📊";
        };
    }
    
    // MBTI 유형별 색상
    public String getMbtiColor() {
        if (mbtiType == null) return "#6B7280";
        
        return switch (mbtiType) {
            case "ENFP" -> "#F59E0B";
            case "INFP" -> "#8B5CF6";
            case "ENFJ" -> "#EF4444";
            case "INFJ" -> "#6366F1";
            case "ENTP" -> "#10B981";
            case "INTP" -> "#6B7280";
            case "ENTJ" -> "#DC2626";
            case "INTJ" -> "#4C1D95";
            case "ESFP" -> "#F97316";
            case "ISFP" -> "#EC4899";
            case "ESFJ" -> "#06B6D4";
            case "ISFJ" -> "#3B82F6";
            case "ESTP" -> "#84CC16";
            case "ISTP" -> "#22C55E";
            case "ESTJ" -> "#7C3AED";
            case "ISTJ" -> "#1F2937";
            default -> "#6B7280";
        };
    }
    
    // MBTI 그룹 분류
    public String getMbtiGroup() {
        if (mbtiType == null || mbtiType.length() != 4) return "기타";
        
        char firstChar = mbtiType.charAt(0);
        char lastChar = mbtiType.charAt(3);
        
        if (firstChar == 'E' && lastChar == 'J') {
            return "외향형 판단자"; // EJ
        } else if (firstChar == 'E' && lastChar == 'P') {
            return "외향형 인식자"; // EP  
        } else if (firstChar == 'I' && lastChar == 'J') {
            return "내향형 판단자"; // IJ
        } else if (firstChar == 'I' && lastChar == 'P') {
            return "내향형 인식자"; // IP
        }
        
        return "기타";
    }
    
    // 성격 특성
    public String getMbtiTrait() {
        if (mbtiType == null || mbtiType.length() != 4) return "";
        
        return switch (mbtiType) {
            case "ENFP" -> "열정적, 창의적";
            case "INFP" -> "이상주의적, 조화로운";
            case "ENFJ" -> "카리스마 있는, 영감을 주는";
            case "INFJ" -> "통찰력 있는, 결단력 있는";
            case "ENTP" -> "혁신적, 전략적";
            case "INTP" -> "논리적, 유연한";
            case "ENTJ" -> "효율적, 활력적";
            case "INTJ" -> "독립적, 결단력 있는";
            case "ESFP" -> "친근한, 수용적";
            case "ISFP" -> "온화한, 배려심 있는";
            case "ESFJ" -> "따뜻한, 책임감 있는";
            case "ISFJ" -> "겸손한, 헌신적";
            case "ESTP" -> "활동적, 현실적";
            case "ISTP" -> "실용적, 사실적";
            case "ESTJ" -> "체계적, 실용적";
            case "ISTJ" -> "신뢰할 수 있는, 철저한";
            default -> "";
        };
    }
}

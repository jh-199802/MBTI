package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 통계 캐시 Entity
 * - 성능 최적화를 위한 통계 데이터 캐싱
 */
@Entity
@Table(name = "STATISTICS_CACHE",
       uniqueConstraints = @UniqueConstraint(columnNames = {"STAT_TYPE", "STAT_KEY"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STAT_ID")
    private Long statId;
    
    @Column(name = "STAT_TYPE", nullable = false, length = 50)
    private String statType; // DAILY_COUNT, MBTI_DISTRIBUTION 등
    
    @Column(name = "STAT_KEY", length = 100)
    private String statKey; // 날짜, MBTI 타입 등 추가 키
    
    @Column(name = "STAT_VALUE")
    private Long statValue; // 수치 데이터
    
    @Lob
    @Column(name = "STAT_DATA")
    private String statData; // JSON 형태의 복잡한 데이터
    
    @CreationTimestamp
    @Column(name = "CALCULATED_AT")
    private LocalDateTime calculatedAt;
    
    @Column(name = "EXPIRES_AT")
    private LocalDateTime expiresAt; // 캐시 만료 시간
    
    // 통계 타입 상수
    public static class StatType {
        public static final String TOTAL_TESTS = "TOTAL_TESTS";
        public static final String TOTAL_USERS = "TOTAL_USERS";
        public static final String MBTI_COUNT = "MBTI_COUNT";
        public static final String DAILY_COUNT = "DAILY_COUNT";
        public static final String HOURLY_COUNT = "HOURLY_COUNT";
        public static final String MBTI_DISTRIBUTION = "MBTI_DISTRIBUTION";
    }
}

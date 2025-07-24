package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHARE_LOGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_share_log")
    @SequenceGenerator(name = "seq_share_log", sequenceName = "SEQ_SHARE_LOG", allocationSize = 1)
    @Column(name = "SHARE_ID")
    private Long shareId;
    
    @Column(name = "RESULT_ID")
    private Long resultId;
    
    @Column(name = "MBTI_TYPE", length = 4, nullable = false)
    private String mbtiType;
    
    @Column(name = "PLATFORM", length = 50, nullable = false)
    private String sharePlatform; // kakao, facebook, twitter, instagram, etc.
    
    @Column(name = "USER_IP", length = 50)
    private String userIp;
    
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;
    
    @Column(name = "REFERRER", length = 500)
    private String referrer;
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    // TestResult와의 관계 (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESULT_ID", insertable = false, updatable = false)
    private TestResult testResult;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

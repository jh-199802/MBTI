package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "VIEW_LOGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_view_log")
    @SequenceGenerator(name = "seq_view_log", sequenceName = "SEQ_VIEW_LOG", allocationSize = 1)
    @Column(name = "VIEW_ID")
    private Long viewId;
    
    @Column(name = "RESULT_ID")
    private Long resultId;
    
    @Column(name = "MBTI_TYPE", length = 20, nullable = true)
    private String mbtiType;
    
    @Column(name = "USER_IP", length = 50)
    private String userIp;
    
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;
    
    @Column(name = "REFERRER", length = 500)
    private String referrer;
    
    @Column(name = "VIEW_DURATION")
    private Integer viewDuration;
    
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

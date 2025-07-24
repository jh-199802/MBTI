package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "COMMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_comment")
    @SequenceGenerator(name = "seq_comment", sequenceName = "SEQ_COMMENT", allocationSize = 1)
    @Column(name = "COMMENT_ID")
    private Long commentId;
    
    @Column(name = "RESULT_ID")
    private Long resultId;
    
    @Column(name = "MBTI_TYPE", length = 4, nullable = false)
    private String mbtiType;
    
    @Column(name = "NICKNAME", length = 50)
    private String nickname;
    
    @Column(name = "COMMENT_TEXT", nullable = false)
    @Lob
    private String commentText;
    
    @Column(name = "USER_IP", length = 50)
    private String userIp;
    
    @Column(name = "LIKES_COUNT")
    @Builder.Default
    private Integer likesCount = 0;
    
    @Column(name = "IS_DELETED", length = 1)
    @Builder.Default
    private String isDeleted = "N";
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    // TestResult와의 관계 (optional - 성능상 필요시에만)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESULT_ID", insertable = false, updatable = false)
    private TestResult testResult;
    
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

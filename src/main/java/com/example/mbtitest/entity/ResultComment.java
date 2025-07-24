package com.example.mbtitest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 댓글/후기 Entity
 */
@Entity
@Table(name = "RESULT_COMMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    private Long commentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESULT_ID")
    private TestResult testResult;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
    
    @Column(name = "COMMENT_TEXT", nullable = false, length = 1000)
    private String commentText;
    
    @Column(name = "RATING")
    private Integer rating; // 1-5 별점
    
    @Column(name = "MBTI_TYPE", length = 4)
    private String mbtiType;
    
    @Column(name = "IS_ANONYMOUS", length = 1)
    @Builder.Default
    private String isAnonymous = "N";
    
    @Column(name = "IS_APPROVED", length = 1)
    @Builder.Default
    private String isApproved = "Y";
    
    @Column(name = "LIKES_COUNT")
    @Builder.Default
    private Integer likesCount = 0;
    
    @CreationTimestamp
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}

package com.example.mbtitest.repository;

import com.example.mbtitest.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 특정 MBTI 타입의 댓글들 조회 (삭제되지 않은 것만)
    List<Comment> findByMbtiTypeAndIsDeletedOrderByCreatedAtDesc(String mbtiType, String isDeleted);
    
    // 특정 테스트 결과의 댓글들 조회
    List<Comment> findByResultIdAndIsDeletedOrderByCreatedAtDesc(Long resultId, String isDeleted);
    
    // 최근 댓글들 조회 (전체)
    List<Comment> findByIsDeletedOrderByCreatedAtDesc(String isDeleted);
    
    // 인기 댓글들 조회 (좋아요 수 기준)
    List<Comment> findByIsDeletedOrderByLikesCountDescCreatedAtDesc(String isDeleted);
    
    // 특정 MBTI 타입의 댓글 개수
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.mbtiType = :mbtiType AND c.isDeleted = 'N'")
    Long countByMbtiType(@Param("mbtiType") String mbtiType);
    
    // 전체 댓글 개수 (삭제되지 않은 것만)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.isDeleted = 'N'")
    Long countActiveComments();
    
    // 오늘의 댓글 개수
    @Query(value = "SELECT COUNT(*) FROM COMMENTS WHERE TO_CHAR(CREATED_AT, 'YYYY-MM-DD') = TO_CHAR(SYSDATE, 'YYYY-MM-DD') AND IS_DELETED = 'N'", nativeQuery = true)
    Long countTodayComments();
    
    // 특정 IP의 댓글들 조회 (스팸 방지용)
    List<Comment> findByUserIpAndCreatedAtAfterOrderByCreatedAtDesc(String userIp, LocalDateTime afterTime);
    
    // MBTI 타입별 댓글 통계
    @Query("SELECT c.mbtiType, COUNT(c) FROM Comment c WHERE c.isDeleted = 'N' GROUP BY c.mbtiType ORDER BY COUNT(c) DESC")
    List<Object[]> getCommentStatsByMbtiType();
    
    // 가장 활발한 MBTI 타입 (댓글 기준)
    @Query("SELECT c.mbtiType FROM Comment c WHERE c.isDeleted = 'N' GROUP BY c.mbtiType ORDER BY COUNT(c) DESC")
    List<String> getMostActiveCommentMbtiTypes();
    
    // 특정 기간의 댓글들
    List<Comment> findByCreatedAtBetweenAndIsDeletedOrderByCreatedAtDesc(
        LocalDateTime startDate, LocalDateTime endDate, String isDeleted);
    
    // 좋아요가 많은 순서로 특정 개수만 조회
    @Query(value = "SELECT * FROM (SELECT c.* FROM COMMENTS c WHERE c.IS_DELETED = 'N' ORDER BY c.LIKES_COUNT DESC, c.CREATED_AT DESC) WHERE ROWNUM <= :limit", nativeQuery = true)
    List<Comment> findTopCommentsByLikes(@Param("limit") int limit);
}

package com.example.mbtitest.repository;

import com.example.mbtitest.entity.TestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    
    // MBTI 타입별 개수 조회
    @Query("SELECT tr.mbtiType, COUNT(tr) FROM TestResult tr GROUP BY tr.mbtiType ORDER BY COUNT(tr) DESC")
    List<Object[]> countByMbtiType();
    
    // 최근 결과 조회 (특정 개수만)
    List<TestResult> findTop10ByOrderByCreatedAtDesc();
    
    // 특정 MBTI 타입의 최근 결과들
    List<TestResult> findByMbtiTypeOrderByCreatedAtDesc(String mbtiType);
    
    // 날짜 범위로 조회
    List<TestResult> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // 특정 IP의 테스트 결과들
    List<TestResult> findByUserIpOrderByCreatedAtDesc(String userIp);
    
    // 특정 사용자 UUID의 테스트 결과들
    List<TestResult> findByUserUuidOrderByCreatedAtDesc(String userUuid);
    
    // 공개/비공개 상태로 조회 (페이징)
    Page<TestResult> findByIsPublicFlag(String isPublicFlag, Pageable pageable);
    
    // 특정 MBTI 타입이면서 공개된 결과들 (페이징)
    Page<TestResult> findByMbtiTypeAndIsPublicFlag(String mbtiType, String isPublicFlag, Pageable pageable);
    
    // 특정 결과 ID와 사용자 UUID로 조회
    Optional<TestResult> findByResultIdAndUserUuid(Long resultId, String userUuid);
    
    // 오늘의 테스트 개수
    @Query(value = "SELECT COUNT(*) FROM TEST_RESULTS WHERE TO_CHAR(CREATED_AT, 'YYYY-MM-DD') = TO_CHAR(SYSDATE, 'YYYY-MM-DD')", nativeQuery = true)
    Long countTodayTests();
    
    // 전체 테스트 개수
    @Query("SELECT COUNT(tr) FROM TestResult tr")
    Long countAllTests();
    
    // 평균 테스트 시간
    @Query("SELECT AVG(tr.testDuration) FROM TestResult tr WHERE tr.testDuration IS NOT NULL")
    Double getAverageTestDuration();
    
    // MBTI 타입별 통계 (Map 형태로 반환)
    @Query("SELECT " +
           "SUM(CASE WHEN tr.mbtiType = 'ENFP' THEN 1 ELSE 0 END) as enfp, " +
           "SUM(CASE WHEN tr.mbtiType = 'ENFJ' THEN 1 ELSE 0 END) as enfj, " +
           "SUM(CASE WHEN tr.mbtiType = 'ENTP' THEN 1 ELSE 0 END) as entp, " +
           "SUM(CASE WHEN tr.mbtiType = 'ENTJ' THEN 1 ELSE 0 END) as entj, " +
           "SUM(CASE WHEN tr.mbtiType = 'ESFP' THEN 1 ELSE 0 END) as esfp, " +
           "SUM(CASE WHEN tr.mbtiType = 'ESFJ' THEN 1 ELSE 0 END) as esfj, " +
           "SUM(CASE WHEN tr.mbtiType = 'ESTP' THEN 1 ELSE 0 END) as estp, " +
           "SUM(CASE WHEN tr.mbtiType = 'ESTJ' THEN 1 ELSE 0 END) as estj, " +
           "SUM(CASE WHEN tr.mbtiType = 'INFP' THEN 1 ELSE 0 END) as infp, " +
           "SUM(CASE WHEN tr.mbtiType = 'INFJ' THEN 1 ELSE 0 END) as infj, " +
           "SUM(CASE WHEN tr.mbtiType = 'INTP' THEN 1 ELSE 0 END) as intp, " +
           "SUM(CASE WHEN tr.mbtiType = 'INTJ' THEN 1 ELSE 0 END) as intj, " +
           "SUM(CASE WHEN tr.mbtiType = 'ISFP' THEN 1 ELSE 0 END) as isfp, " +
           "SUM(CASE WHEN tr.mbtiType = 'ISFJ' THEN 1 ELSE 0 END) as isfj, " +
           "SUM(CASE WHEN tr.mbtiType = 'ISTP' THEN 1 ELSE 0 END) as istp, " +
           "SUM(CASE WHEN tr.mbtiType = 'ISTJ' THEN 1 ELSE 0 END) as istj " +
           "FROM TestResult tr")
    Object[] getMbtiTypeStatistics();
    
    // 특정 기간의 일별 테스트 수
    @Query(value = "SELECT TO_CHAR(CREATED_AT, 'YYYY-MM-DD') as testDate, COUNT(*) as testCount " +
           "FROM TEST_RESULTS " +
           "WHERE CREATED_AT >= :startDate " +
           "GROUP BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD') " +
           "ORDER BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD')", nativeQuery = true)
    List<Object[]> getDailyTestCounts(@Param("startDate") LocalDateTime startDate);
    
    // 조회수가 높은 공개 결과들
    @Query("SELECT tr FROM TestResult tr WHERE tr.isPublicFlag = 'Y' ORDER BY tr.viewCount DESC, tr.createdAt DESC")
    Page<TestResult> findPopularResults(Pageable pageable);
    
    // 공유수가 높은 공개 결과들
    @Query("SELECT tr FROM TestResult tr WHERE tr.isPublicFlag = 'Y' ORDER BY tr.sharedCount DESC, tr.viewCount DESC, tr.createdAt DESC")
    Page<TestResult> findViralResults(Pageable pageable);
    
    // 총 조회수
    @Query("SELECT SUM(tr.viewCount) FROM TestResult tr")
    Long getTotalViewCount();
    
    // 총 공유수
    @Query("SELECT SUM(tr.sharedCount) FROM TestResult tr")
    Long getTotalSharedCount();
}
package com.example.mbtitest.repository;

import com.example.mbtitest.entity.ShareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShareLogRepository extends JpaRepository<ShareLog, Long> {
    
    // 특정 플랫폼의 공유 로그들
    List<ShareLog> findBySharePlatformOrderByCreatedAtDesc(String sharePlatform);
    
    // 특정 테스트 결과의 공유 로그들
    List<ShareLog> findByResultIdOrderByCreatedAtDesc(Long resultId);
    
    // 최근 공유 로그들
    List<ShareLog> findTop50ByOrderByCreatedAtDesc();
    
    // 전체 공유 개수
    @Query("SELECT COUNT(sl) FROM ShareLog sl")
    Long countTotalShares();
    
    // 오늘의 공유 개수
    @Query(value = "SELECT COUNT(*) FROM SHARE_LOGS WHERE TO_CHAR(CREATED_AT, 'YYYY-MM-DD') = TO_CHAR(SYSDATE, 'YYYY-MM-DD')", nativeQuery = true)
    Long countTodayShares();
    
    // 플랫폼별 공유 통계
    @Query("SELECT sl.sharePlatform, COUNT(sl) FROM ShareLog sl GROUP BY sl.sharePlatform ORDER BY COUNT(sl) DESC")
    List<Object[]> getShareStatsByPlatform();
    
    // 가장 인기있는 공유 플랫폼
    @Query("SELECT sl.sharePlatform FROM ShareLog sl GROUP BY sl.sharePlatform ORDER BY COUNT(sl) DESC")
    List<String> getMostPopularSharePlatforms();
    
    // 특정 기간의 공유 로그들
    List<ShareLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // 특정 IP의 공유 로그들 (중복 공유 체크용)
    List<ShareLog> findByUserIpAndCreatedAtAfterOrderByCreatedAtDesc(String userIp, LocalDateTime afterTime);
    
    // 일별 공유 통계
    @Query(value = "SELECT TO_CHAR(CREATED_AT, 'YYYY-MM-DD') as shareDate, COUNT(*) as shareCount " +
           "FROM SHARE_LOGS " +
           "WHERE CREATED_AT >= :startDate " +
           "GROUP BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD') " +
           "ORDER BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD')", nativeQuery = true)
    List<Object[]> getDailyShareCounts(@Param("startDate") LocalDateTime startDate);
    
    // 가장 많이 공유된 테스트 결과들
    @Query("SELECT sl.resultId, COUNT(sl) as shareCount " +
           "FROM ShareLog sl " +
           "GROUP BY sl.resultId " +
           "ORDER BY COUNT(sl) DESC")
    List<Object[]> getMostSharedResults();
}

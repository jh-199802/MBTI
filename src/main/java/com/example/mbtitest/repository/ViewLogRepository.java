package com.example.mbtitest.repository;

import com.example.mbtitest.entity.ViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {
    
    // 특정 테스트 결과의 뷰 로그들
    List<ViewLog> findByResultIdOrderByCreatedAtDesc(Long resultId);
    
    // 특정 MBTI 타입의 뷰 로그들
    List<ViewLog> findByMbtiTypeOrderByCreatedAtDesc(String mbtiType);
    
    // 최근 방문 로그들
    List<ViewLog> findTop100ByOrderByCreatedAtDesc();
    
    // 전체 페이지뷰 개수
    @Query("SELECT COUNT(vl) FROM ViewLog vl")
    Long countTotalViews();
    
    // 오늘의 페이지뷰 개수
    @Query(value = "SELECT COUNT(*) FROM VIEW_LOGS WHERE TO_CHAR(CREATED_AT, 'YYYY-MM-DD') = TO_CHAR(SYSDATE, 'YYYY-MM-DD')", nativeQuery = true)
    Long countTodayViews();
    
    // MBTI 타입별 방문 통계
    @Query("SELECT vl.mbtiType, COUNT(vl) FROM ViewLog vl WHERE vl.mbtiType IS NOT NULL GROUP BY vl.mbtiType ORDER BY COUNT(vl) DESC")
    List<Object[]> getViewStatsByMbtiType();
    
    // 가장 인기있는 페이지 (임시로 MBTI 타입 기준)
    @Query("SELECT vl.mbtiType FROM ViewLog vl WHERE vl.mbtiType IS NOT NULL GROUP BY vl.mbtiType ORDER BY COUNT(vl) DESC")
    List<String> getMostPopularPages();
    
    // 특정 기간의 뷰 로그들
    List<ViewLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // 유니크 방문자 수 (IP 기준)
    @Query(value = "SELECT COUNT(DISTINCT USER_IP) FROM VIEW_LOGS WHERE TO_CHAR(CREATED_AT, 'YYYY-MM-DD') = TO_CHAR(SYSDATE, 'YYYY-MM-DD')", nativeQuery = true)
    Long countTodayUniqueVisitors();
    
    // 시간대별 방문 통계
    @Query(value = "SELECT EXTRACT(HOUR FROM CREATED_AT) as visitHour, COUNT(*) as visitCount " +
           "FROM VIEW_LOGS " +
           "WHERE CREATED_AT >= :startDate " +
           "GROUP BY EXTRACT(HOUR FROM CREATED_AT) " +
           "ORDER BY EXTRACT(HOUR FROM CREATED_AT)", nativeQuery = true)
    List<Object[]> getHourlyViewStats(@Param("startDate") LocalDateTime startDate);
    
    // 일별 방문 통계
    @Query(value = "SELECT TO_CHAR(CREATED_AT, 'YYYY-MM-DD') as visitDate, COUNT(*) as visitCount, COUNT(DISTINCT USER_IP) as uniqueVisitors " +
           "FROM VIEW_LOGS " +
           "WHERE CREATED_AT >= :startDate " +
           "GROUP BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD') " +
           "ORDER BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD')", nativeQuery = true)
    List<Object[]> getDailyViewStats(@Param("startDate") LocalDateTime startDate);
    
    // 가장 많이 조회된 MBTI 타입들
    @Query("SELECT vl.mbtiType, COUNT(vl) FROM ViewLog vl WHERE vl.mbtiType IS NOT NULL GROUP BY vl.mbtiType ORDER BY COUNT(vl) DESC")
    List<Object[]> getMostViewedMbtiTypes();
    
    // 유입 경로 분석
    @Query("SELECT vl.referrer, COUNT(vl) FROM ViewLog vl WHERE vl.referrer IS NOT NULL GROUP BY vl.referrer ORDER BY COUNT(vl) DESC")
    List<Object[]> getTopReferers();
}

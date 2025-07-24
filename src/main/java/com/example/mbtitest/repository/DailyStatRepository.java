package com.example.mbtitest.repository;

import com.example.mbtitest.entity.DailyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DailyStatRepository extends JpaRepository<DailyStat, Long> {
    
    // 특정 날짜의 MBTI별 통계 조회
    Optional<DailyStat> findByStatDateAndMbtiType(LocalDate statDate, String mbtiType);
    
    // 특정 기간의 MBTI별 통계 (합계)
    @Query("SELECT ds.mbtiType as mbtiType, SUM(ds.testCount) as totalCount " +
           "FROM DailyStat ds " +
           "WHERE ds.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ds.mbtiType " +
           "ORDER BY SUM(ds.testCount) DESC")
    List<Map<String, Object>> findMbtiStatsInPeriod(@Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);
    
    // 최근 7일간 MBTI별 통계
    @Query("SELECT ds.mbtiType as mbtiType, SUM(ds.testCount) as totalCount " +
           "FROM DailyStat ds " +
           "WHERE ds.statDate >= :weekAgo " +
           "GROUP BY ds.mbtiType " +
           "ORDER BY SUM(ds.testCount) DESC")
    List<Map<String, Object>> findWeeklyMbtiStats(@Param("weekAgo") LocalDate weekAgo);
    
    // 최근 30일간 일별 테스트 추이
    @Query("SELECT ds.statDate as date, SUM(ds.testCount) as totalCount " +
           "FROM DailyStat ds " +
           "WHERE ds.statDate >= :monthAgo " +
           "GROUP BY ds.statDate " +
           "ORDER BY ds.statDate")
    List<Map<String, Object>> findDailyTrendInMonth(@Param("monthAgo") LocalDate monthAgo);
    
    // 특정 MBTI의 일별 추이
    @Query("SELECT ds.statDate as date, ds.testCount as count " +
           "FROM DailyStat ds " +
           "WHERE ds.mbtiType = :mbtiType AND ds.statDate >= :since " +
           "ORDER BY ds.statDate")
    List<Map<String, Object>> findMbtiDailyTrend(@Param("mbtiType") String mbtiType, 
                                                  @Param("since") LocalDate since);
    
    // 전체 테스트 수 (누적)
    @Query("SELECT SUM(ds.testCount) FROM DailyStat ds")
    Long getTotalTestCount();
    
    // 특정 기간 전체 테스트 수
    @Query("SELECT SUM(ds.testCount) FROM DailyStat ds " +
           "WHERE ds.statDate BETWEEN :startDate AND :endDate")
    Long getTotalTestCountInPeriod(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    // 가장 인기있는 MBTI Top N
    @Query("SELECT ds.mbtiType as mbtiType, SUM(ds.testCount) as totalCount " +
           "FROM DailyStat ds " +
           "GROUP BY ds.mbtiType " +
           "ORDER BY SUM(ds.testCount) DESC")
    List<Map<String, Object>> findTopMbtiTypes();
    
    // 요일별 테스트 활동 (Oracle 네이티브 쿼리 사용)
    @Query(value = "SELECT " +
           "CASE TO_CHAR(stat_date, 'D') " +
           "WHEN '1' THEN '일요일' " +
           "WHEN '2' THEN '월요일' " +
           "WHEN '3' THEN '화요일' " +
           "WHEN '4' THEN '수요일' " +
           "WHEN '5' THEN '목요일' " +
           "WHEN '6' THEN '금요일' " +
           "WHEN '7' THEN '토요일' " +
           "END as dayOfWeek, " +
           "AVG(test_count) as avgCount " +
           "FROM daily_stats " +
           "WHERE stat_date >= :monthAgo " +
           "GROUP BY TO_CHAR(stat_date, 'D') " +
           "ORDER BY AVG(test_count) DESC", nativeQuery = true)
    List<Map<String, Object>> findWeeklyActivity(@Param("monthAgo") LocalDate monthAgo);
    
    // 월별 테스트 수 통계 (Oracle 네이티브 쿼리 사용)
    @Query(value = "SELECT " +
           "EXTRACT(YEAR FROM stat_date) as year, " +
           "EXTRACT(MONTH FROM stat_date) as month, " +
           "SUM(test_count) as totalCount " +
           "FROM daily_stats " +
           "WHERE stat_date >= :yearAgo " +
           "GROUP BY EXTRACT(YEAR FROM stat_date), EXTRACT(MONTH FROM stat_date) " +
           "ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> findMonthlyStats(@Param("yearAgo") LocalDate yearAgo);
    
    // MBTI 성향별 그룹 통계 (E/I, S/N, T/F, J/P)
    @Query("SELECT " +
           "SUBSTRING(ds.mbtiType, 1, 1) as trait, " +
           "SUM(ds.testCount) as count " +
           "FROM DailyStat ds " +
           "GROUP BY SUBSTRING(ds.mbtiType, 1, 1) " +
           "ORDER BY count DESC")
    List<Map<String, Object>> findExtroversionStats();
    
    @Query("SELECT " +
           "SUBSTRING(ds.mbtiType, 2, 1) as trait, " +
           "SUM(ds.testCount) as count " +
           "FROM DailyStat ds " +
           "GROUP BY SUBSTRING(ds.mbtiType, 2, 1) " +
           "ORDER BY count DESC")
    List<Map<String, Object>> findSensingStats();
    
    @Query("SELECT " +
           "SUBSTRING(ds.mbtiType, 3, 1) as trait, " +
           "SUM(ds.testCount) as count " +
           "FROM DailyStat ds " +
           "GROUP BY SUBSTRING(ds.mbtiType, 3, 1) " +
           "ORDER BY count DESC")
    List<Map<String, Object>> findThinkingStats();
    
    @Query("SELECT " +
           "SUBSTRING(ds.mbtiType, 4, 1) as trait, " +
           "SUM(ds.testCount) as count " +
           "FROM DailyStat ds " +
           "GROUP BY SUBSTRING(ds.mbtiType, 4, 1) " +
           "ORDER BY count DESC")
    List<Map<String, Object>> findJudgingStats();
    
    // 성장률 계산 (지난 주 대비 이번 주) - Native Query로 변경
    @Query(value = "SELECT " +
           "tw.mbti_type as mbtiType, " +
           "tw.thisWeekCount as thisWeek, " +
           "NVL(lw.lastWeekCount, 0) as lastWeek, " +
           "CASE " +
           "WHEN NVL(lw.lastWeekCount, 0) = 0 THEN 100.0 " +
           "ELSE ((tw.thisWeekCount - NVL(lw.lastWeekCount, 0)) * 100.0 / lw.lastWeekCount) " +
           "END as growthRate " +
           "FROM " +
           "(SELECT mbti_type, SUM(test_count) as thisWeekCount " +
           " FROM daily_stats " +
           " WHERE stat_date >= :thisWeekStart " +
           " GROUP BY mbti_type) tw " +
           "LEFT JOIN " +
           "(SELECT mbti_type, SUM(test_count) as lastWeekCount " +
           " FROM daily_stats " +
           " WHERE stat_date >= :lastWeekStart AND stat_date < :thisWeekStart " +
           " GROUP BY mbti_type) lw " +
           "ON tw.mbti_type = lw.mbti_type " +
           "ORDER BY growthRate DESC", nativeQuery = true)
    List<Map<String, Object>> findGrowthRates(@Param("lastWeekStart") LocalDate lastWeekStart,
                                               @Param("thisWeekStart") LocalDate thisWeekStart);
}

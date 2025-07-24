package com.example.mbtitest.repository;

import com.example.mbtitest.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    
    // 특정 날짜의 통계 조회
    Optional<UserStats> findByStatDate(LocalDate statDate);
    
    // 오늘 통계 조회
    @Query("SELECT us FROM UserStats us WHERE us.statDate = CURRENT_DATE")
    Optional<UserStats> findTodayStats();
    
    // 최근 통계 조회 (지정된 개수만큼)
    @Query("SELECT us FROM UserStats us ORDER BY us.statDate DESC")
    List<UserStats> findRecentStats(@Param("limit") int limit);
    
    // 특정 기간의 통계 조회
    List<UserStats> findByStatDateBetweenOrderByStatDate(LocalDate startDate, LocalDate endDate);
    
    // 가장 많이 나온 MBTI 타입 조회
    @Query("SELECT " +
           "CASE " +
           "  WHEN us.mbtiEnfp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ENFP' " +
           "  WHEN us.mbtiEnfj = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ENFJ' " +
           "  WHEN us.mbtiEntp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ENTP' " +
           "  WHEN us.mbtiEntj = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ENTJ' " +
           "  WHEN us.mbtiEsfp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ESFP' " +
           "  WHEN us.mbtiEsfj = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ESFJ' " +
           "  WHEN us.mbtiEstp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ESTP' " +
           "  WHEN us.mbtiEstj = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ESTJ' " +
           "  WHEN us.mbtiInfp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'INFP' " +
           "  WHEN us.mbtiInfj = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'INFJ' " +
           "  WHEN us.mbtiIntp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'INTP' " +
           "  WHEN us.mbtiIntj = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'INTJ' " +
           "  WHEN us.mbtiIsfp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ISFP' " +
           "  WHEN us.mbtiIsfj = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ISFJ' " +
           "  WHEN us.mbtiIstp = GREATEST(us.mbtiEnfp,us.mbtiEnfj,us.mbtiEntp,us.mbtiEntj,us.mbtiEsfp,us.mbtiEsfj,us.mbtiEstp,us.mbtiEstj,us.mbtiInfp,us.mbtiInfj,us.mbtiIntp,us.mbtiIntj,us.mbtiIsfp,us.mbtiIsfj,us.mbtiIstp,us.mbtiIstj) THEN 'ISTP' " +
           "  ELSE 'ISTJ' " +
           "END as mostPopularType " +
           "FROM UserStats us WHERE us.statDate = CURRENT_DATE")
    Optional<String> findMostPopularMbtiType();
    
    // 총 통계 조회 (모든 날짜 합계)
    @Query("SELECT " +
           "SUM(us.totalTests) as totalTests, " +
           "SUM(us.mbtiEnfp) as totalEnfp, " +
           "SUM(us.mbtiEnfj) as totalEnfj, " +
           "SUM(us.mbtiEntp) as totalEntp, " +
           "SUM(us.mbtiEntj) as totalEntj, " +
           "SUM(us.mbtiEsfp) as totalEsfp, " +
           "SUM(us.mbtiEsfj) as totalEsfj, " +
           "SUM(us.mbtiEstp) as totalEstp, " +
           "SUM(us.mbtiEstj) as totalEstj, " +
           "SUM(us.mbtiInfp) as totalInfp, " +
           "SUM(us.mbtiInfj) as totalInfj, " +
           "SUM(us.mbtiIntp) as totalIntp, " +
           "SUM(us.mbtiIntj) as totalIntj, " +
           "SUM(us.mbtiIsfp) as totalIsfp, " +
           "SUM(us.mbtiIsfj) as totalIsfj, " +
           "SUM(us.mbtiIstp) as totalIstp, " +
           "SUM(us.mbtiIstj) as totalIstj, " +
           "AVG(us.avgDuration) as overallAvgDuration " +
           "FROM UserStats us")
    Object[] getTotalStatistics();
}

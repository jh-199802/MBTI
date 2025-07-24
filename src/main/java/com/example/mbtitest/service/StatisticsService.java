package com.example.mbtitest.service;

import com.example.mbtitest.entity.UserStats;
import com.example.mbtitest.repository.TestResultRepository;
import com.example.mbtitest.repository.UserStatsRepository;
import com.example.mbtitest.repository.CommentRepository;
import com.example.mbtitest.repository.ShareLogRepository;
import com.example.mbtitest.repository.ViewLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatisticsService {
    
    private final TestResultRepository testResultRepository;
    private final UserStatsRepository userStatsRepository;
    private final CommentRepository commentRepository;
    private final ShareLogRepository shareLogRepository;
    private final ViewLogRepository viewLogRepository;
    
    /**
     * 대시보드용 전체 통계 조회
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 기본 통계
            stats.put("totalTests", testResultRepository.countAllTests());
            stats.put("todayTests", testResultRepository.countTodayTests());
            stats.put("totalComments", commentRepository.countActiveComments());
            stats.put("todayComments", commentRepository.countTodayComments());
            stats.put("totalShares", shareLogRepository.countTotalShares());
            stats.put("todayShares", shareLogRepository.countTodayShares());
            stats.put("totalViews", viewLogRepository.countTotalViews());
            stats.put("todayViews", viewLogRepository.countTodayViews());
            stats.put("uniqueVisitorsToday", viewLogRepository.countTodayUniqueVisitors());
            stats.put("avgTestDuration", testResultRepository.getAverageTestDuration());
            
            // MBTI 타입별 통계
            stats.put("mbtiStats", getMbtiTypeStatistics());
            
            // 인기 통계
            stats.put("mostPopularMbti", getMostPopularMbtiType());
            stats.put("mostActiveCommentMbti", getMostActiveCommentMbtiTypes());
            stats.put("mostPopularSharePlatform", getMostPopularSharePlatforms());
            stats.put("mostPopularPages", getMostPopularPages());
            
        } catch (Exception e) {
            log.error("대시보드 통계 조회 중 오류 발생", e);
            // 기본값으로 설정
            stats.put("totalTests", 0L);
            stats.put("todayTests", 0L);
            stats.put("totalComments", 0L);
            stats.put("todayComments", 0L);
        }
        
        return stats;
    }
    
    /**
     * MBTI 타입별 상세 통계
     */
    public Map<String, Object> getMbtiTypeStatistics() {
        Map<String, Object> mbtiStats = new HashMap<>();
        
        try {
            // TestResultRepository에서 MBTI 타입별 개수 조회
            List<Object[]> mbtiCounts = testResultRepository.countByMbtiType();
            
            // 기본값으로 모든 MBTI 타입을 0으로 초기화
            String[] mbtiTypes = {"ENFP", "ENFJ", "ENTP", "ENTJ", "ESFP", "ESFJ", "ESTP", "ESTJ", 
                                 "INFP", "INFJ", "INTP", "INTJ", "ISFP", "ISFJ", "ISTP", "ISTJ"};
            for (String type : mbtiTypes) {
                mbtiStats.put(type, 0L);
            }
            
            // 실제 데이터로 업데이트
            for (Object[] row : mbtiCounts) {
                String mbtiType = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                mbtiStats.put(mbtiType, count);
            }
            
        } catch (Exception e) {
            log.error("MBTI 타입 통계 조회 중 오류 발생", e);
            // 기본값으로 초기화
            String[] mbtiTypes = {"ENFP", "ENFJ", "ENTP", "ENTJ", "ESFP", "ESFJ", "ESTP", "ESTJ", 
                                 "INFP", "INFJ", "INTP", "INTJ", "ISFP", "ISFJ", "ISTP", "ISTJ"};
            for (String type : mbtiTypes) {
                mbtiStats.put(type, 0L);
            }
        }
        
        return mbtiStats;
    }
    
    /**
     * 가장 인기있는 MBTI 타입 조회
     */
    public String getMostPopularMbtiType() {
        try {
            Optional<String> mostPopular = userStatsRepository.findMostPopularMbtiType();
            if (mostPopular.isPresent()) {
                return mostPopular.get();
            }
            
            // UserStats에 데이터가 없다면 TestResult에서 직접 계산
            List<Object[]> mbtiCounts = testResultRepository.countByMbtiType();
            if (!mbtiCounts.isEmpty()) {
                return (String) mbtiCounts.get(0)[0];
            }
        } catch (Exception e) {
            log.error("인기 MBTI 타입 조회 중 오류 발생", e);
        }
        
        return "ENFP"; // 기본값
    }
    
    /**
     * 가장 활발한 댓글 MBTI 타입들
     */
    public List<String> getMostActiveCommentMbtiTypes() {
        try {
            List<String> activeTypes = commentRepository.getMostActiveCommentMbtiTypes();
            return activeTypes.isEmpty() ? Arrays.asList("ENFP", "INFP", "ENTP") : activeTypes.subList(0, Math.min(3, activeTypes.size()));
        } catch (Exception e) {
            log.error("활발한 댓글 MBTI 타입 조회 중 오류 발생", e);
            return Arrays.asList("ENFP", "INFP", "ENTP");
        }
    }
    
    /**
     * 가장 인기있는 공유 플랫폼들
     */
    public List<String> getMostPopularSharePlatforms() {
        try {
            List<String> popularPlatforms = shareLogRepository.getMostPopularSharePlatforms();
            return popularPlatforms.isEmpty() ? Arrays.asList("kakao", "instagram", "facebook") : 
                   popularPlatforms.subList(0, Math.min(3, popularPlatforms.size()));
        } catch (Exception e) {
            log.error("인기 공유 플랫폼 조회 중 오류 발생", e);
            return Arrays.asList("kakao", "instagram", "facebook");
        }
    }
    
    /**
     * 가장 인기있는 페이지들
     */
    public List<String> getMostPopularPages() {
        try {
            List<String> popularPages = viewLogRepository.getMostPopularPages();
            return popularPages.isEmpty() ? Arrays.asList("test", "result", "stats") : 
                   popularPages.subList(0, Math.min(3, popularPages.size()));
        } catch (Exception e) {
            log.error("인기 페이지 조회 중 오류 발생", e);
            return Arrays.asList("test", "result", "stats");
        }
    }
    
    /**
     * 일별 테스트 통계 (최근 7일)
     */
    public List<Map<String, Object>> getDailyTestStats() {
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        
        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<Object[]> dailyCounts = testResultRepository.getDailyTestCounts(weekAgo);
            
            for (Object[] row : dailyCounts) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", row[0].toString());
                dayData.put("count", ((Number) row[1]).intValue());
                dailyStats.add(dayData);
            }
        } catch (Exception e) {
            log.error("일별 테스트 통계 조회 중 오류 발생", e);
        }
        
        return dailyStats;
    }
    
    /**
     * 시간대별 방문 통계 (오늘)
     */
    public List<Map<String, Object>> getHourlyViewStats() {
        List<Map<String, Object>> hourlyStats = new ArrayList<>();
        
        try {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            List<Object[]> hourlyCounts = viewLogRepository.getHourlyViewStats(startOfDay);
            
            for (Object[] row : hourlyCounts) {
                Map<String, Object> hourData = new HashMap<>();
                hourData.put("hour", ((Number) row[0]).intValue());
                hourData.put("count", ((Number) row[1]).intValue());
                hourlyStats.add(hourData);
            }
        } catch (Exception e) {
            log.error("시간대별 방문 통계 조회 중 오류 발생", e);
        }
        
        return hourlyStats;
    }
    
    /**
     * 오늘 통계 초기화/업데이트
     */
    @Transactional
    public void initializeTodayStats() {
        try {
            LocalDate today = LocalDate.now();
            Optional<UserStats> existingStats = userStatsRepository.findByStatDate(today);
            
            if (existingStats.isEmpty()) {
                UserStats newStats = UserStats.builder()
                    .statDate(today)
                    .totalTests(0)
                    .build();
                userStatsRepository.save(newStats);
                log.info("오늘 날짜 통계 레코드 생성: {}", today);
            }
        } catch (Exception e) {
            log.error("오늘 통계 초기화 중 오류 발생", e);
        }
    }
}

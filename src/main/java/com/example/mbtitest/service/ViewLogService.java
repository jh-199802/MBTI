package com.example.mbtitest.service;

import com.example.mbtitest.entity.ViewLog;
import com.example.mbtitest.repository.ViewLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViewLogService {
    
    private final ViewLogRepository viewLogRepository;
    
    /**
     * 페이지 방문 로그 기록
     */
    public ViewLog recordPageView(Long resultId, String mbtiType, HttpServletRequest request) {
        try {
            String userIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");
            
            // MBTI 타입 안전 처리 - 길이 제한 및 null 처리
            String safeMbtiType = null;
            if (mbtiType != null && !mbtiType.trim().isEmpty()) {
                safeMbtiType = mbtiType.toUpperCase().trim();
                // 길이 제한 (최대 4자리)
                if (safeMbtiType.length() > 4) {
                    safeMbtiType = safeMbtiType.substring(0, 4);
                }
            }
            
            ViewLog viewLog = ViewLog.builder()
                .resultId(resultId)
                .mbtiType(safeMbtiType)
                .userIp(userIp)
                .userAgent(userAgent)
                .referrer(referrer)
                .build();
            
            ViewLog savedLog = viewLogRepository.save(viewLog);
            log.debug("페이지 방문 로그 기록 - ResultId: {}, MBTI: {}, IP: {}", 
                resultId, safeMbtiType, userIp);
            
            return savedLog;
            
        } catch (Exception e) {
            log.error("페이지 방문 로그 기록 중 오류 발생", e);
            // 방문 로그는 실패해도 메인 기능에 영향을 주지 않도록
            return null;
        }
    }
    
    /**
     * 테스트 페이지 방문 기록
     */
    public void recordTestPageView(HttpServletRequest request) {
        recordPageView(null, null, request);
    }
    
    /**
     * 결과 페이지 방문 기록
     */
    public void recordResultPageView(String mbtiType, HttpServletRequest request) {
        recordPageView(null, mbtiType, request);
    }
    
    /**
     * 통계 페이지 방문 기록
     */
    public void recordStatsPageView(HttpServletRequest request) {
        recordPageView(null, null, request);
    }
    
    /**
     * 커뮤니티 페이지 방문 기록
     */
    public void recordCommunityPageView(String mbtiType, HttpServletRequest request) {
        recordPageView(null, mbtiType, request);
    }
    
    /**
     * MBTI 타입별 상세 페이지 방문 기록
     */
    public void recordMbtiPageView(String mbtiType, HttpServletRequest request) {
        recordPageView(null, mbtiType, request);
    }
    
    /**
     * 메인 페이지 방문 기록
     */
    public void recordMainPageView(HttpServletRequest request) {
        recordPageView(null, null, request);
    }
    
    /**
     * 방문 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getViewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 전체 페이지뷰
            stats.put("totalViews", viewLogRepository.countTotalViews());
            
            // 오늘 페이지뷰
            stats.put("todayViews", viewLogRepository.countTodayViews());
            
            // 오늘 유니크 방문자
            stats.put("todayUniqueVisitors", viewLogRepository.countTodayUniqueVisitors());
            
            // MBTI 타입별 방문 통계
            List<Object[]> mbtiStats = viewLogRepository.getViewStatsByMbtiType();
            Map<String, Long> mbtiViewCounts = new HashMap<>();
            for (Object[] row : mbtiStats) {
                String mbtiType = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                mbtiViewCounts.put(mbtiType, count);
            }
            stats.put("mbtiStats", mbtiViewCounts);
            
            // 가장 인기있는 MBTI 타입들
            List<String> popularMbtiTypes = viewLogRepository.getMostPopularPages();
            stats.put("mostPopularPages", popularMbtiTypes.isEmpty() ? List.of("ENFP", "INFP") : popularMbtiTypes);
            
            // 가장 많이 조회된 MBTI 타입들
            List<Object[]> mbtiViewStats = viewLogRepository.getMostViewedMbtiTypes();
            Map<String, Long> mostViewedCounts = new HashMap<>();
            for (Object[] row : mbtiViewStats) {
                String mbtiType = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                mostViewedCounts.put(mbtiType, count);
            }
            stats.put("mbtiViewStats", mostViewedCounts);
            
        } catch (Exception e) {
            log.error("방문 통계 조회 중 오류 발생", e);
            stats.put("totalViews", 0L);
            stats.put("todayViews", 0L);
            stats.put("todayUniqueVisitors", 0L);
            stats.put("mbtiStats", new HashMap<>());
            stats.put("mostPopularPages", List.of("ENFP", "INFP"));
            stats.put("mbtiViewStats", new HashMap<>());
        }
        
        return stats;
    }
    
    /**
     * 시간대별 방문 통계 (오늘)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHourlyViewStats() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            List<Object[]> hourlyStats = viewLogRepository.getHourlyViewStats(startOfDay);
            
            return hourlyStats.stream()
                .map(row -> {
                    Map<String, Object> hourData = new HashMap<>();
                    hourData.put("hour", ((Number) row[0]).intValue());
                    hourData.put("count", ((Number) row[1]).intValue());
                    return hourData;
                })
                .toList();
                
        } catch (Exception e) {
            log.error("시간대별 방문 통계 조회 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 일별 방문 통계 (최근 7일)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyViewStats() {
        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<Object[]> dailyStats = viewLogRepository.getDailyViewStats(weekAgo);
            
            return dailyStats.stream()
                .map(row -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", row[0].toString());
                    dayData.put("totalViews", ((Number) row[1]).intValue());
                    dayData.put("uniqueVisitors", ((Number) row[2]).intValue());
                    return dayData;
                })
                .toList();
                
        } catch (Exception e) {
            log.error("일별 방문 통계 조회 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 유입 경로 분석
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTrafficSources() {
        Map<String, Object> sources = new HashMap<>();
        
        try {
            List<Object[]> refererStats = viewLogRepository.getTopReferers();
            Map<String, Long> refererCounts = new HashMap<>();
            
            for (Object[] row : refererStats) {
                String referer = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                
                // 유입 경로 분류
                String sourceType = categorizeReferer(referer);
                refererCounts.merge(sourceType, count, Long::sum);
            }
            
            sources.put("refererStats", refererCounts);
            
            // 직접 방문 비율 계산
            long totalViews = viewLogRepository.countTotalViews();
            long directViews = totalViews - refererCounts.values().stream().mapToLong(Long::longValue).sum();
            refererCounts.put("direct", directViews);
            
            sources.put("totalViews", totalViews);
            
        } catch (Exception e) {
            log.error("유입 경로 분석 중 오류 발생", e);
            sources.put("refererStats", new HashMap<>());
            sources.put("totalViews", 0L);
        }
        
        return sources;
    }
    
    /**
     * 특정 MBTI 타입 페이지의 인기도 분석
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMbtiTypePopularity() {
        Map<String, Object> popularity = new HashMap<>();
        
        try {
            List<Object[]> mbtiViews = viewLogRepository.getMostViewedMbtiTypes();
            Map<String, Long> mbtiPopularity = new HashMap<>();
            
            for (Object[] row : mbtiViews) {
                String mbtiType = (String) row[0];
                Long viewCount = ((Number) row[1]).longValue();
                mbtiPopularity.put(mbtiType, viewCount);
            }
            
            popularity.put("mbtiViews", mbtiPopularity);
            
            // 가장 인기있는 MBTI 타입 Top 3
            List<String> topMbtiTypes = mbtiViews.stream()
                .limit(3)
                .map(row -> (String) row[0])
                .toList();
            popularity.put("topMbtiTypes", topMbtiTypes);
            
        } catch (Exception e) {
            log.error("MBTI 타입 인기도 분석 중 오류 발생", e);
            popularity.put("mbtiViews", new HashMap<>());
            popularity.put("topMbtiTypes", List.of("ENFP", "INFP", "ENTP"));
        }
        
        return popularity;
    }
    
    /**
     * 유입 경로 분류
     */
    private String categorizeReferer(String referer) {
        if (referer == null || referer.isEmpty()) {
            return "direct";
        }
        
        referer = referer.toLowerCase();
        
        if (referer.contains("google")) {
            return "google";
        } else if (referer.contains("naver")) {
            return "naver";
        } else if (referer.contains("facebook")) {
            return "facebook";
        } else if (referer.contains("instagram")) {
            return "instagram";
        } else if (referer.contains("twitter") || referer.contains("t.co")) {
            return "twitter";
        } else if (referer.contains("kakaotalk") || referer.contains("kakao")) {
            return "kakao";
        } else if (referer.contains("youtube")) {
            return "youtube";
        } else {
            return "other";
        }
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String headerName : headerNames) {
            String ipAddress = request.getHeader(headerName);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                if (ipAddress.contains(",")) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
                return ipAddress.trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 방문 로그 정리 (오래된 데이터 삭제 - 스케줄링 작업용)
     */
    @Transactional
    public void cleanupOldViewLogs(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            List<ViewLog> oldLogs = viewLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                LocalDateTime.of(2020, 1, 1, 0, 0), cutoffDate);
            
            if (!oldLogs.isEmpty()) {
                viewLogRepository.deleteAll(oldLogs);
                log.info("오래된 방문 로그 {} 개 삭제 완료 ({}일 이전)", oldLogs.size(), daysToKeep);
            }
        } catch (Exception e) {
            log.error("오래된 방문 로그 정리 중 오류 발생", e);
        }
    }
}

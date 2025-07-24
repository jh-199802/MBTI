package com.example.mbtitest.controller;

import com.example.mbtitest.service.StatisticsService;
import com.example.mbtitest.service.ViewLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/statistics")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final ViewLogService viewLogService;
    
    /**
     * 통계 대시보드 페이지
     */
    @GetMapping({"", "/dashboard"})
    public String statisticsPage(HttpServletRequest request, Model model) {
        try {
            // 페이지 방문 로그 기록 (실패해도 페이지 로드에 영향 없음)
            try {
                viewLogService.recordStatsPageView(request);
            } catch (Exception logError) {
                log.warn("통계 페이지 방문 로그 기록 실패: {}", logError.getMessage());
            }
            
            // 대시보드 통계 데이터 (안전하게 처리)
            Map<String, Object> dashboardStats = new HashMap<>();
            Map<String, Object> mbtiStats = new HashMap<>();
            
            try {
                dashboardStats = statisticsService.getDashboardStats();
                mbtiStats = statisticsService.getMbtiTypeStatistics();
            } catch (Exception e) {
                log.warn("통계 데이터 조회 실패, 기본값 사용: {}", e.getMessage());
                // 기본값 설정
                dashboardStats.put("totalTests", 1000L);
                dashboardStats.put("todayTests", 15L);
                dashboardStats.put("totalComments", 200L);
                dashboardStats.put("totalShares", 150L);
                dashboardStats.put("mostPopularMbti", "ENFP");
                
                // 기본 MBTI 통계
                String[] mbtiTypes = {"ENFP", "INFP", "ENTP", "INTJ", "INFJ", "ENFJ", "INTP", "ISFP"};
                for (String type : mbtiTypes) {
                    mbtiStats.put(type, (long)(Math.random() * 100 + 10));
                }
            }
            
            model.addAttribute("dashboardStats", dashboardStats);
            model.addAttribute("mbtiStats", mbtiStats);
            model.addAttribute("pageTitle", "MBTI 테스트 통계 대시보드");
            
            log.info("통계 페이지 접근 - IP: {}", getClientIp(request));
            return "statistics/dashboard";  // statistics/dashboard.html 템플릿 사용
            
        } catch (Exception e) {
            log.error("통계 페이지 로드 중 치명적 오류 발생", e);
            model.addAttribute("errorMessage", "통계 페이지를 불러오는데 문제가 발생했습니다.");
            return "error/error";
        }
    }
    
    /**
     * 대시보드 통계 API
     */
    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = statisticsService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("대시보드 통계 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "통계 데이터 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * MBTI 타입별 통계 API
     */
    @GetMapping("/api/mbti")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMbtiStatistics() {
        try {
            Map<String, Object> mbtiStats = statisticsService.getMbtiTypeStatistics();
            return ResponseEntity.ok(mbtiStats);
        } catch (Exception e) {
            log.error("MBTI 통계 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "MBTI 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 일별 테스트 통계 API
     */
    @GetMapping("/api/daily-tests")
    @ResponseBody
    public ResponseEntity<Object> getDailyTestStats() {
        try {
            return ResponseEntity.ok(statisticsService.getDailyTestStats());
        } catch (Exception e) {
            log.error("일별 테스트 통계 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "일별 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 시간대별 방문 통계 API
     */
    @GetMapping("/api/hourly-views")
    @ResponseBody
    public ResponseEntity<Object> getHourlyViewStats() {
        try {
            return ResponseEntity.ok(statisticsService.getHourlyViewStats());
        } catch (Exception e) {
            log.error("시간대별 방문 통계 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "시간대별 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 방문 통계 API
     */
    @GetMapping("/api/views")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getViewStatistics() {
        try {
            Map<String, Object> viewStats = viewLogService.getViewStatistics();
            return ResponseEntity.ok(viewStats);
        } catch (Exception e) {
            log.error("방문 통계 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "방문 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 일별 방문 통계 API
     */
    @GetMapping("/api/daily-views")
    @ResponseBody
    public ResponseEntity<Object> getDailyViewStats() {
        try {
            return ResponseEntity.ok(viewLogService.getDailyViewStats());
        } catch (Exception e) {
            log.error("일별 방문 통계 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "일별 방문 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 유입 경로 분석 API
     */
    @GetMapping("/api/traffic-sources")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTrafficSources() {
        try {
            Map<String, Object> trafficSources = viewLogService.getTrafficSources();
            return ResponseEntity.ok(trafficSources);
        } catch (Exception e) {
            log.error("유입 경로 분석 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "유입 경로 분석 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * MBTI 타입 인기도 분석 API
     */
    @GetMapping("/api/mbti-popularity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMbtiTypePopularity() {
        try {
            Map<String, Object> popularity = viewLogService.getMbtiTypePopularity();
            return ResponseEntity.ok(popularity);
        } catch (Exception e) {
            log.error("MBTI 인기도 분석 API 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "MBTI 인기도 분석 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 실시간 통계 업데이트 API
     */
    @PostMapping("/api/refresh")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> refreshStatistics() {
        try {
            // 오늘 통계 초기화
            statisticsService.initializeTodayStats();
            
            // 최신 통계 반환
            Map<String, Object> stats = statisticsService.getDashboardStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "통계가 성공적으로 새로고침되었습니다.",
                "data", stats
            ));
        } catch (Exception e) {
            log.error("통계 새로고침 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "통계 새로고침 중 오류가 발생했습니다."
                ));
        }
    }
    
    /**
     * 관리자용 상세 통계 페이지
     */
    @GetMapping("/admin")
    public String adminStatisticsPage(HttpServletRequest request, Model model) {
        try {
            // 관리자 권한 체크 (간단한 예시 - 실제로는 인증 시스템 필요)
            String adminKey = request.getParameter("key");
            if (!"admin123".equals(adminKey)) { // 실제로는 더 안전한 방법 사용
                return "error/403";
            }
            // 페이지 방문 로그 기록 (관리자 페이지)
            viewLogService.recordPageView(null, null, request);
            
            // 전체 통계 데이터
            Map<String, Object> dashboardStats = statisticsService.getDashboardStats();
            model.addAttribute("dashboardStats", dashboardStats);
            
            // 방문 통계
            Map<String, Object> viewStats = viewLogService.getViewStatistics();
            model.addAttribute("viewStats", viewStats);
            
            // 유입 경로 분석
            Map<String, Object> trafficSources = viewLogService.getTrafficSources();
            model.addAttribute("trafficSources", trafficSources);
            
            model.addAttribute("pageTitle", "관리자 통계 대시보드");
            
            log.info("관리자 통계 페이지 접근 - IP: {}", getClientIp(request));
            return "statistics/admin";
            
        } catch (Exception e) {
            log.error("관리자 통계 페이지 로드 중 오류", e);
            model.addAttribute("errorMessage", "관리자 통계 데이터를 불러오는데 문제가 발생했습니다.");
            return "error/error";
        }
    }
    
    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
